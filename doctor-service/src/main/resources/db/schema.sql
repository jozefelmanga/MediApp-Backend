CREATE TABLE
    IF NOT EXISTS specialty (
        specialty_id INT AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(100) NOT NULL UNIQUE
    );

CREATE TABLE
    IF NOT EXISTS doctor_profile (
        doctor_id BIGINT AUTO_INCREMENT PRIMARY KEY,
        user_id BIGINT NOT NULL UNIQUE,
        medical_license_number VARCHAR(50) NOT NULL UNIQUE,
        specialty_id INT NOT NULL,
        office_address VARCHAR(255) NOT NULL,
        CONSTRAINT doctor_profile_specialty_fk FOREIGN KEY (specialty_id) REFERENCES specialty (specialty_id),
        INDEX idx_user_id (user_id)
    );

CREATE TABLE
    IF NOT EXISTS availability_slot (
        slot_id BIGINT AUTO_INCREMENT PRIMARY KEY,
        doctor_id BIGINT NOT NULL,
        start_time DATETIME NOT NULL,
        end_time DATETIME NOT NULL,
        is_reserved BOOLEAN NOT NULL DEFAULT FALSE,
        version BIGINT NOT NULL DEFAULT 0,
        CONSTRAINT availability_slot_doctor_fk FOREIGN KEY (doctor_id) REFERENCES doctor_profile (doctor_id) ON DELETE CASCADE,
        INDEX idx_doctor_time (doctor_id, start_time, end_time)
    );

-- Removed redundant index as it's now part of table definition

INSERT IGNORE INTO specialty (specialty_id, name)
VALUES
    (1, 'Cardiology');

INSERT IGNORE INTO specialty (specialty_id, name)
VALUES
    (2, 'Dermatology');

INSERT IGNORE INTO specialty (specialty_id, name)
VALUES
    (3, 'Neurology');

INSERT IGNORE INTO specialty (specialty_id, name)
VALUES
    (4, 'Pediatrics');