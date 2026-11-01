package com.sealth.sealthapi.domain.appointment;

import com.sealth.sealthapi.domain.user.User;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate date;

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
