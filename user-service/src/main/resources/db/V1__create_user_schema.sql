CREATE TABLE
    IF NOT EXISTS app_user (
        user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
        auth_user_id BIGINT NOT NULL UNIQUE,
        email VARCHAR(100) NOT NULL UNIQUE,
        first_name VARCHAR(50) NOT NULL,
        last_name VARCHAR(50) NOT NULL,
        user_role VARCHAR(20) NOT NULL,
        INDEX idx_auth_user_id (auth_user_id),
        INDEX idx_email (email)
    ) ENGINE = InnoDB;

CREATE TABLE
    IF NOT EXISTS patient_profile (
        patient_id BIGINT PRIMARY KEY,
        phone_number VARCHAR(20) NOT NULL,
        date_of_birth DATE NOT NULL,
        CONSTRAINT fk_patient_user FOREIGN KEY (patient_id) REFERENCES app_user (user_id) ON DELETE CASCADE
    ) ENGINE = InnoDB;