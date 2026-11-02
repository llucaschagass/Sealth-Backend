package com.sealth.sealthapi.controllers;

import com.sealth.sealthapi.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VaccineControllerTest extends IntegrationTestBase {

    private long addDose(TestUser user, String code, LocalDate takenAt) throws Exception {
        String response = mockMvc.perform(withJson(withAuth(post("/vaccines/records"), user),
                        Map.of("vaccineCode", code, "takenAt", takenAt.toString())))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private void saveAge(TestUser user, int age) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("weightKg", 70.0);
        body.put("heightCm", 175.0);
        body.put("age", age);
        body.put("sex", "masculino");
        mockMvc.perform(withJson(withAuth(post("/health/records"), user), body)).andExpect(status().isCreated());
    }

    @Test
    void addRecordReturnsTheDoseWithTheVaccineName() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(withJson(withAuth(post("/vaccines/records"), user),
                        Map.of("vaccineCode", "influenza", "takenAt", "2026-04-10")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.vaccineCode").value("influenza"))
                .andExpect(jsonPath("$.vaccineName").value("Gripe (influenza)"))
                .andExpect(jsonPath("$.takenAt").value("2026-04-10"));
    }

    @Test
    void addRecordWithoutFieldsListsEachOne() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(withJson(withAuth(post("/vaccines/records"), user), Map.of()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.vaccineCode").exists())
                .andExpect(jsonPath("$.fieldErrors.takenAt").exists());
    }

    @Test
    void addRecordRejectsUnknownVaccineAndFutureDate() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(withJson(withAuth(post("/vaccines/records"), user),
                        Map.of("vaccineCode", "inexistente", "takenAt", "2026-04-10")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.knownVaccine").value("Vacina desconhecida"));

        mockMvc.perform(withJson(withAuth(post("/vaccines/records"), user),
                        Map.of("vaccineCode", "influenza", "takenAt", LocalDate.now().plusDays(1).toString())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.takenAt").value("A data da dose não pode ser futura"));
    }

    @Test
    void addRecordAcceptsToday() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(withJson(withAuth(post("/vaccines/records"), user),
                        Map.of("vaccineCode", "influenza", "takenAt", LocalDate.now().toString())))
                .andExpect(status().isCreated());
    }

    @Test
    void listRecordsReturnsOnlyOwnDosesNewestFirst() throws Exception {
        TestUser owner = registerUser();
        TestUser other = registerUser();
        addDose(owner, "hepatite-b", LocalDate.now().minusYears(2));
        addDose(owner, "influenza", LocalDate.now().minusMonths(1));
        addDose(other, "dt", LocalDate.now().minusMonths(3));

        mockMvc.perform(withAuth(get("/vaccines/records"), owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].vaccineCode").value("influenza"))
                .andExpect(jsonPath("$[1].vaccineCode").value("hepatite-b"));

        mockMvc.perform(withAuth(get("/vaccines/records"), other))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void deleteRemovesOwnDose() throws Exception {
        TestUser user = registerUser();
        long id = addDose(user, "influenza", LocalDate.now().minusMonths(1));

        mockMvc.perform(withAuth(delete("/vaccines/records/" + id), user)).andExpect(status().isNoContent());

        mockMvc.perform(withAuth(get("/vaccines/records"), user)).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void deleteOfAnotherUsersDoseReturnsNotFoundAndKeepsIt() throws Exception {
        TestUser owner = registerUser();
        TestUser intruder = registerUser();
        long id = addDose(owner, "influenza", LocalDate.now().minusMonths(1));

        mockMvc.perform(withAuth(delete("/vaccines/records/" + id), intruder))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Dose não encontrada"));

        mockMvc.perform(withAuth(get("/vaccines/records"), owner)).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void overviewWithoutHealthRecordsHasNoAgeAndListsTheWholeCalendar() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(withAuth(get("/vaccines"), user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ageYears").value(nullValue()))
                .andExpect(jsonPath("$.vaccines", hasSize(7)))
                .andExpect(jsonPath("$.vaccines[0].code").value("hepatite-b"))
                .andExpect(jsonPath("$.vaccines[0].status").value("PENDING"));
    }

    @Test
    void overviewUsesTheLatestHealthRecordAge() throws Exception {
        TestUser user = registerUser();
        saveAge(user, 30);

        mockMvc.perform(withAuth(get("/vaccines"), user))
                .andExpect(jsonPath("$.ageYears").value(30))
                .andExpect(jsonPath("$.vaccines[?(@.code=='hpv')].status").value("NOT_APPLICABLE"))
                .andExpect(jsonPath("$.vaccines[?(@.code=='febre-amarela')].status").value("PENDING"));
    }

    @Test
    void overviewReflectsTheDosesTaken() throws Exception {
        TestUser user = registerUser();
        saveAge(user, 30);
        addDose(user, "influenza", LocalDate.now().minusMonths(2));
        addDose(user, "hepatite-b", LocalDate.now().minusYears(10));

        mockMvc.perform(withAuth(get("/vaccines"), user))
                .andExpect(jsonPath("$.vaccines[?(@.code=='influenza')].status").value("UP_TO_DATE"))
                .andExpect(jsonPath("$.vaccines[?(@.code=='influenza')].dosesTaken").value(1))
                .andExpect(jsonPath("$.vaccines[?(@.code=='influenza')].nextDueDate")
                        .value(LocalDate.now().minusMonths(2).plusMonths(12).toString()))
                .andExpect(jsonPath("$.vaccines[?(@.code=='hepatite-b')].status").value("PENDING"))
                .andExpect(jsonPath("$.vaccines[?(@.code=='hepatite-b')].dosesTaken").value(1));
    }

    @Test
    void alertsListOnlyWhatNeedsAttentionMostUrgentFirst() throws Exception {
        TestUser user = registerUser();
        saveAge(user, 30);
        // reforço vencido
        addDose(user, "dt", LocalDate.now().minusYears(15));
        addDose(user, "dt", LocalDate.now().minusYears(14));
        addDose(user, "dt", LocalDate.now().minusYears(11));
        // reforço nos próximos dias
        addDose(user, "influenza", LocalDate.now().minusMonths(12).plusDays(10));

        mockMvc.perform(withAuth(get("/vaccines/alerts"), user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("dt"))
                .andExpect(jsonPath("$[0].status").value("DUE"))
                .andExpect(jsonPath("$[?(@.code=='influenza')].status").value("SOON"))
                .andExpect(jsonPath("$[?(@.code=='hepatite-b')].status").value("PENDING"))
                .andExpect(jsonPath("$[?(@.code=='hpv')]").isEmpty())
                .andExpect(jsonPath("$[?(@.code=='triplice-viral')]").isEmpty());
    }

    @Test
    void alertsAreEmptyWhenEverythingIsInOrder() throws Exception {
        TestUser user = registerUser();
        saveAge(user, 30);
        addDose(user, "hepatite-b", LocalDate.now().minusYears(10));
        addDose(user, "hepatite-b", LocalDate.now().minusYears(10).plusMonths(1));
        addDose(user, "hepatite-b", LocalDate.now().minusYears(10).plusMonths(6));
        addDose(user, "dt", LocalDate.now().minusYears(2));
        addDose(user, "dt", LocalDate.now().minusYears(2).plusMonths(1));
        addDose(user, "dt", LocalDate.now().minusYears(2).plusMonths(6));
        addDose(user, "febre-amarela", LocalDate.now().minusYears(5));
        addDose(user, "influenza", LocalDate.now().minusMonths(1));

        mockMvc.perform(withAuth(get("/vaccines/alerts"), user))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void endpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/vaccines")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/vaccines/alerts")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/vaccines/records")).andExpect(status().isUnauthorized());
        mockMvc.perform(withJson(post("/vaccines/records"), Map.of("vaccineCode", "dt", "takenAt", "2026-01-01")))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/vaccines/records/1")).andExpect(status().isUnauthorized());
    }
}
