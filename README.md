# MediApp - Medical Appointment Booking System

A microservices-based medical appointment booking application built with Spring Boot 4.0.0 and Spring Cloud.

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Microservices & Ports](#microservices--ports)
- [Libraries Used](#libraries-used)
- [Database Schemas](#database-schemas)
- [Getting Started](#getting-started)

---

## Architecture Overview

The application follows a microservices architecture with the following components:

- **Discovery Server (Eureka)** - Service registry for service discovery
- **Gateway Service** - API Gateway for routing requests
- **User Service** - User account management
- **Doctor Service** - Doctor profiles and availability management (Reactive)
- **Booking Service** - Appointment booking management
- **Notification Service** - Notification handling via RabbitMQ
- **Security Service** - Authentication and authorization with JWT
- **Catalogue Service** - Catalogue management

---

## Microservices & Ports

| Service                  | Port             | Database               | Description                               |
| ------------------------ | ---------------- | ---------------------- | ----------------------------------------- |
| **Discovery Server**     | `8761`           | None                   | Eureka Service Registry                   |
| **Gateway Service**      | `8550`           | None                   | API Gateway (WebFlux)                     |
| **User Service**         | `8666`           | `mediapp_user`         | User management                           |
| **Doctor Service**       | `0` (Random)     | `mediapp_doctor`       | Doctor profiles & availability (Reactive) |
| **Booking Service**      | `8084`           | `mediapp_booking`      | Appointment bookings                      |
| **Notification Service** | `8667`           | `mediapp_notification` | Notification logs                         |
| **Security Service**     | `8081`           | `mediapp_security`     | JWT Authentication                        |
| **Catalogue Service**    | `8080` (default) | `catalogue_db`         | Catalogue management                      |

### External Services

| Service      | Port   | Description      |
| ------------ | ------ | ---------------- |
| **MySQL**    | `3306` | Primary database |
| **RabbitMQ** | `5672` | Message broker   |

---

## Libraries Used

### Discovery Server

| Library                            | Purpose                      |
| ---------------------------------- | ---------------------------- |
| Spring Boot Starter Actuator       | Health checks and monitoring |
| Spring Cloud Netflix Eureka Server | Service discovery server     |
| Hibernate Validator                | Bean validation              |
| Lombok                             | Boilerplate code reduction   |

### Gateway Service

| Library                             | Purpose                      |
| ----------------------------------- | ---------------------------- |
| Spring Boot Starter Actuator        | Health checks and monitoring |
| Spring Boot Starter Validation      | Request validation           |
| Spring Cloud Gateway Server WebFlux | Reactive API gateway         |
| Spring Cloud Netflix Eureka Client  | Service discovery client     |
| Spring Cloud Starter LoadBalancer   | Client-side load balancing   |
| Lombok                              | Boilerplate code reduction   |
| Reactor Test                        | Testing reactive streams     |

### User Service

| Library                        | Purpose                      |
| ------------------------------ | ---------------------------- |
| Spring Boot Starter Actuator   | Health checks and monitoring |
| Spring Boot Starter Data JPA   | Database operations          |
| Spring Boot Starter Validation | Request validation           |
| Spring Boot Starter Web        | REST API                     |
| Spring Security Crypto         | Password encryption          |
| MySQL Connector J              | MySQL driver                 |
| Lombok                         | Boilerplate code reduction   |
| Common-libs                    | Shared utilities             |
| Logging-starter                | Centralized logging          |

### Doctor Service

| Library                                           | Purpose                               |
| ------------------------------------------------- | ------------------------------------- |
| Spring Boot Starter Actuator                      | Health checks and monitoring          |
| Spring Boot Starter Data R2DBC                    | Reactive database operations          |
| Spring Boot Starter Validation                    | Request validation                    |
| Spring Boot Starter WebFlux                       | Reactive REST API                     |
| Spring Cloud Circuit Breaker Reactor Resilience4j | Fault tolerance                       |
| R2DBC MySQL                                       | Reactive MySQL driver                 |
| MySQL Connector J                                 | JDBC driver for schema initialization |
| Micrometer Prometheus                             | Metrics export                        |
| Testcontainers (MySQL, R2DBC)                     | Integration testing                   |
| Lombok                                            | Boilerplate code reduction            |
| Common-libs                                       | Shared utilities                      |
| Logging-starter                                   | Centralized logging                   |

### Booking Service

| Library                                   | Purpose                      |
| ----------------------------------------- | ---------------------------- |
| Spring Boot Starter Actuator              | Health checks and monitoring |
| Spring Boot Starter AMQP                  | RabbitMQ messaging           |
| Spring Boot Starter Data JPA              | Database operations          |
| Spring Boot Starter Validation            | Request validation           |
| Spring Boot Starter WebMVC                | REST API                     |
| Spring Cloud Circuit Breaker Resilience4j | Fault tolerance              |
| Spring Cloud Netflix Eureka Client        | Service discovery client     |
| MySQL Connector J                         | MySQL driver                 |
| Lombok                                    | Boilerplate code reduction   |

### Notification Service

| Library                      | Purpose                      |
| ---------------------------- | ---------------------------- |
| Spring Boot Starter Actuator | Health checks and monitoring |
| Spring Boot Starter AMQP     | RabbitMQ messaging           |
| Spring Boot Starter Data JPA | Database operations          |
| Spring Boot Starter WebMVC   | REST API                     |
| Jackson Databind             | JSON serialization           |
| Jackson Datatype JSR310      | Java 8 date/time support     |
| MySQL Connector J            | MySQL driver                 |
| Lombok                       | Boilerplate code reduction   |

### Security Service

| Library                            | Purpose                      |
| ---------------------------------- | ---------------------------- |
| Spring Boot Starter Actuator       | Health checks and monitoring |
| Spring Boot Starter Data JPA       | Database operations          |
| Spring Boot Starter Security       | Authentication/Authorization |
| Spring Boot Starter Validation     | Request validation           |
| Spring Boot Starter Web            | REST API                     |
| Spring Security OAuth2 JOSE        | JWT token handling           |
| Spring Cloud Netflix Eureka Client | Service discovery client     |
| MySQL Connector J                  | MySQL driver                 |
| Lombok                             | Boilerplate code reduction   |
| Common-libs                        | Shared utilities             |
| Logging-starter                    | Centralized logging          |
| Contract-test-support              | Contract testing             |

### Catalogue Service

| Library                        | Purpose                      |
| ------------------------------ | ---------------------------- |
| Spring Boot Starter Actuator   | Health checks and monitoring |
| Spring Boot Starter Data JPA   | Database operations          |
| Spring Boot Starter Validation | Request validation           |
| Spring Boot Starter WebMVC     | REST API                     |
| MySQL Connector J              | MySQL driver                 |
| Lombok                         | Boilerplate code reduction   |

---

## Database Schemas

### User Service Database (`mediapp_user`)

#### Table: `app_user`

| Column        | Type         | Constraints                             |
| ------------- | ------------ | --------------------------------------- |
| user_id       | BINARY(16)   | PRIMARY KEY                             |
| email         | VARCHAR(100) | NOT NULL, UNIQUE                        |
| password_hash | VARCHAR(255) | NOT NULL                                |
| first_name    | VARCHAR(50)  | NOT NULL                                |
| last_name     | VARCHAR(50)  | NOT NULL                                |
| user_role     | VARCHAR(20)  | NOT NULL (ENUM: PATIENT, DOCTOR, ADMIN) |

#### Table: `patient_profile`

| Column        | Type        | Constraints                        |
| ------------- | ----------- | ---------------------------------- |
| patient_id    | BINARY(16)  | PRIMARY KEY, FK → app_user.user_id |
| phone_number  | VARCHAR(20) | NOT NULL                           |
| date_of_birth | DATE        | NOT NULL                           |

---

### Doctor Service Database (`mediapp_doctor`)

#### Table: `specialty`

| Column       | Type         | Constraints                 |
| ------------ | ------------ | --------------------------- |
| specialty_id | INT          | PRIMARY KEY, AUTO_INCREMENT |
| name         | VARCHAR(100) | NOT NULL, UNIQUE            |

#### Table: `doctor_profile`

| Column                 | Type         | Constraints                                   |
| ---------------------- | ------------ | --------------------------------------------- |
| doctor_id              | CHAR(36)     | PRIMARY KEY                                   |
| medical_license_number | VARCHAR(50)  | NOT NULL, UNIQUE                              |
| specialty_id           | INT          | NOT NULL, FK → specialty.specialty_id         |
| office_address         | VARCHAR(255) | NOT NULL                                      |
| created_at             | DATETIME     | NOT NULL, DEFAULT CURRENT_TIMESTAMP           |
| updated_at             | DATETIME     | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE |

#### Table: `availability_slot`

| Column            | Type         | Constraints                                   |
| ----------------- | ------------ | --------------------------------------------- |
| slot_id           | CHAR(36)     | PRIMARY KEY                                   |
| doctor_id         | CHAR(36)     | NOT NULL, FK → doctor_profile.doctor_id       |
| start_time        | DATETIME     | NOT NULL                                      |
| end_time          | DATETIME     | NOT NULL                                      |
| is_reserved       | BOOLEAN      | NOT NULL, DEFAULT FALSE                       |
| reservation_token | VARCHAR(100) | UNIQUE                                        |
| reserved_at       | DATETIME     | NULL                                          |
| version           | BIGINT       | NOT NULL, DEFAULT 0                           |
| created_at        | DATETIME     | NOT NULL, DEFAULT CURRENT_TIMESTAMP           |
| updated_at        | DATETIME     | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE |

**Index:** `availability_slot_doctor_time_idx` on (doctor_id, start_time, end_time)

---

### Booking Service Database (`mediapp_booking`)

#### Table: `appointment`

| Column           | Type     | Constraints                                    |
| ---------------- | -------- | ---------------------------------------------- |
| appointment_id   | UUID     | PRIMARY KEY                                    |
| patient_id       | UUID     | NOT NULL                                       |
| doctor_id        | UUID     | NOT NULL                                       |
| slot_id          | UUID     | NOT NULL, UNIQUE                               |
| appointment_date | DATE     | NOT NULL                                       |
| start_time       | TIME     | NOT NULL                                       |
| status           | VARCHAR  | NOT NULL (ENUM: PENDING, CONFIRMED, CANCELLED) |
| created_at       | DATETIME | NOT NULL                                       |
| updated_at       | DATETIME |                                                |
| version          | BIGINT   |                                                |

**Indexes:**

- `idx_appointment_patient` on (patient_id)
- `idx_appointment_doctor` on (doctor_id)
- `idx_appointment_slot` on (slot_id)
- `idx_appointment_date_status` on (appointment_date, status)

---

### Notification Service Database (`mediapp_notification`)

#### Table: `notification_log`

| Column            | Type     | Constraints                 |
| ----------------- | -------- | --------------------------- |
| log_id            | UUID     | PRIMARY KEY                 |
| event_id          | UUID     | NOT NULL, UNIQUE            |
| recipient_user_id | UUID     |                             |
| message_type      | VARCHAR  |                             |
| status            | VARCHAR  | ENUM: PENDING, SENT, FAILED |
| payload           | TEXT     |                             |
| created_at        | DATETIME | NOT NULL                    |
| sent_at           | DATETIME |                             |
| attempts          | INT      |                             |
| last_error        | TEXT     |                             |

**Indexes:**

- `idx_notification_event` on (event_id)
- `idx_notification_recipient` on (recipient_user_id)

---

### Security Service Database (`mediapp_security`)

#### Table: `app_users`

| Column                  | Type         | Constraints            |
| ----------------------- | ------------ | ---------------------- |
| id                      | UUID         | PRIMARY KEY            |
| email                   | VARCHAR      | NOT NULL, UNIQUE       |
| password_hash           | VARCHAR(120) | NOT NULL               |
| enabled                 | BOOLEAN      | NOT NULL, DEFAULT TRUE |
| account_non_expired     | BOOLEAN      | NOT NULL, DEFAULT TRUE |
| credentials_non_expired | BOOLEAN      | NOT NULL, DEFAULT TRUE |
| account_non_locked      | BOOLEAN      | NOT NULL, DEFAULT TRUE |
| created_at              | TIMESTAMP    | NOT NULL               |
| updated_at              | TIMESTAMP    | NOT NULL               |

#### Table: `app_user_roles`

| Column  | Type        | Constraints                             |
| ------- | ----------- | --------------------------------------- |
| user_id | UUID        | FK → app_users.id                       |
| role    | VARCHAR(32) | NOT NULL (ENUM: ADMIN, DOCTOR, PATIENT) |

#### Table: `refresh_tokens`

| Column     | Type         | Constraints                 |
| ---------- | ------------ | --------------------------- |
| id         | UUID         | PRIMARY KEY                 |
| token      | VARCHAR(128) | NOT NULL, UNIQUE            |
| expires_at | TIMESTAMP    | NOT NULL                    |
| revoked    | BOOLEAN      | NOT NULL, DEFAULT FALSE     |
| created_at | TIMESTAMP    | NOT NULL                    |
| updated_at | TIMESTAMP    | NOT NULL                    |
| user_id    | UUID         | NOT NULL, FK → app_users.id |

---

### Catalogue Service Database (`catalogue_db`)

_No entity classes defined yet - service is in initial setup._

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8.0+
- RabbitMQ 3.x (for messaging)

### Running the Services

1. **Start the Discovery Server first:**

   ```bash
   cd discovery-server
   ./mvnw spring-boot:run
   ```

2. **Start the Gateway Service:**

   ```bash
   cd gateway-service
   ./mvnw spring-boot:run
   ```

3. **Start individual microservices:**
   ```bash
   cd <service-name>
   ./mvnw spring-boot:run
   ```

### Service Access URLs

| Service              | URL                   |
| -------------------- | --------------------- |
| Eureka Dashboard     | http://localhost:8761 |
| Gateway              | http://localhost:8550 |
| User Service         | http://localhost:8666 |
| Booking Service      | http://localhost:8084 |
| Notification Service | http://localhost:8667 |

---

## Project Structure

```
mini-projet/
├── booking-service/          # Appointment booking management
├── catalogue-service/        # Catalogue management
├── common-libs/              # Shared utilities and DTOs
├── contract-test-support/    # Contract testing support
├── discovery-server/         # Eureka service registry
├── doctor-service/           # Doctor profiles (Reactive)
├── gateway-service/          # API Gateway
├── infrastructure/           # Docker configs (RabbitMQ)
├── logging-starter/          # Centralized logging
├── notification-service/     # Notification handling
├── security-service/         # JWT Authentication
└── user-service/             # User management
```

---

## Technology Stack

- **Spring Boot** 4.0.0
- **Spring Cloud** 2025.1.0
- **Java** 17
- **MySQL** 8.x
- **RabbitMQ** 3.x
- **Maven** for build management
