package com.sealth.sealthapi.repositories;

import com.sealth.sealthapi.domain.vaccine.VaccinationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VaccinationRecordRepository extends JpaRepository<VaccinationRecord, Long> {
    List<VaccinationRecord> findByOwnerIdOrderByTakenAtDescIdDesc(String ownerId);

    Optional<VaccinationRecord> findByIdAndOwnerId(Long id, String ownerId);
}
