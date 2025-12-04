CREATE TABLE
    IF NOT EXISTS app_user (
        user_id BINARY(16) PRIMARY KEY,
        email VARCHAR(100) NOT NULL UNIQUE,
        password_hash VARCHAR(255) NOT NULL,
        first_name VARCHAR(50) NOT NULL,
        last_name VARCHAR(50) NOT NULL,
        user_role VARCHAR(20) NOT NULL
    ) ENGINE = InnoDB;

CREATE TABLE
    IF NOT EXISTS patient_profile (
        patient_id BINARY(16) PRIMARY KEY,
        phone_number VARCHAR(20) NOT NULL,
        date_of_birth DATE NOT NULL,
        CONSTRAINT fk_patient_user FOREIGN KEY (patient_id) REFERENCES app_user (user_id) ON DELETE CASCADE
    ) ENGINE = InnoDB;