package com.basit.cz.dto;

import com.basit.cz.entity.Insurance;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class InsuranceDTO {

    public static class Request {
        @NotBlank(message = "Provider name is required")
        public String providerName;

        @NotBlank(message = "Policy number is required")
        public String policyNumber;

        public String groupNumber;

        @NotBlank(message = "Policy holder name is required")
        public String policyHolderName;

        @NotNull(message = "Policy holder relationship is required")
        public Insurance.PolicyHolderRelationship policyHolderRelationship;

        public LocalDate coverageStartDate;
        public LocalDate coverageEndDate;
        public Double copayAmount;
        public Double deductibleAmount;
    }

    public static class Response {
        public Long id;
        public Long patientId;
        public String providerName;
        public String policyNumber;
        public String groupNumber;
        public String policyHolderName;
        public Insurance.PolicyHolderRelationship policyHolderRelationship;
        public LocalDate coverageStartDate;
        public LocalDate coverageEndDate;
        public Double copayAmount;
        public Double deductibleAmount;
        public Boolean isActive;
        public Boolean isCoverageActive;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;

        public static Response fromEntity(Insurance insurance) {
            Response response = new Response();
            response.id = insurance.id;
            response.patientId = insurance.patient.id;
            response.providerName = insurance.providerName;
            response.policyNumber = insurance.policyNumber;
            response.groupNumber = insurance.groupNumber;
            response.policyHolderName = insurance.policyHolderName;
            response.policyHolderRelationship = insurance.policyHolderRelationship;
            response.coverageStartDate = insurance.coverageStartDate;
            response.coverageEndDate = insurance.coverageEndDate;
            response.copayAmount = insurance.copayAmount;
            response.deductibleAmount = insurance.deductibleAmount;
            response.isActive = insurance.isActive;
            response.isCoverageActive = insurance.isCoverageActive();
            response.createdAt = insurance.createdAt;
            response.updatedAt = insurance.updatedAt;
            return response;
        }
    }
}



