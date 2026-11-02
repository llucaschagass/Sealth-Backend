package com.sealth.sealthapi.infra.mail;

import com.sealth.sealthapi.domain.user.User;

/** Entrega ao usuário o link de redefinição de senha (e-mail, SMS etc.). */
public interface PasswordResetNotifier {
    void sendResetLink(User user, String link);
}
