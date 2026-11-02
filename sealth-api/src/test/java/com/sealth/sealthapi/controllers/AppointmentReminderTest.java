package com.sealth.sealthapi.controllers;

import com.sealth.sealthapi.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Horário e lembrete das consultas. */
class AppointmentReminderTest extends IntegrationTestBase {

    private static Map<String, Object> body(String date, String doctor, String time, Integer reminderMinutes) {
        Map<String, Object> map = new HashMap<>();
        map.put("date", date);
        map.put("doctor", doctor);
        map.put("specialty", "Cardiologia");
        map.put("time", time);
        map.put("reminderMinutes", reminderMinutes);
        return map;
    }

    private long create(TestUser user, Map<String, Object> body) throws Exception {
        String response = mockMvc.perform(withJson(withAuth(post("/appointments/create"), user), body))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private static String days(int offset) {
        return LocalDate.now().plusDays(offset).toString();
    }

    @Test
    void createReturnsTimeAndTheMomentTheReminderStartsToApply() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(withJson(withAuth(post("/appointments/create"), user), body("2030-05-10", "Dr. Silva", "14:30", 90)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.time").value("14:30"))
                .andExpect(jsonPath("$.reminderMinutes").value(90))
                .andExpect(jsonPath("$.reminderAt").value("2030-05-10T13:00:00"));
    }

    @Test
    void timeAndReminderAreOptional() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(withJson(withAuth(post("/appointments/create"), user), body("2030-05-10", "Dr. Silva", null, null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.time").doesNotExist())
                .andExpect(jsonPath("$.reminderMinutes").doesNotExist())
                .andExpect(jsonPath("$.reminderAt").doesNotExist());
    }

    @Test
    void timeWithoutReminderHasNoReminderMoment() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(withJson(withAuth(post("/appointments/create"), user), body("2030-05-10", "Dr. Silva", "08:00", null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.time").value("08:00"))
                .andExpect(jsonPath("$.reminderAt").doesNotExist());
    }

    @Test
    void reminderRequiresATime() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(withJson(withAuth(post("/appointments/create"), user), body("2030-05-10", "Dr. Silva", null, 60)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.reminder").exists());
    }

    @Test
    void reminderMustBeWithinTheLimits() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(withJson(withAuth(post("/appointments/create"), user), body("2030-05-10", "Dr. Silva", "08:00", -1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.reminderMinutes").exists());
        mockMvc.perform(withJson(withAuth(post("/appointments/create"), user), body("2030-05-10", "Dr. Silva", "08:00", 10081)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.reminderMinutes").exists());
    }

    @Test
    void invalidTimeFormatReturnsBadRequest() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(withJson(withAuth(post("/appointments/create"), user), body("2030-05-10", "Dr. Silva", "25:00", null)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(withJson(withAuth(post("/appointments/create"), user), body("2030-05-10", "Dr. Silva", "8h", null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateReplacesTimeAndReminder() throws Exception {
        TestUser user = registerUser();
        long id = create(user, body("2030-05-10", "Dr. Silva", "08:00", 30));

        mockMvc.perform(withJson(withAuth(put("/appointments/" + id + "/update"), user), body("2030-05-10", "Dr. Silva", "16:45", 120)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.time").value("16:45"))
                .andExpect(jsonPath("$.reminderAt").value("2030-05-10T14:45:00"));

        mockMvc.perform(withJson(withAuth(put("/appointments/" + id + "/update"), user), body("2030-05-10", "Dr. Silva", null, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.time").doesNotExist())
                .andExpect(jsonPath("$.reminderAt").doesNotExist());
    }

    @Test
    void updateRejectsReminderWithoutTime() throws Exception {
        TestUser user = registerUser();
        long id = create(user, body("2030-05-10", "Dr. Silva", "08:00", 30));

        mockMvc.perform(withJson(withAuth(put("/appointments/" + id + "/update"), user), body("2030-05-10", "Dr. Silva", null, 30)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.reminder").exists());
    }

    @Test
    void listOrdersTheSameDayByTimeWithUntimedLast() throws Exception {
        TestUser user = registerUser();
        create(user, body("2030-05-10", "Sem horário", null, null));
        create(user, body("2030-05-10", "Tarde", "15:00", null));
        create(user, body("2030-05-10", "Cedo", "07:30", null));
        create(user, body("2030-05-09", "Dia anterior", "23:00", null));

        mockMvc.perform(withAuth(get("/appointments/all"), user))
                .andExpect(jsonPath("$[0].doctor").value("Dia anterior"))
                .andExpect(jsonPath("$[1].doctor").value("Cedo"))
                .andExpect(jsonPath("$[2].doctor").value("Tarde"))
                .andExpect(jsonPath("$[3].doctor").value("Sem horário"));
    }

    @Test
    void dueRemindersListsOnlyAppointmentsWhoseReminderAlreadyApplies() throws Exception {
        TestUser user = registerUser();
        // lembrete 3 dias antes de uma consulta amanhã: já vale
        create(user, body(days(1), "Em vigor", "12:00", 4320));
        // lembrete 1 hora antes de uma consulta daqui a 5 dias: ainda não vale
        create(user, body(days(5), "Futuro", "12:00", 60));
        // consulta passada com lembrete: não conta
        create(user, body(days(-1), "Passada", "12:00", 4320));
        // sem lembrete
        create(user, body(days(1), "Sem lembrete", "12:00", null));
        // sem horário
        create(user, body(days(1), "Sem horário", null, null));

        mockMvc.perform(withAuth(get("/appointments/reminders"), user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].doctor").value("Em vigor"));
    }

    @Test
    void dueRemindersAreOrderedByStartAndIsolatedPerUser() throws Exception {
        TestUser owner = registerUser();
        TestUser other = registerUser();
        create(owner, body(days(3), "Depois", "08:00", 10080));
        create(owner, body(days(1), "Antes", "08:00", 10080));
        create(other, body(days(2), "De outro", "08:00", 10080));

        mockMvc.perform(withAuth(get("/appointments/reminders"), owner))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].doctor").value("Antes"))
                .andExpect(jsonPath("$[1].doctor").value("Depois"));
    }

    @Test
    void remindersRequireAuthentication() throws Exception {
        mockMvc.perform(get("/appointments/reminders")).andExpect(status().isUnauthorized());
    }
}
