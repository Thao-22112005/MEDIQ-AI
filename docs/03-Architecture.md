# MEDIQ – ARCHITECTURE

## 1. Architecture Overview

MEDIQ sử dụng kiến trúc microservices.

Trong phạm vi phát triển hiện tại:

```text
                         Client
                           |
                           v
                     API Gateway
                           |
          +----------------+----------------+
          |                |                |
          v                v                v
   Doctor Service   Clinic/Scheduling   Appointment
                         Service          Service
          |                |                |
          v                v                v
      doctor_db       scheduling_db   appointment_db
```

Patient Service và Identity/Auth Service thuộc hệ thống tổng thể nhưng ngoài phạm vi implementation hiện tại.

---

# 2. Service Responsibility

## 2.1 Doctor Service

Responsible for:

* Doctor
* DoctorSpecialty
* Doctor information
* Doctor Specialty management

Không chịu trách nhiệm:

* Clinic
* Room
* WorkSchedule
* Slot
* Appointment

---

# 3. Clinic/Scheduling Service

Responsible for:

* Clinic
* Specialty
* Room
* WorkSchedule
* Slot
* LeaveRequest
* ScheduleChangeRequest

Service này chịu trách nhiệm xác định:

```text
Doctor làm việc ở đâu
Doctor làm việc khi nào
Room nào được sử dụng
Slot nào AVAILABLE
```

---

# 4. Appointment Service

Responsible for:

* Appointment
* Appointment lifecycle
* ReplacementProposal
* AppointmentHistory

Appointment Service không sở hữu Slot.

Slot thuộc Scheduling Service.

---

# 5. Database Ownership

Mỗi service sở hữu database riêng:

```text
doctor_db
scheduling_db
appointment_db
```

Không cross-service database access.

Không tạo database FK:

```text
appointment_db -> doctor_db
appointment_db -> scheduling_db
```

---

# 6. Communication

## 6.1 REST

REST được sử dụng cho synchronous operations.

Ví dụ:

```text
Appointment Service
        |
        | REST
        v
Scheduling Service
        |
        v
Check/Hold/Book/Release Slot
```

---

## 6.2 Events

Events được sử dụng cho asynchronous business changes.

Ví dụ:

```text
LeaveRequest.Approved
        |
        v
Appointment Service
        |
        v
Find affected Appointment
        |
        v
Create ReplacementProposal
```

---

# 7. Event List

Minimal event set:

```text
Schedule.Approved
Schedule.Cancelled

LeaveRequest.Approved

Appointment.Created
Appointment.Confirmed
Appointment.Cancelled
Appointment.Rescheduled
Appointment.Reassigned

ReplacementProposal.Created
ReplacementProposal.Accepted
ReplacementProposal.Rescheduled
ReplacementProposal.Cancelled
ReplacementProposal.Expired
```

Không tạo event nếu chưa có business requirement.

---

# 8. Event Envelope

Mỗi event nên có:

```text
eventId
eventType
occurredAt
source
version
payload
```

Ví dụ:

```json
{
  "eventId": "uuid",
  "eventType": "Appointment.Reassigned",
  "occurredAt": "2026-09-17T10:00:00Z",
  "source": "appointment-service",
  "version": 1,
  "payload": {
    "appointmentId": "uuid",
    "oldDoctorId": "uuid",
    "newDoctorId": "uuid",
    "oldClinicId": "uuid",
    "newClinicId": "uuid"
  }
}
```

---

# 9. Event Idempotency

Consumer phải có khả năng xử lý event idempotently.

Nếu cùng một event được nhận nhiều lần:

```text
Event #123
Event #123
```

business state không được bị thay đổi sai.

---

# 10. Replacement Architecture

ReplacementProposal thuộc:

```text
Appointment Service
```

Slot thuộc:

```text
Clinic/Scheduling Service
```

Khi Patient ACCEPT:

```text
Patient
   |
   v
Appointment Service
   |
   | validate proposal
   |
   v
Scheduling Service
   |
   | revalidate Slot
   | HOLD Slot
   |
   v
Appointment Service
   |
   | reassign Appointment
   |
   v
Scheduling Service
   |
   | BOOK new Slot
   | RELEASE old Slot
```

---

# 11. Replacement Slot Policy

Không HOLD proposed Slot trong 24 giờ.

Lý do:

```text
ReplacementProposal = Offer
Slot = Actual Capacity
```

Proposal chỉ reference:

```text
proposedSlotId
```

Khi Patient ACCEPT mới:

```text
revalidate
→ HOLD
→ reassign
→ BOOK
```

---

# 12. Distributed Transaction Policy

Không triển khai Saga/Distributed Transaction Framework nếu chưa có requirement.

Thay vào đó sử dụng:

* deterministic operation order;
* validation;
* compensation;
* idempotency;
* event/history.

---

# 13. Booking Consistency

Booking flow:

```text
Validate Slot
      ↓
HOLD Slot
      ↓
Create Appointment PENDING
      ↓
Confirm Appointment
      ↓
BOOK Slot
```

Nếu bước sau thất bại:

```text
compensate
```

để tránh:

```text
Appointment tồn tại nhưng Slot AVAILABLE
```

hoặc:

```text
Slot BOOKED nhưng Appointment không tồn tại
```

---

# 14. Reschedule Consistency

Reschedule:

```text
Find new Slot
      ↓
HOLD new Slot
      ↓
Validate
      ↓
BOOK new Slot
      ↓
Release old Slot
      ↓
Update Appointment
      ↓
History
      ↓
Event
```

Nếu failure phải xử lý compensation.

---

# 15. Architecture Principles

Ưu tiên:

```text
Correctness
    >
Simplicity
    >
Testability
    >
Maintainability
```

Không over-engineering.

Không tự ý thêm:

```text
Kafka
RabbitMQ
Outbox
Saga
CQRS
Event Sourcing
Kubernetes
```

nếu chưa có requirement.

---

# 16. Source of Truth

Khi code và tài liệu conflict:

```text
Business Rules
      ↓
Decisions
      ↓
Domain Model
      ↓
Architecture
      ↓
API & Events
      ↓
Workflows
      ↓
Current Code
```

Không sửa Business Rules chỉ để code dễ hơn.

Nếu code khác tài liệu:

1. Xác định conflict.
2. Báo rõ conflict.
3. Xác định rule liên quan.
4. Chỉ thay đổi sau khi decision được xác nhận.
