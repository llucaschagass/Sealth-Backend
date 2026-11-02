package com.sealth.sealthapi.infra.seed;

import com.sealth.sealthapi.domain.appointment.Appointment;
import com.sealth.sealthapi.domain.health.HealthRecord;
import com.sealth.sealthapi.domain.medication.Medication;
import com.sealth.sealthapi.domain.user.User;
import com.sealth.sealthapi.domain.vaccine.VaccinationRecord;
import com.sealth.sealthapi.repositories.AppointmentRepository;
import com.sealth.sealthapi.repositories.HealthRecordRepository;
import com.sealth.sealthapi.repositories.MedicationRepository;
import com.sealth.sealthapi.repositories.VaccinationRecordRepository;
import com.sealth.sealthapi.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Popula o banco em memória com dados de demonstração a cada inicialização.
 * Desative com app.seed.enabled=false.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    static final String DEMO_EMAIL = "demo@sealth.com";
    static final String DEMO_PASSWORD = "123456";
    static final String SECOND_EMAIL = "maria@sealth.com";

    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final HealthRecordRepository healthRecordRepository;
    private final MedicationRepository medicationRepository;
    private final VaccinationRecordRepository vaccinationRecordRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, AppointmentRepository appointmentRepository,
                      HealthRecordRepository healthRecordRepository, MedicationRepository medicationRepository,
                      VaccinationRecordRepository vaccinationRecordRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.healthRecordRepository = healthRecordRepository;
        this.medicationRepository = medicationRepository;
        this.vaccinationRecordRepository = vaccinationRecordRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail(DEMO_EMAIL)) {
            return;
        }

        LocalDate today = LocalDate.now();

        User demo = createUser("Usuário Demo", DEMO_EMAIL);
        createAppointment(demo, today.minusDays(30), LocalTime.of(9, 0), null, "Dr. Silva", "Cardiologia", "Hospital ABC - Contagem", "Retorno de rotina");
        createAppointment(demo, today.plusDays(3), LocalTime.of(14, 30), 10080, "Dra. Souza", "Ortopedia", "Hospital XYZ - Betim", "Dores nas costas");
        createAppointment(demo, today.plusDays(15), LocalTime.of(8, 0), 60, "Dr. Almeida", "Dermatologia", "Clínica Bem Estar - BH", "Levar exames anteriores");
        createAppointment(demo, today.plusDays(45), null, null, "Dra. Lima", "Clínico Geral", "UBS Centro", null);

        Instant now = Instant.now();
        createHealthRecord(demo, now.minus(60, ChronoUnit.DAYS), 78.5, 175, 30, "masculino", null, null);
        createHealthRecord(demo, now.minus(30, ChronoUnit.DAYS), 76.2, 175, 30, "masculino", 128, 84);
        createHealthRecord(demo, now.minus(2, ChronoUnit.DAYS), 74.8, 175, 30, "masculino", 120, 80);

        createMedication(demo, "Losartana", "50 mg", List.of("08:00"), today.minusDays(90), null, "Tomar em jejum");
        createMedication(demo, "Metformina", "850 mg", List.of("08:00", "20:00"), today.minusDays(40), null, "Junto às refeições");
        createMedication(demo, "Amoxicilina", "500 mg", List.of("06:00", "14:00", "22:00"), today.minusDays(20), today.minusDays(13), null);

        // com 30 anos: dT com reforço vencido, febre amarela pendente e gripe com reforço previsto para os próximos dias
        createVaccination(demo, "hepatite-b", today.minusYears(12));
        createVaccination(demo, "hepatite-b", today.minusYears(12).plusMonths(1));
        createVaccination(demo, "hepatite-b", today.minusYears(12).plusMonths(6));
        createVaccination(demo, "dt", today.minusYears(15));
        createVaccination(demo, "dt", today.minusYears(15).plusMonths(2));
        createVaccination(demo, "dt", today.minusYears(11));
        createVaccination(demo, "influenza", today.minusMonths(12).plusDays(10));

        User maria = createUser("Maria Souza", SECOND_EMAIL);
        createAppointment(maria, today.plusDays(7), null, null, "Dr. Costa", "Oftalmologia", "Clínica Visão - Contagem", "Exame de vista");

        log.info("Seed carregado. Login de demonstração: {} / {} (segundo usuário: {} / {})",
                DEMO_EMAIL, DEMO_PASSWORD, SECOND_EMAIL, DEMO_PASSWORD);
    }

    private void createVaccination(User owner, String vaccineCode, LocalDate takenAt) {
        VaccinationRecord record = new VaccinationRecord();
        record.setOwner(owner);
        record.setVaccineCode(vaccineCode);
        record.setTakenAt(takenAt);
        vaccinationRecordRepository.save(record);
    }

    private void createMedication(User owner, String name, String dosage, List<String> times, LocalDate startDate,
                                  LocalDate endDate, String notes) {
        Medication medication = new Medication();
        medication.setOwner(owner);
        medication.setName(name);
        medication.setDosage(dosage);
        medication.setTimes(times);
        medication.setStartDate(startDate);
        medication.setEndDate(endDate);
        medication.setNotes(notes);
        medicationRepository.save(medication);
    }

    private void createHealthRecord(User owner, Instant measuredAt, double weightKg, double heightCm, int age,
                                    String sex, Integer systolic, Integer diastolic) {
        HealthRecord record = new HealthRecord();
        record.setOwner(owner);
        record.setMeasuredAt(measuredAt);
        record.setWeightKg(weightKg);
        record.setHeightCm(heightCm);
        record.setAge(age);
        record.setSex(sex);
        record.setSystolic(systolic);
        record.setDiastolic(diastolic);
        healthRecordRepository.save(record);
    }

    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(DEMO_PASSWORD));
        return userRepository.save(user);
    }

    private void createAppointment(User owner, LocalDate date, LocalTime time, Integer reminderMinutes, String doctor,
                                   String specialty, String location, String extraInfo) {
        Appointment appointment = new Appointment();
        appointment.setOwner(owner);
        appointment.setDate(date);
        appointment.setTime(time);
        appointment.setReminderMinutes(reminderMinutes);
        appointment.setDoctor(doctor);
        appointment.setSpecialty(specialty);
        appointment.setLocation(location);
        appointment.setExtraInfo(extraInfo);
        appointmentRepository.save(appointment);
    }
}
