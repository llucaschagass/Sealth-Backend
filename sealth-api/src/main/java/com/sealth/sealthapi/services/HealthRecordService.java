package com.sealth.sealthapi.services;

import com.sealth.sealthapi.domain.health.HealthRecord;
import com.sealth.sealthapi.domain.user.User;
import com.sealth.sealthapi.dto.CreateHealthRecordRequestDTO;
import com.sealth.sealthapi.dto.HealthRecordResponseDTO;
import com.sealth.sealthapi.infra.exception.ResourceNotFoundException;
import com.sealth.sealthapi.repositories.HealthRecordRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class HealthRecordService {

    private final HealthRecordRepository repository;

    public HealthRecordService(HealthRecordRepository repository) {
        this.repository = repository;
    }

    public HealthRecordResponseDTO create(User owner, CreateHealthRecordRequestDTO request) {
        HealthRecord record = new HealthRecord();
        record.setOwner(owner);
        record.setMeasuredAt(Instant.now());
        record.setWeightKg(request.weightKg());
        record.setHeightCm(request.heightCm());
        record.setAge(request.age());
        record.setSex(request.sex());
        record.setSystolic(request.systolic());
        record.setDiastolic(request.diastolic());
        return HealthRecordResponseDTO.from(repository.save(record));
    }

    /** Medições do usuário, da mais recente para a mais antiga. */
    public List<HealthRecordResponseDTO> list(User owner) {
        return repository.findByOwnerIdOrderByMeasuredAtDescIdDesc(owner.getId()).stream()
                .map(HealthRecordResponseDTO::from)
                .toList();
    }

    public HealthRecordResponseDTO latest(User owner) {
        return repository.findFirstByOwnerIdOrderByMeasuredAtDescIdDesc(owner.getId())
                .map(HealthRecordResponseDTO::from)
                .orElseThrow(() -> new ResourceNotFoundException("Nenhuma medição registrada"));
    }

    public void delete(User owner, Long id) {
        HealthRecord record = repository.findByIdAndOwnerId(id, owner.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Medição não encontrada"));
        repository.delete(record);
    }
}
