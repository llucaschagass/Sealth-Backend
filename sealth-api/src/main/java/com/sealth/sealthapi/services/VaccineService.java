package com.sealth.sealthapi.services;

import com.sealth.sealthapi.domain.health.HealthRecord;
import com.sealth.sealthapi.domain.user.User;
import com.sealth.sealthapi.domain.vaccine.VaccinationRecord;
import com.sealth.sealthapi.domain.vaccine.VaccineCatalog;
import com.sealth.sealthapi.domain.vaccine.VaccineDefinition;
import com.sealth.sealthapi.domain.vaccine.VaccineStatus;
import com.sealth.sealthapi.dto.VaccinationOverviewDTO;
import com.sealth.sealthapi.dto.VaccinationRecordRequestDTO;
import com.sealth.sealthapi.dto.VaccinationRecordResponseDTO;
import com.sealth.sealthapi.dto.VaccineStatusDTO;
import com.sealth.sealthapi.infra.exception.ResourceNotFoundException;
import com.sealth.sealthapi.repositories.HealthRecordRepository;
import com.sealth.sealthapi.repositories.VaccinationRecordRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VaccineService {

    /** Antecedência, em dias, com que um reforço passa a ser avisado. */
    static final int SOON_DAYS = 30;

    private final VaccinationRecordRepository recordRepository;
    private final HealthRecordRepository healthRecordRepository;

    public VaccineService(VaccinationRecordRepository recordRepository, HealthRecordRepository healthRecordRepository) {
        this.recordRepository = recordRepository;
        this.healthRecordRepository = healthRecordRepository;
    }

    public VaccinationRecordResponseDTO addRecord(User owner, VaccinationRecordRequestDTO request) {
        VaccinationRecord record = new VaccinationRecord();
        record.setOwner(owner);
        record.setVaccineCode(request.vaccineCode());
        record.setTakenAt(request.takenAt());
        return VaccinationRecordResponseDTO.from(recordRepository.save(record));
    }

    /** Doses registradas, da mais recente para a mais antiga. */
    public List<VaccinationRecordResponseDTO> listRecords(User owner) {
        return recordRepository.findByOwnerIdOrderByTakenAtDescIdDesc(owner.getId()).stream()
                .map(VaccinationRecordResponseDTO::from)
                .toList();
    }

    public void deleteRecord(User owner, Long id) {
        VaccinationRecord record = recordRepository.findByIdAndOwnerId(id, owner.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Dose não encontrada"));
        recordRepository.delete(record);
    }

    /** Calendário completo, na ordem do catálogo, com a situação de cada vacina para o usuário. */
    public VaccinationOverviewDTO overview(User owner) {
        return new VaccinationOverviewDTO(currentAge(owner), statuses(owner, LocalDate.now()));
    }

    /** Somente o que pede atenção, do mais urgente (reforço vencido) ao menos urgente. */
    public List<VaccineStatusDTO> alerts(User owner) {
        return statuses(owner, LocalDate.now()).stream()
                .filter(status -> status.status().isAlert())
                .sorted(Comparator.comparing(VaccineStatusDTO::status))
                .toList();
    }

    private List<VaccineStatusDTO> statuses(User owner, LocalDate today) {
        Integer age = currentAge(owner);
        Map<String, List<LocalDate>> dosesByVaccine = recordRepository
                .findByOwnerIdOrderByTakenAtDescIdDesc(owner.getId()).stream()
                .collect(Collectors.groupingBy(VaccinationRecord::getVaccineCode,
                        Collectors.mapping(VaccinationRecord::getTakenAt, Collectors.toList())));
        return VaccineCatalog.ALL.stream()
                .map(vaccine -> evaluate(vaccine, dosesByVaccine.getOrDefault(vaccine.code(), List.of()), age, today))
                .toList();
    }

    private Integer currentAge(User owner) {
        return healthRecordRepository.findFirstByOwnerIdOrderByMeasuredAtDescIdDesc(owner.getId())
                .map(HealthRecord::getAge)
                .orElse(null);
    }

    /** Regra de negócio isolada (sem acesso a banco) para ser testada com datas fixas. */
    static VaccineStatusDTO evaluate(VaccineDefinition vaccine, List<LocalDate> doses, Integer ageYears, LocalDate today) {
        LocalDate lastDose = doses.stream().max(LocalDate::compareTo).orElse(null);
        LocalDate nextDue = null;
        VaccineStatus status;

        if (doses.isEmpty() && !vaccine.appliesTo(ageYears)) {
            status = VaccineStatus.NOT_APPLICABLE;
        } else if (doses.size() < vaccine.totalDoses()) {
            status = VaccineStatus.PENDING;
        } else if (vaccine.boosterIntervalMonths() == null) {
            status = VaccineStatus.UP_TO_DATE;
        } else {
            nextDue = lastDose.plusMonths(vaccine.boosterIntervalMonths());
            if (!today.isBefore(nextDue)) {
                status = VaccineStatus.DUE;
            } else if (!today.isBefore(nextDue.minusDays(SOON_DAYS))) {
                status = VaccineStatus.SOON;
            } else {
                status = VaccineStatus.UP_TO_DATE;
            }
        }

        return new VaccineStatusDTO(vaccine.code(), vaccine.name(), vaccine.description(), vaccine.totalDoses(),
                doses.size(), lastDose, nextDue, status, vaccine.notice());
    }
}
