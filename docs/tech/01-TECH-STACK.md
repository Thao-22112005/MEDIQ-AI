# MEDIQ – TECH STACK

## 1. Purpose

This document defines the approved technology stack, development tools, libraries, coding conventions, testing technologies, and infrastructure for MEDIQ.

This document is the source of truth for technical implementation.

AI agents must follow this document unless an explicit technical decision changes it.

---

# 2. Core Stack

| Category              | Technology                                    |
| --------------------- | --------------------------------------------- |
| Programming Language  | Java 21                                       |
| Framework             | Spring Boot 3.5.x                             |
| Build Tool            | Maven                                         |
| Web Framework         | Spring Web / Spring MVC                       |
| ORM                   | Spring Data JPA + Hibernate                   |
| Database              | MySQL 8.4 LTS                                 |
| Database Migration    | Flyway                                        |
| Validation            | Jakarta Bean Validation / Hibernate Validator |
| DTO Mapping           | MapStruct                                     |
| Boilerplate Reduction | Lombok                                        |
| JSON                  | Jackson                                       |
| API Documentation     | SpringDoc OpenAPI / Swagger UI                |
| Logging               | SLF4J + Logback                               |
| Testing               | JUnit 5 + Mockito + Spring Boot Test          |
| Integration Testing   | Testcontainers                                |
| Containerization      | Docker + Docker Compose                       |
| HTTP Communication    | REST / JSON                                   |
| Security              | Spring Security + JWT                         |
| Cache                 | Redis, only when required                     |
| Version Control       | Git                                           |
| IDE                   | IntelliJ IDEA                                 |

---

# 3. Java

The project uses:

```text
Java 21
```

Use modern Java features when they improve readability and maintainability.

Preferred:

```text
java.time.*
UUID
enum
record
```

Avoid legacy date/time APIs such as:

```text
java.util.Date
java.sql.Date
```

for new domain code.

---

# 4. Spring Boot

The project uses:

```text
Spring Boot 3.5.x
```

All services should use the same Spring Boot major/minor line unless a documented technical decision requires otherwise.

Do not upgrade or downgrade Spring Boot independently for one service without documenting the reason.

---

# 5. Build Tool

The project uses:

```text
Maven
```

Each microservice should have its own Maven project unless the repository structure explicitly defines another valid arrangement.

---

# 6. Web Layer

Use:

```text
Spring Web
Spring MVC
Jackson
```

API communication format:

```text
HTTP + JSON
```

Controllers should:

* Accept Request DTOs.
* Return Response DTOs.
* Delegate business logic to services.
* Not contain complex business logic.
* Not access repositories directly.

---

# 7. Persistence

The persistence stack is:

```text
Spring Data JPA
Hibernate
MySQL 8.4 LTS
```

Each microservice owns its own database.

Current databases:

```text
doctor_db
scheduling_db
appointment_db
```

Cross-service database access is forbidden.

Do not create database foreign keys between databases owned by different services.

---

# 8. JPA Rules

Use JPA entities only inside the service that owns them.

Cross-service references must use IDs.

Example:

```java
private UUID doctorId;
```

Do not create cross-service JPA relationships such as:

```java
@ManyToOne
private Doctor doctor;
```

when Doctor belongs to another microservice.

Prefer explicit repository queries and clear transaction boundaries.

---

# 9. Database Migration

Use:

```text
Flyway
```

Database schema changes must be versioned through migrations.

Example:

```text
db/migration/

V1__create_doctor.sql
V2__create_specialty.sql
V3__create_doctor_specialty.sql
```

Do not use Hibernate automatic schema update as the primary database migration mechanism.

Production-like environments must use versioned migrations.

---

# 10. Validation

Use:

```text
Jakarta Bean Validation
Hibernate Validator
```

Use annotations for structural validation:

```text
@NotNull
@NotBlank
@Size
@Email
@Positive
```

Business rules must be validated inside the appropriate service/domain logic.

Examples:

```text
Doctor specialty compatibility
Schedule overlap
Room specialty compatibility
Room clinic compatibility
Slot availability
Appointment state transition
Replacement proposal expiration
```

Do not attempt to implement complex business rules using DTO annotations alone.

---

# 11. DTO Mapping

Use:

```text
MapStruct
```

Controllers should not expose JPA entities directly.

Preferred flow:

```text
Request DTO
    ↓
Controller
    ↓
Application/Domain Service
    ↓
Entity
    ↓
Repository
```

Response:

```text
Entity
    ↓
MapStruct
    ↓
Response DTO
    ↓
Controller
```

---

# 12. Lombok

Lombok may be used to reduce boilerplate.

Preferred use cases:

```text
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Slf4j
```

Do not use Lombok in a way that hides important business behavior or makes the domain difficult to understand.

---

# 13. API Documentation

Use:

```text
SpringDoc OpenAPI
Swagger UI
```

Every public API should have meaningful:

* HTTP method
* endpoint
* request DTO
* response DTO
* status codes
* validation/error behavior

---

# 14. Exception Handling

Each service should have centralized API exception handling.

Preferred:

```text
@RestControllerAdvice
```

Errors should use a consistent structure.

Example:

```json
{
  "timestamp": "2026-09-17T10:00:00Z",
  "status": 400,
  "code": "ROOM_SPECIALTY_MISMATCH",
  "message": "Room specialty does not match schedule specialty",
  "path": "/work-schedules"
}
```

Business exceptions should have meaningful error codes.

---

# 15. Communication

Synchronous communication:

```text
REST / HTTP / JSON
```

Asynchronous communication:

```text
Domain/Application Events
```

Event contracts are defined in:

```text
docs/04-API-EVENTS.md
```

A message broker is NOT mandatory at the current stage.

Do not introduce Kafka, RabbitMQ, Pulsar, or another broker unless a concrete requirement exists.

---

# 16. Event Architecture

Event envelope:

```text
eventId
eventType
occurredAt
source
version
payload
```

Consumers must be designed to tolerate duplicate events.

Event handling should be idempotent where applicable.

Do not introduce Event Sourcing or CQRS unless explicitly required.

---

# 17. Security

The intended security stack is:

```text
Spring Security
JWT
```

However:

```text
Identity/Auth Service
```

is currently outside the active implementation scope.

Current services should not independently implement separate authentication systems.

Security integration should be designed to integrate with the future Identity/Auth Service.

Do not invent authentication flows that are not defined by project requirements.

---

# 18. Redis

Redis is an optional infrastructure component.

Redis must only be introduced when there is a concrete requirement such as:

```text
Caching
Temporary state
Distributed locking
Performance optimization
```

Do not add Redis to every service by default.

---

# 19. Testing

Required testing technologies:

```text
JUnit 5
Mockito
Spring Boot Test
Testcontainers
```

Testing layers:

```text
Unit Tests
Integration Tests
API Tests
Cross-service workflow tests where required
```

Critical MEDIQ business flows must have automated tests.

Especially:

```text
Doctor schedule conflict
Room schedule conflict
Slot booking
Appointment lifecycle
Leave approval
Replacement Doctor selection
Replacement Proposal
Replacement acceptance
Replacement expiration
Appointment reschedule
Cross-clinic replacement
```

---

# 20. Docker

Use:

```text
Docker
Docker Compose
```

Docker Compose may be used for local development infrastructure and multi-service testing.

Do not introduce Kubernetes unless there is a specific deployment requirement.

---

# 21. Logging

Use:

```text
SLF4J
Logback
```

Logs should contain enough context to diagnose business operations.

Avoid logging:

```text
Passwords
JWT tokens
Sensitive patient information
Secrets
```

---

# 22. Project Structure

Preferred Spring Boot service structure:

```text
src/main/java/com/mediq/<service>/

├── controller/
├── dto/
│   ├── request/
│   └── response/
├── entity/
├── repository/
├── service/
├── mapper/
├── validation/
├── exception/
├── event/
│   ├── model/
│   └── publisher/
├── client/
├── config/
└── <Service>Application.java
```

Do not create empty packages without a reason.

Not every service must contain every package.

---

# 23. Coding Principles

Priority:

```text
Correctness
>
Simplicity
>
Testability
>
Maintainability
>
Performance optimization
```

Avoid unnecessary:

```text
Design patterns
Abstractions
Interfaces
Frameworks
Infrastructure
Microservices
Distributed transactions
```

Use the simplest implementation that correctly satisfies the Business Rules.

---

# 24. Forbidden Technical Decisions Without Approval

AI agents must not introduce the following without explicit approval:

```text
Kafka
RabbitMQ
Pulsar
Kubernetes
CQRS
Event Sourcing
Saga framework
Distributed transaction framework
Service mesh
GraphQL
NoSQL database
Additional microservices
Additional databases
```

unless a documented requirement makes them necessary.

---

# 25. Cross-Service Database Rule

Absolutely forbidden:

```text
Service A
    ↓
direct database access
    ↓
Service B database
```

Services communicate through:

```text
REST
Events
```

Cross-service entity relationships are represented by IDs.

---

# 26. Technical Decision Rule

If an AI agent believes a new technology or architectural pattern is necessary:

1. Stop before introducing it.
2. Explain the problem.
3. Explain why the existing stack cannot solve it adequately.
4. Explain the proposed technology.
5. Explain its complexity and maintenance cost.
6. Ask for approval.

Do not silently expand the technology stack.

---

# 27. Source of Truth

Technical decisions are governed by:

```text
docs/tech/01-TECH-STACK.md
```

Business decisions are governed by:

```text
docs/01-BUSINESS-RULES.md
docs/06-DECISIONS.md
```

Architecture is governed by:

```text
docs/03-ARCHITECTURE.md
```

AI handover state is governed by:

```text
docs/ai/
```

If source code conflicts with these documents, report the conflict before making architectural or business-rule changes.

---

# 28. Final Technical Baseline

MEDIQ currently uses:

```text
Java 21
Spring Boot 3.5.x
Maven
Spring Web / MVC
Spring Data JPA
Hibernate
MySQL 8.4 LTS
Flyway
Jakarta Validation
MapStruct
Lombok
Jackson
SpringDoc OpenAPI
SLF4J + Logback
JUnit 5
Mockito
Spring Boot Test
Testcontainers
Docker
Docker Compose
Spring Security + JWT
Redis (optional)
REST
Events
Git
```

The project intentionally avoids unnecessary infrastructure and architectural complexity.

Any future technology change must be documented as a technical decision before implementation.
