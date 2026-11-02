package com.sealth.sealthapi.services;

import com.sealth.sealthapi.domain.medication.Medication;
import com.sealth.sealthapi.domain.user.User;
import com.sealth.sealthapi.dto.MedicationRequestDTO;
import com.sealth.sealthapi.dto.MedicationResponseDTO;
import com.sealth.sealthapi.infra.exception.ResourceNotFoundException;
import com.sealth.sealthapi.repositories.MedicationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class MedicationService {

    private final MedicationRepository repository;

    public MedicationService(MedicationRepository repository) {
        this.repository = repository;
    }

    public MedicationResponseDTO create(User owner, MedicationRequestDTO request) {
        Medication medication = new Medication();
        medication.setOwner(owner);
        apply(medication, request);
        return toResponse(repository.save(medication));
    }

    /** Medicamentos em uso primeiro; dentro de cada grupo, em ordem alfabética. */
    public List<MedicationResponseDTO> list(User owner) {
        return repository.findByOwnerIdOrderByNameAsc(owner.getId()).stream()
                .map(this::toResponse)
                .sorted(Comparator.comparing(MedicationResponseDTO::active).reversed())
                .toList();
    }

    public MedicationResponseDTO update(User owner, Long id, MedicationRequestDTO request) {
        Medication medication = find(owner, id);
        apply(medication, request);
        return toResponse(repository.save(medication));
    }

    public void delete(User owner, Long id) {
        repository.delete(find(owner, id));
    }

    private Medication find(User owner, Long id) {
        return repository.findByIdAndOwnerId(id, owner.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento não encontrado"));
    }

    private void apply(Medication medication, MedicationRequestDTO request) {
        medication.setName(request.name().trim());
        medication.setDosage(request.dosage().trim());
        medication.setTimes(request.times().stream().distinct().sorted().toList());
        medication.setStartDate(request.startDate());
        medication.setEndDate(request.endDate());
        String notes = request.notes() == null ? "" : request.notes().trim();
        medication.setNotes(notes.isEmpty() ? null : notes);
    }

    private MedicationResponseDTO toResponse(Medication medication) {
        return MedicationResponseDTO.from(medication, LocalDate.now());
    }
}
