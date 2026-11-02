package com.sealth.sealthapi.dto;

import com.sealth.sealthapi.domain.vaccine.VaccinationRecord;
import com.sealth.sealthapi.domain.vaccine.VaccineCatalog;

import java.time.LocalDate;

public record VaccinationRecordResponseDTO(Long id, String vaccineCode, String vaccineName, LocalDate takenAt) {

    public static VaccinationRecordResponseDTO from(VaccinationRecord record) {
        String name = VaccineCatalog.find(record.getVaccineCode())
                .map(vaccine -> vaccine.name())
                .orElse(record.getVaccineCode());
        return new VaccinationRecordResponseDTO(record.getId(), record.getVaccineCode(), name, record.getTakenAt());
    }
}
