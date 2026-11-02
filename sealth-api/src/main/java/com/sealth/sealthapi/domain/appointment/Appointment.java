package com.sealth.sealthapi.domain.appointment;

import com.sealth.sealthapi.domain.user.User;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate date;

    /** Horário da consulta; opcional. */
    @Column(name = "appointment_time")
    private LocalTime time;

    /** Antecedência do lembrete em minutos; opcional e só vale com horário. */
    @Column(name = "reminder_minutes")
    private Integer reminderMinutes;

    @Column(name = "doctor", nullable = false)
    private String doctor;

    @Column(name = "specialty", nullable = false)
    private String specialty;

    @Column(name = "location")
    private String location;

    @Column(name = "extra_info")
    private String extraInfo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    public Appointment() {
    }

    // Getters
    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public Integer getReminderMinutes() {
        return reminderMinutes;
    }

    /** Instante de início da consulta; nulo sem horário. */
    public LocalDateTime getStartsAt() {
        return time == null ? null : LocalDateTime.of(date, time);
    }

    /** Momento a partir do qual o lembrete vale; nulo sem horário ou sem lembrete. */
    public LocalDateTime getReminderAt() {
        return time == null || reminderMinutes == null ? null : getStartsAt().minusMinutes(reminderMinutes);
    }

    public String getDoctor() {
        return doctor;
    }

    public String getSpecialty() {
        return specialty;
    }

    public String getLocation() {
        return location;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public User getOwner() {
        return owner;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public void setReminderMinutes(Integer reminderMinutes) {
        this.reminderMinutes = reminderMinutes;
    }

    public void setDoctor(String doctor) {
        this.doctor = doctor;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }
}
