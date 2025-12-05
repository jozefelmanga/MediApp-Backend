# MediApp - Course Scope & Technology Guidelines

> ⚠️ **IMPORTANT FOR AI ASSISTANTS**: This document outlines the technologies and concepts covered in our Spring Boot Microservices course. When working on this project, **DO NOT** introduce features, patterns, or technologies beyond what is listed here.

---

## 📚 Course Overview: Spring Boot Microservices (7 TPs)

This project follows the curriculum of a university course on microservices architecture. The following TPs (Travaux Pratiques) define the scope of what should be used in this project.

---

## TP1: Introduction to Microservices

### Concepts Covered:

- Basic Spring Boot application setup
- REST API fundamentals
- `@RestController`, `@GetMapping`, `@PostMapping`, etc.
- `application.properties` / `application.yml` configuration
- Spring Boot DevTools

### Dependencies Allowed:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

## TP2: Development of Interconnected Microservices

### Concepts Covered:

- Creating multiple independent microservices
- Spring Data JPA for database access
- H2 in-memory database for development
- MySQL for production
- `RestTemplate` or `WebClient` for inter-service communication
- DTOs (Data Transfer Objects)
- Entity relationships

### Dependencies Allowed:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

### Patterns Used:

- Repository pattern (`@Repository`)
- Service layer (`@Service`)
- REST controllers (`@RestController`)

---

## TP3: API Gateway and Service Discovery with Spring Cloud

### Concepts Covered:

- **Eureka Server** for service discovery
- **Spring Cloud Gateway** for API routing
- Service registration with Eureka Client
- Dynamic routing through service discovery
- Load balancing with `lb://` prefix

### Dependencies Allowed:

#### For Eureka Server:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

#### For Eureka Client (all services):

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

#### For Gateway:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
```

### Configuration Examples:

```yaml
# Eureka Server
eureka:
  client:
    register-with-eureka: false
    fetch-registry: false

# Eureka Client
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

# Gateway Routes
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
      routes:
        - id: service-name
          uri: lb://SERVICE-NAME
          predicates:
            - Path=/api/**
```

---

## TP4: Securing Microservices with Spring Security and JWT

### Concepts Covered:

- Spring Security basics
- JWT (JSON Web Token) authentication
- Stateless authentication
- Password encoding with BCrypt
- Security filter chain configuration
- Token generation and validation
- Protecting REST endpoints

### Dependencies Allowed:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
</dependency>
```

### ⚠️ What is NOT Covered:

- ❌ Refresh tokens
- ❌ Token blacklisting
- ❌ OAuth2 / OpenID Connect
- ❌ Advanced role hierarchies

### Basic JWT Flow:

1. User sends credentials to `/login`
2. Server validates and returns JWT access token
3. Client includes token in `Authorization: Bearer <token>` header
4. Server validates token on each request

---

## TP5: Asynchronous Communication with RabbitMQ

### Concepts Covered:

- Message broker concepts
- RabbitMQ setup and configuration
- Publishing messages (Producer)
- Consuming messages (Consumer)
- Exchanges, Queues, and Routing Keys
- `@RabbitListener` annotation
- `RabbitTemplate` for sending messages

### Dependencies Allowed:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

### Configuration Example:

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

### Patterns Used:

- Event-driven architecture
- Publisher/Subscriber pattern
- Point-to-point messaging

---

## TP6: Contract Testing with Spring Cloud Contract

### Concepts Covered:

- Consumer-driven contract testing
- Spring Cloud Contract Verifier
- Contract DSL (Groovy or YAML)
- Stub generation for consumers
- Producer-side contract verification

### Dependencies Allowed:

```xml
<!-- Producer side -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-contract-verifier</artifactId>
    <scope>test</scope>
</dependency>

<!-- Consumer side -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-contract-stub-runner</artifactId>
    <scope>test</scope>
</dependency>
```

---

## TP7: Reactive Microservices with Spring WebFlux

### Concepts Covered:

- Reactive programming basics
- `Mono` and `Flux` types
- Spring WebFlux for non-blocking APIs
- R2DBC for reactive database access
- Reactive REST controllers
- `WebClient` for reactive HTTP calls

### Dependencies Allowed:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-r2dbc</artifactId>
</dependency>
<dependency>
    <groupId>io.asyncer</groupId>
    <artifactId>r2dbc-mysql</artifactId>
</dependency>
```

### Patterns Used:

- Reactive streams
- Non-blocking I/O
- Backpressure handling

---

## 🚫 Technologies NOT Covered (Do Not Use)

The following technologies are **NOT** part of the course and should **NOT** be introduced:

| Technology                                | Reason                |
| ----------------------------------------- | --------------------- |
| **Resilience4j / Circuit Breaker**        | Not covered in any TP |
| **Spring Cloud Config Server**            | Not covered           |
| **Refresh Tokens**                        | Only basic JWT in TP4 |
| **Token Blacklisting**                    | Not covered           |
| **OAuth2 / OpenID Connect**               | Not covered           |
| **Kubernetes / Docker Compose**           | Not covered           |
| **Kafka**                                 | Only RabbitMQ in TP5  |
| **Redis**                                 | Not covered           |
| **GraphQL**                               | Not covered           |
| **gRPC**                                  | Not covered           |
| **Sleuth / Zipkin (Distributed Tracing)** | Not covered           |
| **Prometheus / Grafana**                  | Not covered           |
| **Testcontainers**                        | Not covered           |
| **Custom Spring Boot Starters**           | Not covered           |
| **Shared Libraries (multi-module)**       | Not covered           |

---

## ✅ Allowed Patterns & Practices

### Architecture:

- Microservices architecture
- API Gateway pattern
- Service Discovery pattern
- Event-driven architecture (with RabbitMQ)

### Communication:

- Synchronous: REST APIs (HTTP)
- Asynchronous: RabbitMQ messaging

### Data:

- JPA with MySQL (blocking services)
- R2DBC with MySQL (reactive services)

### Security:

- Basic JWT authentication
- BCrypt password encoding
- Role-based access (simple)

### Testing:

- Unit tests with JUnit 5
- Integration tests
- Contract tests with Spring Cloud Contract

---

## 📁 Project Structure

```
mini-projet/
├── discovery-server/      # Eureka Server (TP3)
├── gateway-service/       # Spring Cloud Gateway (TP3)
├── security-service/      # JWT Authentication (TP4)
├── user-service/          # User management (TP2)
├── doctor-service/        # Doctor availability (TP2 - JPA/REST)
├── booking-service/       # REST + RabbitMQ producer (TP5)
└── notification-service/  # RabbitMQ consumer (TP5)
```

---

## 🎯 Summary for AI Assistants

When helping with this project:

1. **DO** use Spring Boot 3.x / Spring Cloud 2023.x
2. **DO** implement REST APIs with standard Spring MVC or WebFlux
3. **DO** use Eureka for service discovery
4. **DO** use Spring Cloud Gateway for routing
5. **DO** implement basic JWT authentication (access token only)
6. **DO** use RabbitMQ for async messaging
7. **DO** write contract tests with Spring Cloud Contract
8. **DO** use reactive programming where appropriate (optional for TP7 demonstration)

9. **DON'T** add circuit breakers (Resilience4j)
10. **DON'T** implement refresh tokens or token blacklisting
11. **DON'T** add OAuth2/OpenID Connect
12. **DON'T** introduce Kafka, Redis, or other message brokers
13. **DON'T** add distributed tracing (Sleuth/Zipkin)
14. **DON'T** create shared library modules
15. **DON'T** add Docker/Kubernetes configurations
16. **DON'T** use Testcontainers

---

_Last updated: December 2024_
_Course: Spring Boot Microservices - University Project_
