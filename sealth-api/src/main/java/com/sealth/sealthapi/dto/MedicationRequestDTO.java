package com.sealth.sealthapi.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/** Corpo de criação e de atualização (a atualização substitui todos os campos). */
public record MedicationRequestDTO(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String name,

        @NotBlank(message = "A dosagem é obrigatória")
        @Size(max = 100, message = "A dosagem deve ter no máximo 100 caracteres")
        String dosage,

        @NotEmpty(message = "Informe ao menos um horário")
        @Size(max = 12, message = "Informe no máximo 12 horários")
        List<@Pattern(regexp = "([01]\\d|2[0-3]):[0-5]\\d", message = "Os horários devem estar no formato HH:mm") String> times,

        LocalDate startDate,

        LocalDate endDate,

        @Size(max = 500, message = "As observações devem ter no máximo 500 caracteres")
        String notes) {

    @AssertTrue(message = "A data de término não pode ser anterior à data de início")
    public boolean isPeriod() {
        return startDate == null || endDate == null || !endDate.isBefore(startDate);
    }
}
