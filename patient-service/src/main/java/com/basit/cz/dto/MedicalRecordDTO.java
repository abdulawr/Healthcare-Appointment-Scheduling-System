package com.basit.cz.dto;

import com.basit.cz.entity.MedicalRecord;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MedicalRecordDTO {

    public static class Request {
        @NotNull(message = "Record type is required")
        public MedicalRecord.RecordType recordType;

        @NotNull(message = "Record date is required")
        public LocalDate recordDate;

        public String description;
        public String diagnosis;
        public String prescription;
        public String doctorName;
        public String hospitalName;
        public String notes;
    }

    public static class Response {
        public Long id;
        public Long patientId;
        public MedicalRecord.RecordType recordType;
        public LocalDate recordDate;
        public String description;
        public String diagnosis;
        public String prescription;
        public String doctorName;
        public String hospitalName;
        public String notes;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;

        public static Response fromEntity(MedicalRecord record) {
            Response response = new Response();
            response.id = record.id;
            response.patientId = record.patient.id;
            response.recordType = record.recordType;
            response.recordDate = record.recordDate;
            response.description = record.description;
            response.diagnosis = record.diagnosis;
            response.prescription = record.prescription;
            response.doctorName = record.doctorName;
            response.hospitalName = record.hospitalName;
            response.notes = record.notes;
            response.createdAt = record.createdAt;
            response.updatedAt = record.updatedAt;
            return response;
        }
    }
}



