package com.basit.cz.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "communication_preferences")
public class CommunicationPreference extends PanacheEntity {

    @OneToOne
    @JoinColumn(name = "patient_id", nullable = false)
    public Patient patient;

    @Column(name = "email_notifications", nullable = false)
    public Boolean emailNotifications = true;

    @Column(name = "sms_notifications", nullable = false)
    public Boolean smsNotifications = true;

    @Column(name = "push_notifications", nullable = false)
    public Boolean pushNotifications = false;

    @Column(name = "appointment_reminders", nullable = false)
    public Boolean appointmentReminders = true;

    @Column(name = "marketing_communications", nullable = false)
    public Boolean marketingCommunications = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_contact_method", nullable = false)
    public ContactMethod preferredContactMethod = ContactMethod.EMAIL;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_language", nullable = false)
    public Language preferredLanguage = Language.ENGLISH;

    @Column(name = "reminder_hours_before")
    public Integer reminderHoursBefore = 24;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    public enum ContactMethod {
        EMAIL, SMS, PHONE, PUSH
    }

    public enum Language {
        ENGLISH, SPANISH, FRENCH, GERMAN, CZECH, OTHER
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
        return "CommunicationPreference{" +
                "id=" + id +
                ", preferredContactMethod=" + preferredContactMethod +
                ", preferredLanguage=" + preferredLanguage +
                '}';
    }
}
