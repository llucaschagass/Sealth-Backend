package com.sealth.sealthapi.dto;

import com.sealth.sealthapi.domain.health.HealthRecord;

import java.time.Instant;

public record HealthRecordResponseDTO(
        Long id,
        Instant measuredAt,
        double weightKg,
        double heightCm,
        int age,
        String sex,
        Integer systolic,
        Integer diastolic) {

    public static HealthRecordResponseDTO from(HealthRecord record) {
        return new HealthRecordResponseDTO(
                record.getId(),
                record.getMeasuredAt(),
                record.getWeightKg(),
                record.getHeightCm(),
                record.getAge(),
                record.getSex(),
                record.getSystolic(),
                record.getDiastolic()
        );
    }
}
