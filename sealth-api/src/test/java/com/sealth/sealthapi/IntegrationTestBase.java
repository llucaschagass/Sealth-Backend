package com.sealth.sealthapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Base dos testes de integração: sobe o contexto completo sem o seed
 * e oferece helpers para cadastrar usuários e montar requisições autenticadas.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.seed.enabled=false")
public abstract class IntegrationTestBase {

    protected static final String PASSWORD = "123456";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected record TestUser(String id, String email, String token) {}

    protected TestUser registerUser() throws Exception {
        String email = "user-" + UUID.randomUUID() + "@test.com";
        String body = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "Usuário Teste", "email", email, "password", PASSWORD))))
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(body);
        return new TestUser(node.get("id").asText(), email, node.get("token").asText());
    }

    protected String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    protected MockHttpServletRequestBuilder withAuth(MockHttpServletRequestBuilder request, TestUser user) {
        return request.header("Authorization", "Bearer " + user.token());
    }

    protected MockHttpServletRequestBuilder withJson(MockHttpServletRequestBuilder request, Object body) throws Exception {
        return request.contentType(MediaType.APPLICATION_JSON).content(json(body));
    }

    protected long createAppointment(TestUser user, String date, String doctor) throws Exception {
        ResultActions result = mockMvc.perform(withJson(withAuth(post("/appointments/create"), user),
                Map.of("date", date, "doctor", doctor, "specialty", "Cardiologia", "location", "Hospital", "extraInfo", "info")));
        return objectMapper.readTree(result.andReturn().getResponse().getContentAsString()).get("id").asLong();
    }
}
