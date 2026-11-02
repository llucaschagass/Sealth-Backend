package com.sealth.sealthapi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sealth.sealthapi.domain.appointment.Appointment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** `reminderAt` é o momento em que o lembrete passa a valer (nulo sem horário ou sem lembrete). */
public record AppointmentResponseDTO(
        Long id,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime time,
        String doctor,
        String specialty,
        String location,
        String extraInfo,
        Integer reminderMinutes,
        LocalDateTime reminderAt) {

    public static AppointmentResponseDTO from(Appointment appointment) {
        return new AppointmentResponseDTO(
                appointment.getId(),
                appointment.getDate(),
                appointment.getTime(),
                appointment.getDoctor(),
                appointment.getSpecialty(),
                appointment.getLocation(),
                appointment.getExtraInfo(),
                appointment.getReminderMinutes(),
                appointment.getReminderAt()
        );
    }
}
