package com.sealth.sealthapi.dto;

import com.sealth.sealthapi.domain.vaccine.VaccineCatalog;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record VaccinationRecordRequestDTO(
        @NotBlank(message = "A vacina é obrigatória")
        String vaccineCode,

        @NotNull(message = "A data da dose é obrigatória")
        @PastOrPresent(message = "A data da dose não pode ser futura")
        LocalDate takenAt) {

    @AssertTrue(message = "Vacina desconhecida")
    public boolean isKnownVaccine() {
        return vaccineCode == null || vaccineCode.isBlank() || VaccineCatalog.find(vaccineCode).isPresent();
    }
}
