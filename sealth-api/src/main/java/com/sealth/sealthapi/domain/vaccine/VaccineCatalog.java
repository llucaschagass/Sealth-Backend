package com.sealth.sealthapi.domain.vaccine;

import java.util.List;
import java.util.Optional;

/**
 * Calendário de referência (resumo informativo, inspirado no calendário nacional de vacinação).
 * Não substitui a orientação de um profissional de saúde.
 */
public final class VaccineCatalog {

    public static final List<VaccineDefinition> ALL = List.of(
            new VaccineDefinition("hepatite-b", "Hepatite B",
                    "Protege contra a hepatite B. Esquema de 3 doses.",
                    3, null, null, null, null),
            new VaccineDefinition("dt", "Difteria e tétano (dT)",
                    "Esquema de 3 doses, com reforço a cada 10 anos.",
                    3, 7, null, 120, null),
            new VaccineDefinition("triplice-viral", "Tríplice viral (sarampo, caxumba e rubéola)",
                    "Esquema de 2 doses até os 29 anos.",
                    2, null, 29, null, "Mantenha o cartão em dia: o sarampo pode voltar a circular."),
            new VaccineDefinition("febre-amarela", "Febre amarela",
                    "Dose única.",
                    1, null, 59, null, "Indicada para quem vive ou viaja para áreas de risco."),
            new VaccineDefinition("influenza", "Gripe (influenza)",
                    "Dose anual.",
                    1, null, null, 12, "A campanha nacional costuma ocorrer no outono."),
            new VaccineDefinition("hpv", "HPV",
                    "Esquema de 2 doses entre 9 e 14 anos.",
                    2, 9, 14, null, null),
            new VaccineDefinition("pneumococica-23", "Pneumocócica 23-valente",
                    "Indicada a partir dos 60 anos.",
                    1, 60, null, null, null)
    );

    private VaccineCatalog() {
    }

    public static Optional<VaccineDefinition> find(String code) {
        return ALL.stream().filter(vaccine -> vaccine.code().equals(code)).findFirst();
    }
}
