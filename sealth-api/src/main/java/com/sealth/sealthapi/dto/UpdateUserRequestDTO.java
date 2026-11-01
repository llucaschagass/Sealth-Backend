package com.sealth.sealthapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

/** Todos os campos são opcionais: vazios ou ausentes mantêm o valor atual. */
public record UpdateUserRequestDTO(
        @Email(message = "E-mail inválido") String email,
        String name,
        @Pattern(regexp = "^$|.{6,}", message = "A senha deve ter no mínimo 6 caracteres") String password) {
}
