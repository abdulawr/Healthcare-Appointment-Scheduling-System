package com.basit.cz.dto;

import com.basit.cz.entity.CommunicationPreference;

import java.time.LocalDateTime;

public class CommunicationPreferenceDTO {

    public static class Request {
        public Boolean emailNotifications;
        public Boolean smsNotifications;
        public Boolean pushNotifications;
        public Boolean appointmentReminders;
        public Boolean marketingCommunications;
        public CommunicationPreference.ContactMethod preferredContactMethod;
        public CommunicationPreference.Language preferredLanguage;
        public Integer reminderHoursBefore;
    }

    public static class Response {
        public Long id;
        public Long patientId;
        public Boolean emailNotifications;
        public Boolean smsNotifications;
        public Boolean pushNotifications;
        public Boolean appointmentReminders;
        public Boolean marketingCommunications;
        public CommunicationPreference.ContactMethod preferredContactMethod;
        public CommunicationPreference.Language preferredLanguage;
        public Integer reminderHoursBefore;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;

        public static Response fromEntity(CommunicationPreference pref) {
            Response response = new Response();
            response.id = pref.id;
            response.patientId = pref.patient.id;
            response.emailNotifications = pref.emailNotifications;
            response.smsNotifications = pref.smsNotifications;
            response.pushNotifications = pref.pushNotifications;
            response.appointmentReminders = pref.appointmentReminders;
            response.marketingCommunications = pref.marketingCommunications;
            response.preferredContactMethod = pref.preferredContactMethod;
            response.preferredLanguage = pref.preferredLanguage;
            response.reminderHoursBefore = pref.reminderHoursBefore;
            response.createdAt = pref.createdAt;
            response.updatedAt = pref.updatedAt;
            return response;
        }
    }
}



