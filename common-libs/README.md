# MediApp Common Libraries

Shared DTOs, error handling constructs, and auditing support reused across MediApp microservices.

## Features

- Canonical API response and pagination DTOs.
- Consistent error model with `ErrorCode`, `DomainException`, and `ErrorResponse` utilities.
- JPA auditing base entity plus auto-configuration to enable auditing with a default auditor.

## Usage

Add the dependency to any service:

```xml
<dependency>
    <groupId>com.mediapp</groupId>
    <artifactId>common-libs</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

Override the `mediapp.auditing.enabled` property or provide a custom `AuditorAware` bean whenever advanced auditing is necessary.
