# MediApp Messaging Infrastructure

This module provisions a local RabbitMQ instance with the baseline messaging topology for MediApp.

## Topology

- Exchange `appointment.events` (topic) routes booking life-cycle events.
- Exchange `appointment.events.dlx` (fanout) collects failed deliveries for inspection.
- Queue `appointment-created` receives events with routing key `appointment.created`.
- Queue `appointment-cancelled` receives events with routing key `appointment.cancelled`.
- Queue `appointment-dead-letter` stores messages that could not be processed downstream.

## Local Usage

1. From the repository root run `docker compose -f infrastructure/messaging/docker-compose.messaging.yml up -d`.
2. Access the management console at `http://localhost:15672` (user `mediapp`, password `mediapp`).
3. Publish test messages via the management UI or service integrations.
4. Tear down the stack with `docker compose -f infrastructure/messaging/docker-compose.messaging.yml down`.

## Message Contracts

- `AppointmentCreated`
  - Routing key `appointment.created`.
  - TBD schema: will include appointment ID, slot ID, patient ID, doctor ID, booking timestamps.
- `AppointmentCancelled`
  - Routing key `appointment.cancelled`.
  - TBD schema: will include appointment ID, cancel reason, cancelled timestamp, actor details.

Schema definitions will be codified once the booking and notification services are implemented. Include versioning metadata (`eventVersion`, `occurredAt`) in every contract.
