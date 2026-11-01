package com.sealth.sealthapi.controllers;

import com.sealth.sealthapi.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AppointmentControllerTest extends IntegrationTestBase {

    private static final Map<String, Object> VALID_BODY = Map.of(
            "date", "2030-05-10", "doctor", "Dr. Silva", "specialty", "Cardiologia",
            "location", "Hospital ABC", "extraInfo", "Retorno");

    @Test
    void createReturnsCreatedAppointment() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(withJson(withAuth(post("/appointments/create"), user), VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.date").value("2030-05-10"))
                .andExpect(jsonPath("$.doctor").value("Dr. Silva"))
                .andExpect(jsonPath("$.extraInfo").value("Retorno"));
    }

    @Test
    void createWithoutRequiredFieldsReturnsBadRequest() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(withJson(withAuth(post("/appointments/create"), user), Map.of("location", "x")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.date").exists())
                .andExpect(jsonPath("$.fieldErrors.doctor").exists())
                .andExpect(jsonPath("$.fieldErrors.specialty").exists());
    }

    @Test
    void createWithInvalidDateFormatReturnsBadRequest() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(withJson(withAuth(post("/appointments/create"), user),
                        Map.of("date", "10/05/2030", "doctor", "Dr", "specialty", "Cardio")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(withJson(post("/appointments/create"), VALID_BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listReturnsOnlyOwnAppointmentsOrderedByDate() throws Exception {
        TestUser owner = registerUser();
        TestUser other = registerUser();
        createAppointment(owner, "2030-06-01", "Dr. Tarde");
        createAppointment(owner, "2030-01-01", "Dr. Cedo");
        createAppointment(other, "2030-03-01", "Dr. Outro");

        mockMvc.perform(withAuth(get("/appointments/all"), owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].doctor").value("Dr. Cedo"))
                .andExpect(jsonPath("$[1].doctor").value("Dr. Tarde"));

        mockMvc.perform(withAuth(get("/appointments/all"), other))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void updateChangesAppointment() throws Exception {
        TestUser user = registerUser();
        long id = createAppointment(user, "2030-05-10", "Dr. Silva");

        mockMvc.perform(withJson(withAuth(put("/appointments/" + id + "/update"), user),
                        Map.of("date", "2030-07-20", "doctor", "Dra. Souza", "specialty", "Ortopedia", "extraInfo", "novo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.date").value("2030-07-20"))
                .andExpect(jsonPath("$.doctor").value("Dra. Souza"))
                .andExpect(jsonPath("$.extraInfo").value("novo"));
    }

    @Test
    void updateAcceptsLegacyExtraInformationField() throws Exception {
        TestUser user = registerUser();
        long id = createAppointment(user, "2030-05-10", "Dr. Silva");

        mockMvc.perform(withJson(withAuth(put("/appointments/" + id + "/update"), user),
                        Map.of("date", "2030-05-10", "doctor", "Dr. Silva", "specialty", "Cardiologia", "extraInformation", "legado")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.extraInfo").value("legado"));
    }

    @Test
    void updateWithInvalidDataReturnsBadRequest() throws Exception {
        TestUser user = registerUser();
        long id = createAppointment(user, "2030-05-10", "Dr. Silva");

        mockMvc.perform(withJson(withAuth(put("/appointments/" + id + "/update"), user), Map.of("doctor", "")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateOfAnotherUsersAppointmentReturnsNotFound() throws Exception {
        TestUser owner = registerUser();
        TestUser intruder = registerUser();
        long id = createAppointment(owner, "2030-05-10", "Dr. Silva");

        mockMvc.perform(withJson(withAuth(put("/appointments/" + id + "/update"), intruder), VALID_BODY))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelRemovesAppointment() throws Exception {
        TestUser user = registerUser();
        long id = createAppointment(user, "2030-05-10", "Dr. Silva");

        mockMvc.perform(withAuth(delete("/appointments/" + id + "/cancel"), user))
                .andExpect(status().isNoContent());

        mockMvc.perform(withAuth(get("/appointments/all"), user))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void cancelOfAnotherUsersAppointmentReturnsNotFoundAndKeepsIt() throws Exception {
        TestUser owner = registerUser();
        TestUser intruder = registerUser();
        long id = createAppointment(owner, "2030-05-10", "Dr. Silva");

        mockMvc.perform(withAuth(delete("/appointments/" + id + "/cancel"), intruder))
                .andExpect(status().isNotFound());

        mockMvc.perform(withAuth(get("/appointments/all"), owner))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void cancelOfUnknownAppointmentReturnsNotFound() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(withAuth(delete("/appointments/999999/cancel"), user))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Consulta não encontrada"));
    }

    @Test
    void nonNumericIdReturnsBadRequest() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(withAuth(delete("/appointments/abc/cancel"), user))
                .andExpect(status().isBadRequest());
    }
}
