package com.sealth.sealthapi.infra.seed;

import com.sealth.sealthapi.domain.appointment.Appointment;
import com.sealth.sealthapi.domain.health.HealthRecord;
import com.sealth.sealthapi.domain.user.User;
import com.sealth.sealthapi.repositories.AppointmentRepository;
import com.sealth.sealthapi.repositories.HealthRecordRepository;
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
import java.time.temporal.ChronoUnit;

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
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, AppointmentRepository appointmentRepository,
                      HealthRecordRepository healthRecordRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.healthRecordRepository = healthRecordRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail(DEMO_EMAIL)) {
            return;
        }

        LocalDate today = LocalDate.now();

        User demo = createUser("Usuário Demo", DEMO_EMAIL);
        createAppointment(demo, today.minusDays(30), "Dr. Silva", "Cardiologia", "Hospital ABC - Contagem", "Retorno de rotina");
        createAppointment(demo, today.plusDays(3), "Dra. Souza", "Ortopedia", "Hospital XYZ - Betim", "Dores nas costas");
        createAppointment(demo, today.plusDays(15), "Dr. Almeida", "Dermatologia", "Clínica Bem Estar - BH", "Levar exames anteriores");
        createAppointment(demo, today.plusDays(45), "Dra. Lima", "Clínico Geral", "UBS Centro", null);

        Instant now = Instant.now();
        createHealthRecord(demo, now.minus(60, ChronoUnit.DAYS), 78.5, 175, 30, "masculino", null, null);
        createHealthRecord(demo, now.minus(30, ChronoUnit.DAYS), 76.2, 175, 30, "masculino", 128, 84);
        createHealthRecord(demo, now.minus(2, ChronoUnit.DAYS), 74.8, 175, 30, "masculino", 120, 80);

        User maria = createUser("Maria Souza", SECOND_EMAIL);
        createAppointment(maria, today.plusDays(7), "Dr. Costa", "Oftalmologia", "Clínica Visão - Contagem", "Exame de vista");

        log.info("Seed carregado. Login de demonstração: {} / {} (segundo usuário: {} / {})",
                DEMO_EMAIL, DEMO_PASSWORD, SECOND_EMAIL, DEMO_PASSWORD);
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

    private void createAppointment(User owner, LocalDate date, String doctor, String specialty, String location, String extraInfo) {
        Appointment appointment = new Appointment();
        appointment.setOwner(owner);
        appointment.setDate(date);
        appointment.setDoctor(doctor);
        appointment.setSpecialty(specialty);
        appointment.setLocation(location);
        appointment.setExtraInfo(extraInfo);
        appointmentRepository.save(appointment);
    }
}
