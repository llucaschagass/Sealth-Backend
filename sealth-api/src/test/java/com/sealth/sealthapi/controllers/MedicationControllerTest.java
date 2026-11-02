package com.sealth.sealthapi.controllers;

import com.sealth.sealthapi.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MedicationControllerTest extends IntegrationTestBase {

    private static Map<String, Object> body(String name, List<String> times) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("dosage", "500 mg");
        map.put("times", times);
        return map;
    }

    private long createMedication(TestUser user, Map<String, Object> body) throws Exception {
        String response = mockMvc.perform(withJson(withAuth(post("/medicines/mine"), user), body))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    void createReturnsTheSavedMedicationWithSortedUniqueTimes() throws Exception {
        TestUser user = registerUser();
        Map<String, Object> body = body("  Paracetamol ", List.of("20:00", "08:00", "20:00"));
        body.put("notes", "  Após as refeições ");
        body.put("startDate", "2026-01-10");

        mockMvc.perform(withJson(withAuth(post("/medicines/mine"), user), body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Paracetamol"))
                .andExpect(jsonPath("$.dosage").value("500 mg"))
                .andExpect(jsonPath("$.times", contains("08:00", "20:00")))
                .andExpect(jsonPath("$.startDate").value("2026-01-10"))
                .andExpect(jsonPath("$.notes").value("Após as refeições"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void blankNotesAreStoredAsNull() throws Exception {
        TestUser user = registerUser();
        Map<String, Object> body = body("Dipirona", List.of("08:00"));
        body.put("notes", "   ");

        mockMvc.perform(withJson(withAuth(post("/medicines/mine"), user), body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.notes").doesNotExist());
    }

    @Test
    void createWithoutRequiredFieldsListsEachOne() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(withJson(withAuth(post("/medicines/mine"), user), Map.of()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.dosage").exists())
                .andExpect(jsonPath("$.fieldErrors.times").exists());
    }

    @Test
    void timesMustBeValidAndNotTooMany() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(withJson(withAuth(post("/medicines/mine"), user), body("A", List.of("25:00"))))
                .andExpect(status().isBadRequest());
        mockMvc.perform(withJson(withAuth(post("/medicines/mine"), user), body("A", List.of("8:00"))))
                .andExpect(status().isBadRequest());
        mockMvc.perform(withJson(withAuth(post("/medicines/mine"), user), body("A", List.of("12:60"))))
                .andExpect(status().isBadRequest());
        mockMvc.perform(withJson(withAuth(post("/medicines/mine"), user), body("A", List.of())))
                .andExpect(status().isBadRequest());

        List<String> thirteen = java.util.stream.IntStream.range(0, 13).mapToObj(i -> String.format("%02d:00", i)).toList();
        mockMvc.perform(withJson(withAuth(post("/medicines/mine"), user), body("A", thirteen)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.times").value("Informe no máximo 12 horários"));
    }

    @Test
    void endDateCannotBeBeforeStartDate() throws Exception {
        TestUser user = registerUser();
        Map<String, Object> body = body("A", List.of("08:00"));
        body.put("startDate", "2026-05-10");
        body.put("endDate", "2026-05-09");

        mockMvc.perform(withJson(withAuth(post("/medicines/mine"), user), body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.period").exists());
    }

    @Test
    void finishedTreatmentIsInactiveAndListedAfterActiveOnes() throws Exception {
        TestUser user = registerUser();
        Map<String, Object> finished = body("Amoxicilina", List.of("08:00"));
        finished.put("endDate", LocalDate.now().minusDays(1).toString());
        Map<String, Object> endsToday = body("Cefalexina", List.of("08:00"));
        endsToday.put("endDate", LocalDate.now().toString());
        createMedication(user, finished);
        createMedication(user, endsToday);
        createMedication(user, body("Zinco", List.of("09:00")));

        mockMvc.perform(withAuth(get("/medicines/mine"), user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].name").value("Cefalexina"))
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$[1].name").value("Zinco"))
                .andExpect(jsonPath("$[2].name").value("Amoxicilina"))
                .andExpect(jsonPath("$[2].active").value(false));
    }

    @Test
    void listReturnsOnlyOwnMedications() throws Exception {
        TestUser owner = registerUser();
        TestUser other = registerUser();
        createMedication(owner, body("A", List.of("08:00")));
        createMedication(other, body("B", List.of("08:00")));

        mockMvc.perform(withAuth(get("/medicines/mine"), owner))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("A"));
    }

    @Test
    void updateReplacesAllFields() throws Exception {
        TestUser user = registerUser();
        Map<String, Object> original = body("A", List.of("08:00", "20:00"));
        original.put("notes", "antiga");
        long id = createMedication(user, original);

        Map<String, Object> changed = body("B", List.of("12:00"));
        changed.put("dosage", "1 comprimido");

        mockMvc.perform(withJson(withAuth(put("/medicines/mine/" + id), user), changed))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("B"))
                .andExpect(jsonPath("$.dosage").value("1 comprimido"))
                .andExpect(jsonPath("$.times", contains("12:00")))
                .andExpect(jsonPath("$.notes").doesNotExist());

        mockMvc.perform(withAuth(get("/medicines/mine"), user))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].times", contains("12:00")));
    }

    @Test
    void updateValidatesTheBody() throws Exception {
        TestUser user = registerUser();
        long id = createMedication(user, body("A", List.of("08:00")));

        mockMvc.perform(withJson(withAuth(put("/medicines/mine/" + id), user), body("", List.of("08:00"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }

    @Test
    void updateOfUnknownOrAnotherUsersMedicationReturnsNotFound() throws Exception {
        TestUser owner = registerUser();
        TestUser intruder = registerUser();
        long id = createMedication(owner, body("A", List.of("08:00")));

        mockMvc.perform(withJson(withAuth(put("/medicines/mine/" + id), intruder), body("X", List.of("08:00"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Medicamento não encontrado"));
        mockMvc.perform(withJson(withAuth(put("/medicines/mine/999999"), owner), body("X", List.of("08:00"))))
                .andExpect(status().isNotFound());

        mockMvc.perform(withAuth(get("/medicines/mine"), owner))
                .andExpect(jsonPath("$[0].name").value("A"));
    }

    @Test
    void deleteRemovesOwnMedication() throws Exception {
        TestUser user = registerUser();
        long id = createMedication(user, body("A", List.of("08:00")));

        mockMvc.perform(withAuth(delete("/medicines/mine/" + id), user))
                .andExpect(status().isNoContent());

        mockMvc.perform(withAuth(get("/medicines/mine"), user))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void deleteOfAnotherUsersMedicationReturnsNotFoundAndKeepsIt() throws Exception {
        TestUser owner = registerUser();
        TestUser intruder = registerUser();
        long id = createMedication(owner, body("A", List.of("08:00")));

        mockMvc.perform(withAuth(delete("/medicines/mine/" + id), intruder))
                .andExpect(status().isNotFound());

        mockMvc.perform(withAuth(get("/medicines/mine"), owner))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void endpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/medicines/mine")).andExpect(status().isUnauthorized());
        mockMvc.perform(withJson(post("/medicines/mine"), body("A", List.of("08:00"))))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(withJson(put("/medicines/mine/1"), body("A", List.of("08:00"))))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/medicines/mine/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void existingPdfListEndpointIsUnchanged() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(withAuth(get("/medicines/pdf"), user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)));
    }
}
