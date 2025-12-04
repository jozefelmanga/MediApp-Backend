# MediApp Logging Starter

Auto-configuration for correlation IDs, structured request logging, and exception translation.

## Features

- Injects an `X-Correlation-Id` header and propagates MDC entries for log correlation.
- Adds a lightweight interceptor that logs incoming requests and response status codes.
- Converts exceptions into the shared `ErrorResponse` envelope while honouring localization via Spring `MessageSource`.

## Usage

1. Publish the starter to your Maven repository (`mvn install`).
2. Add the dependency to a service module:

```xml
<dependency>
    <groupId>com.mediapp</groupId>
    <artifactId>logging-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

Optional properties:

```properties
mediapp.logging.correlation-header=X-Request-Id
mediapp.logging.log-inbound-requests=false
```
