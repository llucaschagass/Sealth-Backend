package com.sealth.sealthapi.repositories;

import com.sealth.sealthapi.domain.appointment.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByOwnerIdOrderByDateAsc(String ownerId);

    Optional<Appointment> findByIdAndOwnerId(Long id, String ownerId);
}
