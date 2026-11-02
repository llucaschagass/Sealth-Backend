package com.sealth.sealthapi.infra.mail;

import com.sealth.sealthapi.domain.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Implementação de desenvolvimento: não há servidor de e-mail configurado, então o link é escrito no log
 * da aplicação. Para enviar e-mails de verdade, basta trocar por outra implementação de {@link PasswordResetNotifier}.
 */
@Component
public class LoggingPasswordResetNotifier implements PasswordResetNotifier {

    private static final Logger log = LoggerFactory.getLogger(LoggingPasswordResetNotifier.class);

    @Override
    public void sendResetLink(User user, String link) {
        log.info("Link de redefinição de senha para {}: {}", user.getEmail(), link);
    }
}
