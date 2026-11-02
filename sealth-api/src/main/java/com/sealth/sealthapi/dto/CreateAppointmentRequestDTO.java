package com.sealth.sealthapi.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * `time` (HH:mm) e `reminderMinutes` (antecedência do lembrete, em minutos) são opcionais;
 * o lembrete só faz sentido quando há horário.
 */
public record CreateAppointmentRequestDTO(
        @NotNull(message = "A data é obrigatória") LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime time,
        @NotBlank(message = "O médico é obrigatório") String doctor,
        @NotBlank(message = "A especialidade é obrigatória") String specialty,
        String location,
        @JsonAlias("extraInformation") String extraInfo,
        @Min(value = 0, message = "A antecedência do lembrete deve estar entre 0 minuto e 7 dias")
        @Max(value = 10080, message = "A antecedência do lembrete deve estar entre 0 minuto e 7 dias")
        Integer reminderMinutes) {

    @AssertTrue(message = "Informe o horário da consulta para configurar um lembrete")
    public boolean isReminder() {
        return reminderMinutes == null || time != null;
    }
}
