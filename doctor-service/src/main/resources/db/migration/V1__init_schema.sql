CREATE TABLE
    IF NOT EXISTS specialty (
        specialty_id INT AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(100) NOT NULL UNIQUE
    );

CREATE TABLE
    IF NOT EXISTS doctor_profile (
        doctor_id CHAR(36) PRIMARY KEY,
        medical_license_number VARCHAR(50) NOT NULL UNIQUE,
        specialty_id INT NOT NULL,
        office_address VARCHAR(255) NOT NULL,
        CONSTRAINT doctor_profile_specialty_fk FOREIGN KEY (specialty_id) REFERENCES specialty (specialty_id)
    );

CREATE TABLE
    IF NOT EXISTS availability_slot (
        slot_id CHAR(36) PRIMARY KEY,
        doctor_id CHAR(36) NOT NULL,
        start_time DATETIME NOT NULL,
        end_time DATETIME NOT NULL,
        is_reserved BOOLEAN NOT NULL DEFAULT FALSE,
        version BIGINT NOT NULL DEFAULT 0,
        CONSTRAINT availability_slot_doctor_fk FOREIGN KEY (doctor_id) REFERENCES doctor_profile (doctor_id) ON DELETE CASCADE
    );

CREATE INDEX availability_slot_doctor_time_idx ON availability_slot (doctor_id, start_time, end_time);

INSERT INTO
    specialty (specialty_id, name)
VALUES
    (1, 'Cardiology'),
    (2, 'Dermatology'),
    (3, 'Neurology'),
    (4, 'Pediatrics') ON DUPLICATE KEY
UPDATE name =
VALUES
    (name);