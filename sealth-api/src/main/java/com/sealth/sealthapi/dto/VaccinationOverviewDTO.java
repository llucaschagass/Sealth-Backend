package com.sealth.sealthapi.dto;

import java.util.List;

/** `ageYears` vem da medição de saúde mais recente; é nulo enquanto o usuário não tiver nenhuma. */
public record VaccinationOverviewDTO(Integer ageYears, List<VaccineStatusDTO> vaccines) {
}
