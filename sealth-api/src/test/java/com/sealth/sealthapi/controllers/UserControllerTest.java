package com.sealth.sealthapi.controllers;

import com.sealth.sealthapi.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends IntegrationTestBase {

    @Test
    void getReturnsOwnDataWithoutPassword() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(withAuth(get("/user/" + user.id()), user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.id()))
                .andExpect(jsonPath("$.email").value(user.email()))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void getAnotherUserReturnsForbidden() throws Exception {
        TestUser user = registerUser();
        TestUser other = registerUser();

        mockMvc.perform(withAuth(get("/user/" + other.id()), user))
                .andExpect(status().isForbidden());
    }

    @Test
    void getWithoutTokenReturnsUnauthorized() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(get("/user/" + user.id()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateChangesNameEmailAndPassword() throws Exception {
        TestUser user = registerUser();
        String newEmail = "novo-" + user.email();

        mockMvc.perform(withJson(withAuth(put("/user/update/" + user.id()), user),
                        Map.of("name", "Nome Novo", "email", newEmail, "password", "nova-senha")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nome Novo"))
                .andExpect(jsonPath("$.email").value(newEmail))
                .andExpect(jsonPath("$.password").doesNotExist());

        mockMvc.perform(withJson(post("/auth/login"), Map.of("email", newEmail, "password", "nova-senha")))
                .andExpect(status().isOk());
        mockMvc.perform(withJson(post("/auth/login"), Map.of("email", newEmail, "password", PASSWORD)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateWithEmptyFieldsKeepsCurrentValues() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(withJson(withAuth(put("/user/update/" + user.id()), user),
                        Map.of("name", "", "email", "", "password", "")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Usuário Teste"))
                .andExpect(jsonPath("$.email").value(user.email()));

        mockMvc.perform(withJson(post("/auth/login"), Map.of("email", user.email(), "password", PASSWORD)))
                .andExpect(status().isOk());
    }

    @Test
    void updateKeepingSameEmailIsAllowed() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(withJson(withAuth(put("/user/update/" + user.id()), user), Map.of("email", user.email())))
                .andExpect(status().isOk());
    }

    @Test
    void updateWithEmailOfAnotherUserReturnsConflict() throws Exception {
        TestUser user = registerUser();
        TestUser other = registerUser();

        mockMvc.perform(withJson(withAuth(put("/user/update/" + user.id()), user), Map.of("email", other.email())))
                .andExpect(status().isConflict());
    }

    @Test
    void updateWithInvalidDataReturnsBadRequest() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(withJson(withAuth(put("/user/update/" + user.id()), user),
                        Map.of("email", "invalido", "password", "123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    void updateAnotherUserReturnsForbidden() throws Exception {
        TestUser user = registerUser();
        TestUser other = registerUser();

        mockMvc.perform(withJson(withAuth(put("/user/update/" + other.id()), user), Map.of("name", "Hacker")))
                .andExpect(status().isForbidden());
    }
}
