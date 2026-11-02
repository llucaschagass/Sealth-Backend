package com.sealth.sealthapi.controllers;

import com.sealth.sealthapi.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HealthRecordControllerTest extends IntegrationTestBase {

    private static Map<String, Object> body(double weight, Integer systolic, Integer diastolic) {
        Map<String, Object> map = new HashMap<>();
        map.put("weightKg", weight);
        map.put("heightCm", 175.0);
        map.put("age", 30);
        map.put("sex", "masculino");
        map.put("systolic", systolic);
        map.put("diastolic", diastolic);
        return map;
    }

    private long createRecord(TestUser user, double weight) throws Exception {
        String response = mockMvc.perform(withJson(withAuth(post("/health/records"), user), body(weight, null, null)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    void createReturnsTheSavedRecordWithServerTimestamp() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(withJson(withAuth(post("/health/records"), user), body(72.5, 120, 80)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.measuredAt").exists())
                .andExpect(jsonPath("$.weightKg").value(72.5))
                .andExpect(jsonPath("$.heightCm").value(175.0))
                .andExpect(jsonPath("$.age").value(30))
                .andExpect(jsonPath("$.sex").value("masculino"))
                .andExpect(jsonPath("$.systolic").value(120))
                .andExpect(jsonPath("$.diastolic").value(80));
    }

    @Test
    void bloodPressureIsOptional() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(withJson(withAuth(post("/health/records"), user), body(72.5, null, null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.systolic").doesNotExist())
                .andExpect(jsonPath("$.diastolic").doesNotExist());
    }

    @Test
    void createWithoutRequiredFieldsListsEachOne() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(withJson(withAuth(post("/health/records"), user), Map.of()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.weightKg").exists())
                .andExpect(jsonPath("$.fieldErrors.heightCm").exists())
                .andExpect(jsonPath("$.fieldErrors.age").exists())
                .andExpect(jsonPath("$.fieldErrors.sex").exists());
    }

    @Test
    void createRejectsValuesOutsideTheLimits() throws Exception {
        TestUser user = registerUser();
        Map<String, Object> invalid = body(10, null, null);
        invalid.put("heightCm", 300.0);
        invalid.put("age", 0);
        invalid.put("sex", "outro");

        mockMvc.perform(withJson(withAuth(post("/health/records"), user), invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.weightKg").value("O peso deve estar entre 20 e 400 kg"))
                .andExpect(jsonPath("$.fieldErrors.heightCm").exists())
                .andExpect(jsonPath("$.fieldErrors.age").exists())
                .andExpect(jsonPath("$.fieldErrors.sex").exists());
    }

    @Test
    void bloodPressureMustBeCompleteAndConsistent() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(withJson(withAuth(post("/health/records"), user), body(70, 120, null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.bloodPressure").exists());

        mockMvc.perform(withJson(withAuth(post("/health/records"), user), body(70, 80, 120)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.bloodPressure").exists());
    }

    @Test
    void listReturnsOnlyOwnRecordsNewestFirst() throws Exception {
        TestUser owner = registerUser();
        TestUser other = registerUser();
        createRecord(owner, 80);
        createRecord(owner, 78);
        createRecord(other, 60);

        mockMvc.perform(withAuth(get("/health/records"), owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].weightKg").value(78.0))
                .andExpect(jsonPath("$[1].weightKg").value(80.0));

        mockMvc.perform(withAuth(get("/health/records"), other))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void latestReturnsTheMostRecentRecord() throws Exception {
        TestUser user = registerUser();
        createRecord(user, 80);
        createRecord(user, 75);

        mockMvc.perform(withAuth(get("/health/records/latest"), user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weightKg").value(75.0));
    }

    @Test
    void latestWithoutRecordsReturnsNotFound() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(withAuth(get("/health/records/latest"), user))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Nenhuma medição registrada"));
    }

    @Test
    void deleteRemovesOwnRecord() throws Exception {
        TestUser user = registerUser();
        long id = createRecord(user, 80);

        mockMvc.perform(withAuth(delete("/health/records/" + id), user))
                .andExpect(status().isNoContent());

        mockMvc.perform(withAuth(get("/health/records"), user))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void deleteOfAnotherUsersRecordReturnsNotFoundAndKeepsIt() throws Exception {
        TestUser owner = registerUser();
        TestUser intruder = registerUser();
        long id = createRecord(owner, 80);

        mockMvc.perform(withAuth(delete("/health/records/" + id), intruder))
                .andExpect(status().isNotFound());

        mockMvc.perform(withAuth(get("/health/records"), owner))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void endpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/health/records")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/health/records/latest")).andExpect(status().isUnauthorized());
        mockMvc.perform(withJson(post("/health/records"), body(70, null, null)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/health/records/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void existingDefaultsEndpointIsUnchanged() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(withAuth(get("/health/defaults"), user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weight").value(70.0))
                .andExpect(jsonPath("$.height").value(1.75));
    }
}
