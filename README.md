# MediApp - Backend (Microservices)

This repository contains the backend microservices for MediApp — a medical appointment booking system built with Spring Boot, Spring Cloud and RabbitMQ.

**Quick facts:**

- **Backend:** Spring Boot 4.x, Spring Cloud 2025.1.x, Java 17
- **Databases:** MySQL 8.x (each service uses its own schema)
- **Messaging:** RabbitMQ 3.x (local Docker Compose available)

**Project authors:** Youssef Ben Salem, Seifeddine Boudoukhane

---

**Services & Default Ports**

| Service                | Default Port | Database Schema        | Description                            |
| ---------------------- | -----------: | ---------------------- | -------------------------------------- |
| `discovery-server`     |       `8761` | none                   | Eureka service registry                |
| `gateway-service`      |       `8550` | none                   | API Gateway (WebFlux)                  |
| `security-service`     |       `8085` | `mediapp_security`     | JWT authentication & auth management   |
| `user-service`         |       `8081` | `mediapp_user`         | Users & patient profiles               |
| `doctor-service`       |       `8082` | `mediapp_doctor`       | Doctors, specialties, availability     |
| `booking-service`      |       `8083` | `mediapp_booking`      | Appointment booking & event publishing |
| `notification-service` |       `8084` | `mediapp_notification` | Notification consumer (email/SMS)      |

> Note: Ports are configurable in each service's `application.properties` / `application.yml`.

---

**Prerequisites**

- **Java 17+**
- **Maven 3.8+**
- **MySQL 8.x** (or any compatible MySQL instance; create required schemas)
- **Docker & Docker Compose** (recommended for local RabbitMQ)

---

**Run Locally (recommended sequence)**

1. Start MySQL (for example via XAMPP or your preferred local MySQL).

2. Start RabbitMQ (local Docker):

```bash
cd <repo-root>
docker compose up -d
# Verify management UI: http://localhost:15672  (guest/guest)
```

3. Start the Discovery Server (Eureka):

```bash
cd discovery-server
./mvnw spring-boot:run
```

4. Start the Gateway:

```bash
cd gateway-service
./mvnw spring-boot:run
```

5. Start each microservice in this order (or in separate terminals):

```bash
cd user-service
./mvnw spring-boot:run

cd doctor-service
./mvnw spring-boot:run

cd booking-service
./mvnw spring-boot:run

cd notification-service
./mvnw spring-boot:run

cd security-service
./mvnw spring-boot:run
```

On Windows with `bash.exe` you can run the same `./mvnw` commands from Git Bash or WSL.

6. Optional: enable mock-data seeding (if available in a service):

```properties
# Add to service run command or application.properties
-Dapp.seed.enabled=true
```

---

**Docker Compose (RabbitMQ)**

The repository includes a `docker-compose.yml` that starts a local RabbitMQ with management UI and persistent volume. Default credentials are `guest` / `guest`.

- Start RabbitMQ: `docker compose up -d`
- Management UI: `http://localhost:15672` (user: `guest`, pass: `guest`)

The compose file maps `5672:5672` (AMQP) and `15672:15672` (management).

---

**Postman & API Testing**

- Postman collections are stored in the `postman/` folder:
  - `postman/MediApp_Gateway_Collection.json`
  - `postman/MediApp_API_Collection.json`
- Import the gateway collection and set the `gateway_url` variable to `http://localhost:8550`.
- The collection includes automated test scripts that set `access_token`, `refresh_token`, and entity IDs during the workflow.

---

**Useful Commands & Tips**

- Build a service: `cd <service> && ./mvnw clean package`
- Run jar directly: `java -jar target/<service>-0.0.1-SNAPSHOT.jar`
- Troubleshooting: if a port is in use, change `server.port` in the service's `application.properties`.

---

**Where to find things**

- `postman/` : Postman collections for gateway and services
- Each microservice folder contains its own `README` and `HELP.md` with service-specific notes
- `docker-compose.yml` : local RabbitMQ configuration

---

If you'd like, I can also:

- add Dockerfiles and compose services to run all microservices together
- add a sample `docker-compose` that brings up MySQL + RabbitMQ + all services
- generate a short troubleshooting guide for Windows-specific Java/Maven issues

---

License: educational/demo use only — not production-ready.

### Mock Data Seeding

- Flip `app.seed.enabled=true` (application property or CLI argument) to activate each service's `CommandLineRunner` seeder.
- Fixture IDs/passwords are documented in `develoment requirements/mock-data.yaml` so all microservices stay consistent.
- Quick commands:

  ```bash
  # user-service (creates admin + sample patients)
  cd user-service
  ./mvnw spring-boot:run -Dspring-boot.run.arguments="--app.seed.enabled=true"

  # doctor-service (populates specialty catalog)
  cd ../doctor-service
  ./mvnw spring-boot:run -Dspring-boot.run.arguments="--app.seed.enabled=true"
  ```

- Once seeded, rerun services without the flag for normal operation—the data remains in MySQL.

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
├── discovery-server/         # Eureka service registry
├── doctor-service/           # Doctor profiles (Reactive)
├── gateway-service/          # API Gateway
├── infrastructure/           # Docker configs (RabbitMQ)
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

---

