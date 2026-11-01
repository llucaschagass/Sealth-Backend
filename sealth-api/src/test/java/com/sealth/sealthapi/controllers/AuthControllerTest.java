package com.sealth.sealthapi.controllers;

import com.sealth.sealthapi.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends IntegrationTestBase {

    @Test
    void registerReturnsTokenAndUserData() throws Exception {
        mockMvc.perform(withJson(post("/auth/register"),
                        Map.of("name", "Ana", "email", "ana-register@test.com", "password", PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana"))
                .andExpect(jsonPath("$.token").value(notNullValue()))
                .andExpect(jsonPath("$.id").value(notNullValue()));
    }

    @Test
    void registerWithInvalidDataReturnsFieldErrors() throws Exception {
        mockMvc.perform(withJson(post("/auth/register"), Map.of("name", "", "email", "invalido", "password", "123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    void registerWithDuplicatedEmailReturnsConflict() throws Exception {
        TestUser existing = registerUser();

        mockMvc.perform(withJson(post("/auth/register"),
                        Map.of("name", "Outro", "email", existing.email(), "password", PASSWORD)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("E-mail já cadastrado"));
    }

    @Test
    void loginWithValidCredentialsReturnsToken() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(withJson(post("/auth/login"), Map.of("email", user.email(), "password", PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(notNullValue()))
                .andExpect(jsonPath("$.id").value(user.id()));
    }

    @Test
    void loginWithWrongPasswordReturnsUnauthorized() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(withJson(post("/auth/login"), Map.of("email", user.email(), "password", "senha-errada")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("E-mail ou senha inválidos"));
    }

    @Test
    void loginWithUnknownEmailReturnsSameErrorAsWrongPassword() throws Exception {
        mockMvc.perform(withJson(post("/auth/login"), Map.of("email", "naoexiste@test.com", "password", PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("E-mail ou senha inválidos"));
    }

    @Test
    void loginWithMissingFieldsReturnsBadRequest() throws Exception {
        mockMvc.perform(withJson(post("/auth/login"), Map.of("email", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    void malformedJsonReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/auth/login").contentType("application/json").content("{nao-e-json"))
                .andExpect(status().isBadRequest());
    }
}
