# MediApp Architecture Diagram

## System Architecture Overview

```mermaid
flowchart TB
    subgraph Client["🖥️ Client Layer"]
        WEB["Web Application"]
        MOBILE["Mobile App"]
        POSTMAN["Postman/API Client"]
    end

    subgraph Gateway["🚪 API Gateway Layer"]
        GW["Gateway Service<br/>:8550<br/>(Spring Cloud Gateway WebFlux)"]
    end

    subgraph Security["🔐 Security Layer"]
        SEC["Security Service<br/>:8085<br/>(JWT Authentication)"]
    end

    subgraph Discovery["📡 Service Discovery"]
        EUR["Discovery Server<br/>:8761<br/>(Eureka Server)"]
    end

    subgraph CoreServices["⚙️ Core Microservices"]
        USER["User Service<br/>:8081<br/>(REST API)"]
        DOC["Doctor Service<br/>:8082<br/>(REST API)"]
        BOOK["Booking Service<br/>:8083<br/>(REST API)"]
        NOTIF["Notification Service<br/>:8084<br/>(RabbitMQ Consumer)"]
    end

    subgraph Messaging["📨 Message Broker"]
        RMQ["RabbitMQ<br/>:5672"]
    end

    subgraph Databases["💾 Data Layer"]
        DB_USER[("mediapp_user<br/>MySQL")]
        DB_DOC[("mediapp_doctor<br/>MySQL")]
        DB_BOOK[("mediapp_booking<br/>MySQL")]
        DB_NOTIF[("mediapp_notification<br/>MySQL")]
        DB_SEC[("mediapp_security<br/>MySQL")]
    end

    %% Client to Gateway
    WEB --> GW
    MOBILE --> GW
    POSTMAN --> GW

    %% Gateway routes
    GW --> SEC
    GW --> USER
    GW --> DOC
    GW --> BOOK

    %% Service Discovery Registration
    GW -.->|registers| EUR
    SEC -.->|registers| EUR
    USER -.->|registers| EUR
    DOC -.->|registers| EUR
    BOOK -.->|registers| EUR
    NOTIF -.->|registers| EUR

    %% Inter-service Communication
    BOOK -->|"check/reserve slot"| DOC
    USER -->|"sync doctor info"| DOC

    %% RabbitMQ Communication
    BOOK -->|"publish events"| RMQ
    RMQ -->|"consume events"| NOTIF

    %% Database Connections
    USER --> DB_USER
    DOC --> DB_DOC
    BOOK --> DB_BOOK
    NOTIF --> DB_NOTIF
    SEC --> DB_SEC

    %% Styling
    classDef gateway fill:#ff9800,stroke:#e65100,color:#fff
    classDef security fill:#f44336,stroke:#c62828,color:#fff
    classDef discovery fill:#9c27b0,stroke:#6a1b9a,color:#fff
    classDef service fill:#2196f3,stroke:#1565c0,color:#fff
    classDef messaging fill:#4caf50,stroke:#2e7d32,color:#fff
    classDef database fill:#607d8b,stroke:#37474f,color:#fff
    classDef client fill:#00bcd4,stroke:#00838f,color:#fff

    class GW gateway
    class SEC security
    class EUR discovery
    class USER,DOC,BOOK,NOTIF service
    class RMQ messaging
    class DB_USER,DB_DOC,DB_BOOK,DB_NOTIF,DB_SEC database
    class WEB,MOBILE,POSTMAN client
```

---

## Booking Flow Sequence

```mermaid
sequenceDiagram
    autonumber
    participant C as 👤 Client
    participant GW as 🚪 Gateway
    participant SEC as 🔐 Security
    participant BOOK as 📅 Booking Service
    participant DOC as 👨‍⚕️ Doctor Service
    participant RMQ as 📨 RabbitMQ
    participant NOTIF as 🔔 Notification Service

    Note over C,NOTIF: Authentication Flow
    C->>GW: POST /login (credentials)
    GW->>SEC: Forward auth request
    SEC-->>GW: JWT Token
    GW-->>C: JWT Token

    Note over C,NOTIF: Booking Flow
    C->>GW: POST /api/v1/appointments/book<br/>(with JWT)
    GW->>GW: Validate JWT
    GW->>BOOK: Forward booking request
    BOOK->>DOC: GET /availability/{doctorId}
    DOC-->>BOOK: Available slots
    BOOK->>DOC: PUT /availability/{slotId}/reserve
    DOC-->>BOOK: Slot reserved
    BOOK->>BOOK: Persist appointment
    BOOK->>RMQ: Publish AppointmentCreatedEvent
    BOOK-->>GW: Appointment confirmation
    GW-->>C: Appointment confirmation

    Note over RMQ,NOTIF: Async Notification
    RMQ-->>NOTIF: Consume event
    NOTIF->>NOTIF: Send Email/SMS
    NOTIF->>NOTIF: Log notification
```

---

## Service Communication Matrix

```mermaid
graph LR
    subgraph Sync["Synchronous (HTTP/REST)"]
        direction TB
        A1["Gateway → All Services"]
        A2["Booking → Doctor Service"]
        A3["User → Doctor Service"]
    end

    subgraph Async["Asynchronous (RabbitMQ)"]
        direction TB
        B1["Booking Service<br/>(Producer)"]
        B2["Notification Service<br/>(Consumer)"]
        B1 -->|"appointment-created<br/>appointment-cancelled"| B2
    end

    subgraph Discovery["Service Discovery"]
        direction TB
        C1["All Services"]
        C2["Eureka Server"]
        C1 -.->|register/discover| C2
    end
```

---

## Database Schema Relationships

```mermaid
erDiagram
    APP_USER ||--o| PATIENT_PROFILE : has
    APP_USER ||--o| DOCTOR_PROFILE : has
    DOCTOR_PROFILE }o--|| SPECIALTY : belongs_to
    DOCTOR_PROFILE ||--o{ AVAILABILITY_SLOT : offers
    AVAILABILITY_SLOT ||--o| APPOINTMENT : reserved_by
    APPOINTMENT }o--|| APP_USER : patient
    APPOINTMENT }o--|| DOCTOR_PROFILE : doctor
    NOTIFICATION_LOG }o--|| APP_USER : recipient

    APP_USER {
        bigint user_id PK
        bigint auth_user_id UNIQUE
        varchar email
        varchar first_name
        varchar last_name
        enum user_role
    }

    PATIENT_PROFILE {
        bigint patient_id PK_FK
        varchar phone_number
        date date_of_birth
    }

    DOCTOR_PROFILE {
        bigint doctor_id PK_FK
        bigint user_id FK
        varchar medical_license_number
        int specialty_id FK
        varchar office_address
    }

    SPECIALTY {
        int specialty_id PK
        varchar name
    }

    AVAILABILITY_SLOT {
        bigint slot_id PK
        bigint doctor_id FK
        datetime start_time
        datetime end_time
        boolean is_reserved
    }

    APPOINTMENT {
        bigint appointment_id PK
        bigint patient_id
        bigint doctor_id
        bigint slot_id
        date appointment_date
        time start_time
        enum status
        datetime created_at
    }

    NOTIFICATION_LOG {
        bigint log_id PK
        bigint recipient_user_id
        enum message_type
        varchar topic
        boolean sent_success
        datetime sent_at
    }
```

---

## Technology Stack

```mermaid
mindmap
    root((MediApp))
        Infrastructure
            Eureka Discovery
            Spring Cloud Gateway
            RabbitMQ
        Security
            Spring Security
            JWT Tokens
        Core Services
            User Service
                Spring Boot
                REST API
                JPA
            Doctor Service
                Spring Boot
                REST API
                JPA
            Booking Service
                Spring Boot
                REST API
                RabbitMQ Producer
            Notification Service
                RabbitMQ Consumer
                Email/SMS
        Data
            MySQL
```

---

## Port Reference

| Service                   | Port  | Protocol |
| ------------------------- | ----- | -------- |
| Discovery Server (Eureka) | 8761  | HTTP     |
| Gateway Service           | 8550  | HTTP     |
| User Service              | 8081  | HTTP     |
| Doctor Service            | 8082  | HTTP     |
| Booking Service           | 8083  | HTTP     |
| Notification Service      | 8084  | HTTP     |
| Security Service          | 8085  | HTTP     |
| MySQL                     | 3306  | TCP      |
| RabbitMQ                  | 5672  | AMQP     |
| RabbitMQ Management       | 15672 | HTTP     |
