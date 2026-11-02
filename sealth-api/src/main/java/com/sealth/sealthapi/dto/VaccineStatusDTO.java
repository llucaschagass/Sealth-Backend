package com.sealth.sealthapi.dto;

import com.sealth.sealthapi.domain.vaccine.VaccineStatus;

import java.time.LocalDate;

/**
 * Situação de uma vacina do calendário para o usuário.
 * `nextDueDate` só existe para vacinas com reforço e esquema completo.
 */
public record VaccineStatusDTO(
        String code,
        String name,
        String description,
        int totalDoses,
        int dosesTaken,
        LocalDate lastDoseAt,
        LocalDate nextDueDate,
        VaccineStatus status,
        String notice) {
}
