package com.sealth.sealthapi.domain.vaccine;

/** Situação de uma vacina para o usuário, da mais para a menos urgente. */
public enum VaccineStatus {
    /** Reforço já vencido. */
    DUE,
    /** Esquema ainda incompleto para a idade. */
    PENDING,
    /** Reforço previsto para os próximos 30 dias. */
    SOON,
    UP_TO_DATE,
    /** Fora da faixa de idade recomendada e sem doses registradas. */
    NOT_APPLICABLE;

    public boolean isAlert() {
        return this == DUE || this == PENDING || this == SOON;
    }
}
