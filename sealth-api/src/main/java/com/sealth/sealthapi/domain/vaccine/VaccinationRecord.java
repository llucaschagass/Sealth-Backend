package com.sealth.sealthapi.domain.vaccine;

import com.sealth.sealthapi.domain.user.User;
import jakarta.persistence.*;

import java.time.LocalDate;

/** Uma dose de vacina tomada por um usuário. */
@Entity
@Table(name = "vaccination_records")
public class VaccinationRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "vaccine_code", nullable = false)
    private String vaccineCode;

    @Column(name = "taken_at", nullable = false)
    private LocalDate takenAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    public VaccinationRecord() {
    }

    public Long getId() {
        return id;
    }

    public String getVaccineCode() {
        return vaccineCode;
    }

    public void setVaccineCode(String vaccineCode) {
        this.vaccineCode = vaccineCode;
    }

    public LocalDate getTakenAt() {
        return takenAt;
    }

    public void setTakenAt(LocalDate takenAt) {
        this.takenAt = takenAt;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }
}
