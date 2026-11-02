package com.sealth.sealthapi.domain.vaccine;

/**
 * Vacina do calendário de referência.
 *
 * @param totalDoses            doses necessárias para completar o esquema básico
 * @param minAgeYears           idade (em anos) a partir da qual é recomendada; nulo = sem limite inferior
 * @param maxAgeYears           idade (em anos) até a qual é recomendada; nulo = sem limite superior
 * @param boosterIntervalMonths meses entre a última dose e o reforço; nulo = sem reforço
 * @param notice                observação informativa (campanhas, áreas de risco); opcional
 */
public record VaccineDefinition(
        String code,
        String name,
        String description,
        int totalDoses,
        Integer minAgeYears,
        Integer maxAgeYears,
        Integer boosterIntervalMonths,
        String notice) {

    /** Sem idade conhecida, considera-se que a vacina se aplica. */
    public boolean appliesTo(Integer ageYears) {
        if (ageYears == null) {
            return true;
        }
        return (minAgeYears == null || ageYears >= minAgeYears)
                && (maxAgeYears == null || ageYears <= maxAgeYears);
    }
}
