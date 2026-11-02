package com.sealth.sealthapi.controllers;

import com.sealth.sealthapi.IntegrationTestBase;
import com.sealth.sealthapi.domain.user.PasswordResetToken;
import com.sealth.sealthapi.infra.mail.PasswordResetNotifier;
import com.sealth.sealthapi.repositories.PasswordResetTokenRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Recuperação de senha: pedido do link e definição da nova senha. */
class PasswordResetTest extends IntegrationTestBase {

    private static final String LINK_PREFIX = "http://localhost:4200/reset-password?token=";
    private static final String NEW_PASSWORD = "nova-senha-123";

    @MockBean
    private PasswordResetNotifier notifier;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    /** Pede o link e devolve o token enviado ao usuário. */
    private String requestToken(String email) throws Exception {
        reset(notifier);
        mockMvc.perform(withJson(post("/auth/forgot-password"), Map.of("email", email)))
                .andExpect(status().isAccepted());
        ArgumentCaptor<String> link = ArgumentCaptor.forClass(String.class);
        verify(notifier).sendResetLink(any(), link.capture());
        assertTrue(link.getValue().startsWith(LINK_PREFIX), link.getValue());
        return link.getValue().substring(LINK_PREFIX.length());
    }

    private void resetPassword(String token, String password, int expectedStatus) throws Exception {
        mockMvc.perform(withJson(post("/auth/reset-password"), Map.of("token", token, "password", password)))
                .andExpect(status().is(expectedStatus));
    }

    private void login(String email, String password, int expectedStatus) throws Exception {
        mockMvc.perform(withJson(post("/auth/login"), Map.of("email", email, "password", password)))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void forgotPasswordSendsALinkToAKnownUser() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(withJson(post("/auth/forgot-password"), Map.of("email", user.email())))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value(
                        "Se o e-mail estiver cadastrado, você receberá um link para redefinir a senha."));

        verify(notifier).sendResetLink(any(), anyString());
    }

    @Test
    void forgotPasswordAnswersTheSameWayForUnknownEmailsAndSendsNothing() throws Exception {
        reset(notifier);

        mockMvc.perform(withJson(post("/auth/forgot-password"), Map.of("email", "ninguem-" + System.nanoTime() + "@test.com")))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value(
                        "Se o e-mail estiver cadastrado, você receberá um link para redefinir a senha."));

        verify(notifier, never()).sendResetLink(any(), anyString());
    }

    @Test
    void forgotPasswordValidatesTheEmail() throws Exception {
        mockMvc.perform(withJson(post("/auth/forgot-password"), Map.of("email", "invalido")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
        mockMvc.perform(withJson(post("/auth/forgot-password"), Map.of()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    void theLinkSetsANewPasswordAndTheOldOneStopsWorking() throws Exception {
        TestUser user = registerUser();
        String token = requestToken(user.email());

        mockMvc.perform(withJson(post("/auth/reset-password"), Map.of("token", token, "password", NEW_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Senha redefinida com sucesso."));

        login(user.email(), NEW_PASSWORD, 200);
        login(user.email(), PASSWORD, 401);
    }

    @Test
    void theTokenWorksOnlyOnce() throws Exception {
        TestUser user = registerUser();
        String token = requestToken(user.email());
        resetPassword(token, NEW_PASSWORD, 200);

        mockMvc.perform(withJson(post("/auth/reset-password"), Map.of("token", token, "password", "outra-senha-1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Link inválido ou expirado"));

        login(user.email(), NEW_PASSWORD, 200);
    }

    @Test
    void aNewRequestInvalidatesThePreviousLink() throws Exception {
        TestUser user = registerUser();
        String first = requestToken(user.email());
        String second = requestToken(user.email());

        resetPassword(first, NEW_PASSWORD, 400);
        resetPassword(second, NEW_PASSWORD, 200);
    }

    @Test
    void usingALinkInvalidatesTheOtherPendingOnes() throws Exception {
        TestUser user = registerUser();
        String token = requestToken(user.email());
        // outro pedido pendente criado diretamente, como se tivesse sobrado de antes
        PasswordResetToken extra = new PasswordResetToken();
        extra.setUser(tokenRepository.findByUserIdAndUsedAtIsNull(user.id()).get(0).getUser());
        extra.setTokenHash("hash-extra-" + System.nanoTime());
        extra.setExpiresAt(Instant.now().plus(Duration.ofMinutes(10)));
        tokenRepository.save(extra);

        resetPassword(token, NEW_PASSWORD, 200);

        assertTrue(tokenRepository.findByUserIdAndUsedAtIsNull(user.id()).isEmpty());
    }

    @Test
    void anExpiredLinkIsRejectedAndKeepsThePassword() throws Exception {
        TestUser user = registerUser();
        String token = requestToken(user.email());
        PasswordResetToken entity = tokenRepository.findByUserIdAndUsedAtIsNull(user.id()).get(0);
        entity.setExpiresAt(Instant.now().minusSeconds(1));
        tokenRepository.save(entity);

        mockMvc.perform(withJson(post("/auth/reset-password"), Map.of("token", token, "password", NEW_PASSWORD)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Link inválido ou expirado"));

        login(user.email(), PASSWORD, 200);
    }

    @Test
    void anUnknownTokenIsRejected() throws Exception {
        mockMvc.perform(withJson(post("/auth/reset-password"), Map.of("token", "token-que-nao-existe", "password", NEW_PASSWORD)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Link inválido ou expirado"));
    }

    @Test
    void resetValidatesTokenAndPassword() throws Exception {
        mockMvc.perform(withJson(post("/auth/reset-password"), Map.of("token", "abc", "password", "123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.password").value("A senha deve ter no mínimo 6 caracteres"));
        mockMvc.perform(withJson(post("/auth/reset-password"), Map.of()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.token").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    void aTooShortPasswordDoesNotConsumeTheLink() throws Exception {
        TestUser user = registerUser();
        String token = requestToken(user.email());

        resetPassword(token, "123", 400);

        resetPassword(token, NEW_PASSWORD, 200);
    }

    @Test
    void onlyTheHashOfTheTokenIsStored() throws Exception {
        TestUser user = registerUser();
        String token = requestToken(user.email());

        assertTrue(tokenRepository.findByTokenHash(token).isEmpty());
        PasswordResetToken stored = tokenRepository.findByUserIdAndUsedAtIsNull(user.id()).get(0);
        assertFalse(stored.getTokenHash().contains(token));
        assertEquals(64, stored.getTokenHash().length());
        assertTrue(stored.getExpiresAt().isAfter(Instant.now().plus(Duration.ofMinutes(29))));
        assertTrue(stored.getExpiresAt().isBefore(Instant.now().plus(Duration.ofMinutes(31))));
    }

    @Test
    void bothEndpointsArePublic() throws Exception {
        // sem token JWT nenhum dos dois pode responder 401/403
        mockMvc.perform(withJson(post("/auth/forgot-password"), Map.of("email", "x@test.com")))
                .andExpect(status().isAccepted());
        mockMvc.perform(withJson(post("/auth/reset-password"), Map.of("token", "x", "password", NEW_PASSWORD)))
                .andExpect(status().isBadRequest());
    }
}
