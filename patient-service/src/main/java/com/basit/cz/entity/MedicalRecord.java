package com.basit.cz.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "medical_records")
public class MedicalRecord extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    public Patient patient;

    @Enumerated(EnumType.STRING)
    @Column(name = "record_type", nullable = false)
    public RecordType recordType;

    @Column(name = "record_date", nullable = false)
    public LocalDate recordDate;

    @Column(length = 1000)
    public String description;

    @Column(length = 500)
    public String diagnosis;

    @Column(length = 500)
    public String prescription;

    @Column(name = "doctor_name", length = 100)
    public String doctorName;

    @Column(name = "hospital_name", length = 200)
    public String hospitalName;

    @Column(length = 1000)
    public String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    public enum RecordType {
        ALLERGY,
        CHRONIC_CONDITION,
        SURGERY,
        MEDICATION,
        VACCINATION,
        LAB_RESULT,
        DIAGNOSIS,
        TREATMENT,
        CONSULTATION,
        OTHER
    }

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "MedicalRecord{" +
                "id=" + id +
                ", recordType=" + recordType +
                ", recordDate=" + recordDate +
                '}';
    }
}



