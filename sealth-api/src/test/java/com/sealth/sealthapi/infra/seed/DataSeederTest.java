package com.sealth.sealthapi.infra.seed;

import com.sealth.sealthapi.IntegrationTestBase;
import com.sealth.sealthapi.domain.user.User;
import com.sealth.sealthapi.repositories.AppointmentRepository;
import com.sealth.sealthapi.repositories.HealthRecordRepository;
import com.sealth.sealthapi.repositories.MedicationRepository;
import com.sealth.sealthapi.repositories.VaccinationRecordRepository;
import com.sealth.sealthapi.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * O contexto de teste desliga o seed; aqui o executamos manualmente (com rollback) para validar seu conteúdo.
 */
@Transactional
class DataSeederTest extends IntegrationTestBase {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private HealthRecordRepository healthRecordRepository;
    @Autowired
    private MedicationRepository medicationRepository;
    @Autowired
    private VaccinationRecordRepository vaccinationRecordRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private DataSeeder seeder() {
        return new DataSeeder(userRepository, appointmentRepository, healthRecordRepository, medicationRepository,
                vaccinationRecordRepository, passwordEncoder);
    }

    @Test
    void seedCreatesDemoUsersWithWorkingPasswordAndAppointments() {
        seeder().run(null);

        User demo = userRepository.findByEmail(DataSeeder.DEMO_EMAIL).orElseThrow();
        assertTrue(passwordEncoder.matches(DataSeeder.DEMO_PASSWORD, demo.getPassword()));
        assertEquals(4, appointmentRepository.findByOwnerIdOrderByDateAsc(demo.getId()).size());

        User maria = userRepository.findByEmail(DataSeeder.SECOND_EMAIL).orElseThrow();
        assertEquals(1, appointmentRepository.findByOwnerIdOrderByDateAsc(maria.getId()).size());
    }

    @Test
    void seedHasBothPastAndFutureAppointments() {
        seeder().run(null);

        User demo = userRepository.findByEmail(DataSeeder.DEMO_EMAIL).orElseThrow();
        var dates = appointmentRepository.findByOwnerIdOrderByDateAsc(demo.getId()).stream().map(a -> a.getDate()).toList();
        assertTrue(dates.stream().anyMatch(d -> d.isBefore(LocalDate.now())));
        assertTrue(dates.stream().anyMatch(d -> d.isAfter(LocalDate.now())));
    }

    @Test
    void seedGivesUpcomingDemoAppointmentsATimeAndOneActiveReminder() {
        seeder().run(null);

        User demo = userRepository.findByEmail(DataSeeder.DEMO_EMAIL).orElseThrow();
        var appointments = appointmentRepository.findByOwnerIdOrderByDateAsc(demo.getId());
        assertTrue(appointments.stream().anyMatch(a -> a.getTime() != null && a.getReminderMinutes() != null));
        var now = java.time.LocalDateTime.now();
        assertTrue(appointments.stream().anyMatch(a -> a.getReminderAt() != null
                && !now.isBefore(a.getReminderAt()) && now.isBefore(a.getStartsAt())));
    }

    @Test
    void seedGivesTheDemoUserAHealthHistoryAndOthersNone() {
        seeder().run(null);

        User demo = userRepository.findByEmail(DataSeeder.DEMO_EMAIL).orElseThrow();
        var records = healthRecordRepository.findByOwnerIdOrderByMeasuredAtDescIdDesc(demo.getId());
        assertEquals(3, records.size());
        assertEquals(74.8, records.get(0).getWeightKg());
        assertEquals(78.5, records.get(2).getWeightKg());

        User maria = userRepository.findByEmail(DataSeeder.SECOND_EMAIL).orElseThrow();
        assertTrue(healthRecordRepository.findByOwnerIdOrderByMeasuredAtDescIdDesc(maria.getId()).isEmpty());
    }

    @Test
    void seedGivesTheDemoUserMedicationsIncludingAFinishedOne() {
        seeder().run(null);

        User demo = userRepository.findByEmail(DataSeeder.DEMO_EMAIL).orElseThrow();
        var medications = medicationRepository.findByOwnerIdOrderByNameAsc(demo.getId());
        assertEquals(3, medications.size());
        assertTrue(medications.stream().anyMatch(m -> m.getEndDate() != null && m.getEndDate().isBefore(LocalDate.now())));
        assertTrue(medications.stream().allMatch(m -> !m.getTimes().isEmpty()));

        User maria = userRepository.findByEmail(DataSeeder.SECOND_EMAIL).orElseThrow();
        assertTrue(medicationRepository.findByOwnerIdOrderByNameAsc(maria.getId()).isEmpty());
    }

    @Test
    void seedGivesTheDemoUserVaccinationRecordsAndOthersNone() {
        seeder().run(null);

        User demo = userRepository.findByEmail(DataSeeder.DEMO_EMAIL).orElseThrow();
        var records = vaccinationRecordRepository.findByOwnerIdOrderByTakenAtDescIdDesc(demo.getId());
        assertEquals(7, records.size());
        assertTrue(records.stream().noneMatch(r -> r.getTakenAt().isAfter(LocalDate.now())));

        User maria = userRepository.findByEmail(DataSeeder.SECOND_EMAIL).orElseThrow();
        assertTrue(vaccinationRecordRepository.findByOwnerIdOrderByTakenAtDescIdDesc(maria.getId()).isEmpty());
    }

    @Test
    void runningSeedTwiceDoesNotDuplicateData() {
        seeder().run(null);
        long users = userRepository.count();
        long appointments = appointmentRepository.count();
        long healthRecords = healthRecordRepository.count();
        long medications = medicationRepository.count();
        long vaccinations = vaccinationRecordRepository.count();

        seeder().run(null);

        assertEquals(users, userRepository.count());
        assertEquals(appointments, appointmentRepository.count());
        assertEquals(healthRecords, healthRecordRepository.count());
        assertEquals(medications, medicationRepository.count());
        assertEquals(vaccinations, vaccinationRecordRepository.count());
        assertFalse(userRepository.findByEmail(DataSeeder.DEMO_EMAIL).isEmpty());
    }
}
