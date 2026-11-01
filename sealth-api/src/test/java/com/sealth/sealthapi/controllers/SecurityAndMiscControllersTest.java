package com.sealth.sealthapi.controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.sealth.sealthapi.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityAndMiscControllersTest extends IntegrationTestBase {

    @Value("${api.security.token.secret}")
    private String secret;

    @Test
    void protectedEndpointWithoutTokenReturnsJsonUnauthorized() throws Exception {
        mockMvc.perform(get("/appointments/all"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void invalidTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/appointments/all").header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authorizationHeaderWithoutBearerPrefixIsIgnored() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(get("/appointments/all").header("Authorization", user.token()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void expiredTokenReturnsUnauthorized() throws Exception {
        TestUser user = registerUser();
        String expired = JWT.create().withIssuer("sealth-api").withSubject(user.id())
                .withExpiresAt(Instant.now().minusSeconds(60)).sign(Algorithm.HMAC256(secret));

        mockMvc.perform(get("/appointments/all").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validTokenOfDeletedUserReturnsUnauthorizedInsteadOfServerError() throws Exception {
        String orphan = JWT.create().withIssuer("sealth-api").withSubject(UUID.randomUUID().toString())
                .withExpiresAt(Instant.now().plusSeconds(600)).sign(Algorithm.HMAC256(secret));

        mockMvc.perform(get("/appointments/all").header("Authorization", "Bearer " + orphan))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void corsPreflightFromAllowedOriginIsAccepted() throws Exception {
        mockMvc.perform(options("/appointments/create")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
    }

    @Test
    void corsPreflightFromUnknownOriginIsRejected() throws Exception {
        mockMvc.perform(options("/appointments/create")
                        .header("Origin", "http://malicioso.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }

    @Test
    void healthDefaultsRequireAuthenticationAndReturnValues() throws Exception {
        mockMvc.perform(get("/health/defaults")).andExpect(status().isUnauthorized());

        TestUser user = registerUser();
        mockMvc.perform(withAuth(get("/health/defaults"), user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weight").value(70.0))
                .andExpect(jsonPath("$.height").value(1.75))
                .andExpect(jsonPath("$.age").value(30))
                .andExpect(jsonPath("$.sex").value("masculino"));
    }

    @Test
    void medicinesRequireAuthenticationAndReturnList() throws Exception {
        mockMvc.perform(get("/medicines/pdf")).andExpect(status().isUnauthorized());

        TestUser user = registerUser();
        mockMvc.perform(withAuth(get("/medicines/pdf"), user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$", hasItem("Paracetamol")));
    }

    @Test
    void openApiDocumentationIsPublicAndDescribesTheApi() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Sealth API"))
                .andExpect(jsonPath("$.paths['/auth/login']").exists())
                .andExpect(jsonPath("$.paths['/appointments/create']").exists())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"));
    }
}
