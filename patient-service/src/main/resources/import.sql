-- Sample patients
INSERT INTO patients (id, first_name, last_name, email, phone_number, date_of_birth, gender, is_active, created_at, updated_at)
VALUES
    (1, 'John', 'Doe', 'john.doe@email.com', '+420123456789', '1990-01-15', 'MALE', true, NOW(), NOW()),
    (2, 'Jane', 'Smith', 'jane.smith@email.com', '+420987654321', '1985-05-20', 'FEMALE', true, NOW(), NOW()),
    (3, 'Bob', 'Johnson', 'bob.johnson@email.com', '+420555666777', '1978-11-30', 'MALE', true, NOW(), NOW());

-- Reset sequence
ALTER SEQUENCE patients_seq RESTART WITH 4;