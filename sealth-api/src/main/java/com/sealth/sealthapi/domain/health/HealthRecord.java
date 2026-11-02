package com.sealth.sealthapi.domain.health;

import com.sealth.sealthapi.domain.user.User;
import jakarta.persistence.*;

import java.time.Instant;

/** Medição de saúde de um usuário: um instantâneo dos dados usados nas calculadoras. */
@Entity
@Table(name = "health_records")
public class HealthRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "measured_at", nullable = false)
    private Instant measuredAt;

    @Column(name = "weight_kg", nullable = false)
    private double weightKg;

    @Column(name = "height_cm", nullable = false)
    private double heightCm;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false)
    private String sex;

    /** Pressão sistólica (mmHg); opcional. */
    private Integer systolic;

    /** Pressão diastólica (mmHg); opcional. */
    private Integer diastolic;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    public HealthRecord() {
    }

    public Long getId() {
        return id;
    }

    public Instant getMeasuredAt() {
        return measuredAt;
    }

    public void setMeasuredAt(Instant measuredAt) {
        this.measuredAt = measuredAt;
    }

    public double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(double weightKg) {
        this.weightKg = weightKg;
    }

    public double getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(double heightCm) {
        this.heightCm = heightCm;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Integer getSystolic() {
        return systolic;
    }

    public void setSystolic(Integer systolic) {
        this.systolic = systolic;
    }

    public Integer getDiastolic() {
        return diastolic;
    }

    public void setDiastolic(Integer diastolic) {
        this.diastolic = diastolic;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }
}
