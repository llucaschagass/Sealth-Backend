package com.sealth.sealthapi.services;

import com.sealth.sealthapi.domain.appointment.Appointment;
import com.sealth.sealthapi.domain.user.User;
import com.sealth.sealthapi.dto.AppointmentResponseDTO;
import com.sealth.sealthapi.dto.CreateAppointmentRequestDTO;
import com.sealth.sealthapi.dto.UpdateAppointmentRequestDTO;
import com.sealth.sealthapi.infra.exception.ResourceNotFoundException;
import com.sealth.sealthapi.repositories.AppointmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public AppointmentResponseDTO createAppointment(User owner, CreateAppointmentRequestDTO request) {
        Appointment appointment = new Appointment();
        appointment.setOwner(owner);
        appointment.setDate(request.date());
        appointment.setTime(request.time());
        appointment.setReminderMinutes(request.reminderMinutes());
        appointment.setDoctor(request.doctor());
        appointment.setSpecialty(request.specialty());
        appointment.setLocation(request.location());
        appointment.setExtraInfo(request.extraInfo());
        return AppointmentResponseDTO.from(appointmentRepository.save(appointment));
    }

    /** Ordenadas por data e, no mesmo dia, por horário (consultas sem horário por último). */
    public List<AppointmentResponseDTO> getAppointments(User owner) {
        return appointmentRepository.findByOwnerIdOrderByDateAsc(owner.getId()).stream()
                .sorted(Comparator.comparing(Appointment::getDate)
                        .thenComparing(Appointment::getTime, Comparator.nullsLast(Comparator.<LocalTime>naturalOrder())))
                .map(AppointmentResponseDTO::from)
                .toList();
    }

    /** Consultas cujo lembrete já passou a valer e que ainda não começaram, da mais próxima para a mais distante. */
    public List<AppointmentResponseDTO> getDueReminders(User owner) {
        LocalDateTime now = LocalDateTime.now();
        return appointmentRepository.findByOwnerIdOrderByDateAsc(owner.getId()).stream()
                .filter(a -> a.getReminderAt() != null && !now.isBefore(a.getReminderAt()) && now.isBefore(a.getStartsAt()))
                .sorted(Comparator.comparing(Appointment::getStartsAt))
                .map(AppointmentResponseDTO::from)
                .toList();
    }

    public AppointmentResponseDTO updateAppointment(User owner, Long appointmentId, UpdateAppointmentRequestDTO request) {
        Appointment appointment = findOwned(owner, appointmentId);
        appointment.setDate(request.date());
        appointment.setTime(request.time());
        appointment.setReminderMinutes(request.reminderMinutes());
        appointment.setDoctor(request.doctor());
        appointment.setSpecialty(request.specialty());
        appointment.setLocation(request.location());
        appointment.setExtraInfo(request.extraInfo());
        return AppointmentResponseDTO.from(appointmentRepository.save(appointment));
    }

    public void cancelAppointment(User owner, Long appointmentId) {
        appointmentRepository.delete(findOwned(owner, appointmentId));
    }

    private Appointment findOwned(User owner, Long appointmentId) {
        return appointmentRepository.findByIdAndOwnerId(appointmentId, owner.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada"));
    }
}
