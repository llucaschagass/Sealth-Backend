package com.sealth.sealthapi.repositories;

import com.sealth.sealthapi.domain.health.HealthRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long> {
    List<HealthRecord> findByOwnerIdOrderByMeasuredAtDescIdDesc(String ownerId);

    Optional<HealthRecord> findFirstByOwnerIdOrderByMeasuredAtDescIdDesc(String ownerId);

    Optional<HealthRecord> findByIdAndOwnerId(Long id, String ownerId);
}
