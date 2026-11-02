package com.sealth.sealthapi.repositories;

import com.sealth.sealthapi.domain.medication.Medication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicationRepository extends JpaRepository<Medication, Long> {
    List<Medication> findByOwnerIdOrderByNameAsc(String ownerId);

    Optional<Medication> findByIdAndOwnerId(Long id, String ownerId);
}
