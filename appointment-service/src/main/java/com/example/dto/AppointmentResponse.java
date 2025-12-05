package com.example.dto;

import com.example.constant.AppointmentStatus;
import com.example.constant.AppointmentType;
import com.example.entity.Appointment;

import java.time.LocalDateTime;

/**
 * DTO for appointment response
 */
public class AppointmentResponse {

    public Long id;
    public Long patientId;
    public Long doctorId;
    public LocalDateTime startTime;
    public LocalDateTime endTime;
    public AppointmentStatus status;
    public AppointmentType type;
    public String notes;
    public String reason;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public LocalDateTime checkedInAt;
    public LocalDateTime completedAt;
    public LocalDateTime cancelledAt;
    public String cancellationReason;
    public boolean reminderSent;
    public boolean confirmationSent;

    // Default constructor
    public AppointmentResponse() {}

    // Constructor from entity
    public AppointmentResponse(Appointment appointment) {
        this.id = appointment.id;
        this.patientId = appointment.patientId;
        this.doctorId = appointment.doctorId;
        this.startTime = appointment.startTime;
        this.endTime = appointment.endTime;
        this.status = appointment.status;
        this.type = appointment.type;
        this.notes = appointment.notes;
        this.reason = appointment.reason;
        this.createdAt = appointment.createdAt;
        this.updatedAt = appointment.updatedAt;
        this.checkedInAt = appointment.checkedInAt;
        this.completedAt = appointment.completedAt;
        this.cancelledAt = appointment.cancelledAt;
        this.cancellationReason = appointment.cancellationReason;
        this.reminderSent = appointment.reminderSent;
        this.confirmationSent = appointment.confirmationSent;
    }

    // Builder pattern for testing
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private AppointmentResponse response = new AppointmentResponse();

        public Builder id(Long id) {
            response.id = id;
            return this;
        }

        public Builder patientId(Long patientId) {
            response.patientId = patientId;
            return this;
        }

        public Builder doctorId(Long doctorId) {
            response.doctorId = doctorId;
            return this;
        }

        public Builder startTime(LocalDateTime startTime) {
            response.startTime = startTime;
            return this;
        }

        public Builder endTime(LocalDateTime endTime) {
            response.endTime = endTime;
            return this;
        }

        public Builder status(AppointmentStatus status) {
            response.status = status;
            return this;
        }

        public Builder type(AppointmentType type) {
            response.type = type;
            return this;
        }

        public AppointmentResponse build() {
            return response;
        }
    }
}


