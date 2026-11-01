package com.sealth.sealthapi.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateAppointmentRequestDTO(
        @NotNull(message = "A data é obrigatória") LocalDate date,
        @NotBlank(message = "O médico é obrigatório") String doctor,
        @NotBlank(message = "A especialidade é obrigatória") String specialty,
        String location,
        @JsonAlias("extraInformation") String extraInfo) {}
