package com.sealth.sealthapi.services;

import com.sealth.sealthapi.domain.user.PasswordResetToken;
import com.sealth.sealthapi.domain.user.User;
import com.sealth.sealthapi.infra.exception.BadRequestException;
import com.sealth.sealthapi.infra.mail.PasswordResetNotifier;
import com.sealth.sealthapi.repositories.PasswordResetTokenRepository;
import com.sealth.sealthapi.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class PasswordResetService {

    static final String INVALID_TOKEN_MESSAGE = "Link inválido ou expirado";

    private static final int TOKEN_BYTES = 32;

    private final SecureRandom random = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetNotifier notifier;
    private final String frontendUrl;
    private final Duration expiration;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder,
                                PasswordResetNotifier notifier,
                                @Value("${app.frontend-url}") String frontendUrl,
                                @Value("${app.password-reset.expiration-minutes}") long expirationMinutes) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.notifier = notifier;
        this.frontendUrl = frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1) : frontendUrl;
        this.expiration = Duration.ofMinutes(expirationMinutes);
    }

    /**
     * Gera um link de redefinição e o entrega ao usuário. Para e-mails desconhecidos não faz nada, e quem
     * chama responde da mesma forma, para não revelar quais e-mails têm conta.
     */
    @Transactional
    public void requestReset(String email) {
        userRepository.findByEmail(email.trim()).ifPresent(user -> {
            Instant now = Instant.now();
            // só o pedido mais recente vale
            tokenRepository.findByUserIdAndUsedAtIsNull(user.getId()).forEach(old -> old.setUsedAt(now));

            byte[] bytes = new byte[TOKEN_BYTES];
            random.nextBytes(bytes);
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

            PasswordResetToken entity = new PasswordResetToken();
            entity.setUser(user);
            entity.setTokenHash(hash(token));
            entity.setExpiresAt(now.plus(expiration));
            tokenRepository.save(entity);

            notifier.sendResetLink(user, frontendUrl + "/reset-password?token=" + token);
        });
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        Instant now = Instant.now();
        PasswordResetToken entity = tokenRepository.findByTokenHash(hash(token.trim()))
                .filter(found -> found.isUsable(now))
                .orElseThrow(() -> new BadRequestException(INVALID_TOKEN_MESSAGE));

        User user = entity.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        entity.setUsedAt(now);
        tokenRepository.findByUserIdAndUsedAtIsNull(user.getId()).forEach(other -> other.setUsedAt(now));
    }

    static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }
}
