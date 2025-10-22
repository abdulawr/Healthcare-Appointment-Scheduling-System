-- Sample patients for development
INSERT INTO patients (first_name, last_name, date_of_birth, email, phone_number, gender, address, medical_history, blood_type, emergency_contact_name, emergency_contact_phone, status, created_at, updated_at)
VALUES
    ('John', 'Doe', '1985-03-15', 'john.doe@email.com', '+420123456789', 'MALE', '123 Main Street, Prague', 'Hypertension, Type 2 Diabetes', 'A+', 'Jane Doe', '+420987654321', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    ('Jane', 'Smith', '1990-07-22', 'jane.smith@email.com', '+420234567890', 'FEMALE', '456 Oak Avenue, Brno', 'Asthma', 'O-', 'John Smith', '+420876543210', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    ('Robert', 'Johnson', '1978-11-30', 'robert.j@email.com', '+420345678901', 'MALE', '789 Pine Road, Ostrava', 'None', 'B+', 'Mary Johnson', '+420765432109', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    ('Emily', 'Williams', '1995-02-14', 'emily.w@email.com', '+420456789012', 'FEMALE', '321 Elm Street, Plzen', 'Migraine', 'AB+', 'Michael Williams', '+420654321098', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    ('Michael', 'Brown', '1982-09-08', 'michael.b@email.com', '+420567890123', 'MALE', '654 Maple Drive, Liberec', 'High cholesterol', 'A-', 'Sarah Brown', '+420543210987', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);