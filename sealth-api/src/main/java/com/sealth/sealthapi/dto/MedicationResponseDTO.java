package com.sealth.sealthapi.dto;

import com.sealth.sealthapi.domain.medication.Medication;

import java.time.LocalDate;
import java.util.List;

/** `active` é falso quando o tratamento já terminou (data de término anterior a hoje). */
public record MedicationResponseDTO(
        Long id,
        String name,
        String dosage,
        List<String> times,
        LocalDate startDate,
        LocalDate endDate,
        String notes,
        boolean active) {

    public static MedicationResponseDTO from(Medication medication, LocalDate today) {
        LocalDate end = medication.getEndDate();
        return new MedicationResponseDTO(
                medication.getId(),
                medication.getName(),
                medication.getDosage(),
                medication.getTimes(),
                medication.getStartDate(),
                end,
                medication.getNotes(),
                end == null || !end.isBefore(today)
        );
    }
}
