package com.sealth.sealthapi.infra.seed;

import com.sealth.sealthapi.domain.appointment.Appointment;
import com.sealth.sealthapi.domain.user.User;
import com.sealth.sealthapi.repositories.AppointmentRepository;
import com.sealth.sealthapi.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

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
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, AppointmentRepository appointmentRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
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

        User maria = createUser("Maria Souza", SECOND_EMAIL);
        createAppointment(maria, today.plusDays(7), "Dr. Costa", "Oftalmologia", "Clínica Visão - Contagem", "Exame de vista");

        log.info("Seed carregado. Login de demonstração: {} / {} (segundo usuário: {} / {})",
                DEMO_EMAIL, DEMO_PASSWORD, SECOND_EMAIL, DEMO_PASSWORD);
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
