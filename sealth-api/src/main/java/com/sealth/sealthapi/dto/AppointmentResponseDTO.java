package com.sealth.sealthapi.dto;

import com.sealth.sealthapi.domain.appointment.Appointment;

import java.time.LocalDate;

public record AppointmentResponseDTO(Long id, LocalDate date, String doctor, String specialty, String location, String extraInfo) {

    public static AppointmentResponseDTO from(Appointment appointment) {
        return new AppointmentResponseDTO(
                appointment.getId(),
                appointment.getDate(),
                appointment.getDoctor(),
                appointment.getSpecialty(),
                appointment.getLocation(),
                appointment.getExtraInfo()
        );
    }
}
