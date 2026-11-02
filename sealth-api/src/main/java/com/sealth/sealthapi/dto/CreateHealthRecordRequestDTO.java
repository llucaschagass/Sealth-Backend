package com.sealth.sealthapi.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateHealthRecordRequestDTO(
        @NotNull(message = "O peso é obrigatório")
        @DecimalMin(value = "20.0", message = "O peso deve estar entre 20 e 400 kg")
        @DecimalMax(value = "400.0", message = "O peso deve estar entre 20 e 400 kg")
        Double weightKg,

        @NotNull(message = "A altura é obrigatória")
        @DecimalMin(value = "50.0", message = "A altura deve estar entre 50 e 250 cm")
        @DecimalMax(value = "250.0", message = "A altura deve estar entre 50 e 250 cm")
        Double heightCm,

        @NotNull(message = "A idade é obrigatória")
        @Min(value = 1, message = "A idade deve estar entre 1 e 120 anos")
        @Max(value = 120, message = "A idade deve estar entre 1 e 120 anos")
        Integer age,

        @NotNull(message = "O sexo é obrigatório")
        @Pattern(regexp = "masculino|feminino", message = "O sexo deve ser \"masculino\" ou \"feminino\"")
        String sex,

        @Min(value = 50, message = "A pressão sistólica deve estar entre 50 e 300 mmHg")
        @Max(value = 300, message = "A pressão sistólica deve estar entre 50 e 300 mmHg")
        Integer systolic,

        @Min(value = 30, message = "A pressão diastólica deve estar entre 30 e 200 mmHg")
        @Max(value = 200, message = "A pressão diastólica deve estar entre 30 e 200 mmHg")
        Integer diastolic) {

    /** A pressão arterial só faz sentido completa (as duas medidas) e com sistólica maior que diastólica. */
    @AssertTrue(message = "Informe as pressões sistólica e diastólica juntas, com a sistólica maior que a diastólica")
    public boolean isBloodPressure() {
        if (systolic == null && diastolic == null) {
            return true;
        }
        return systolic != null && diastolic != null && systolic > diastolic;
    }
}
