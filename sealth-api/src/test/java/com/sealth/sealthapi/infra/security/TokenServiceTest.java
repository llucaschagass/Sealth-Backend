package com.sealth.sealthapi.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.sealth.sealthapi.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenServiceTest {

    private static final String SECRET = "segredo-de-teste";

    private TokenService tokenService;
    private User user;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "secret", SECRET);
        user = new User();
        user.setId("user-123");
    }

    @Test
    void generatedTokenIsValidAndCarriesUserId() {
        String token = tokenService.generateToken(user);

        assertEquals("user-123", tokenService.validateToken(token));
    }

    @Test
    void tokenExpiresInAboutTwoHours() {
        String token = tokenService.generateToken(user);

        Instant expiresAt = JWT.decode(token).getExpiresAtAsInstant();
        Duration remaining = Duration.between(Instant.now(), expiresAt);
        assertTrue(remaining.compareTo(Duration.ofMinutes(119)) > 0 && remaining.compareTo(Duration.ofMinutes(121)) < 0,
                "expiração deveria ser ~2h, mas foi " + remaining);
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        String forged = JWT.create().withIssuer("sealth-api").withSubject("user-123")
                .withExpiresAt(Instant.now().plusSeconds(60)).sign(Algorithm.HMAC256("outro-segredo"));

        assertNull(tokenService.validateToken(forged));
    }

    @Test
    void tokenFromAnotherIssuerIsRejected() {
        String foreign = JWT.create().withIssuer("outro-emissor").withSubject("user-123")
                .withExpiresAt(Instant.now().plusSeconds(60)).sign(Algorithm.HMAC256(SECRET));

        assertNull(tokenService.validateToken(foreign));
    }

    @Test
    void garbageTokenIsRejected() {
        assertNull(tokenService.validateToken("nao-e-um-jwt"));
    }
}
