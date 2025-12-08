CREATE TABLE fact_appointment (
                                  id                 BIGSERIAL PRIMARY KEY,
                                  appointment_id     BIGINT NOT NULL,
                                  doctor_id          BIGINT NOT NULL,
                                  patient_id         BIGINT NOT NULL,
                                  status             VARCHAR(50) NOT NULL,
                                  start_time         TIMESTAMP WITH TIME ZONE NOT NULL,
                                  end_time           TIMESTAMP WITH TIME ZONE,
                                  booking_time       TIMESTAMP WITH TIME ZONE NOT NULL,
                                  cancellation_time  TIMESTAMP WITH TIME ZONE,
                                  cancellation_reason VARCHAR(255),
                                  price_cents        BIGINT,
                                  source_service     VARCHAR(100) DEFAULT 'appointment-service',
                                  event_type         VARCHAR(50) NOT NULL,  -- CREATED, UPDATED, CANCELLED, COMPLETED
                                  event_time         TIMESTAMP WITH TIME ZONE NOT NULL,
                                  created_at         TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE INDEX idx_fact_appointment_status ON fact_appointment(status);
CREATE INDEX idx_fact_appointment_doctor ON fact_appointment(doctor_id);
CREATE INDEX idx_fact_appointment_patient ON fact_appointment(patient_id);
CREATE INDEX idx_fact_appointment_start_time ON fact_appointment(start_time);
CREATE INDEX idx_fact_appointment_event_time ON fact_appointment(event_time);

CREATE TABLE analytics_report (
                                  id          BIGSERIAL PRIMARY KEY,
                                  report_id   BIGINT NOT NULL UNIQUE,
                                  name        VARCHAR(255) NOT NULL,
                                  parameters  JSONB NOT NULL,
                                  status      VARCHAR(50) NOT NULL, -- PENDING, COMPLETED, FAILED
                                  result      JSONB,
                                  created_at  TIMESTAMP WITH TIME ZONE DEFAULT now(),
                                  updated_at  TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE INDEX idx_analytics_report_status ON analytics_report(status);
