# MEDIQ – API & EVENTS

## 1. API Principles

API phải phản ánh business action.

Ưu tiên command-oriented API thay vì generic update.

Không sử dụng:

```text
PUT /appointments/{id}
```

để tùy ý thay đổi Doctor/Clinic/Room/Slot.

---

# 2. Doctor Service API

## Create Doctor

```http
POST /doctors
```

## Get Doctor

```http
GET /doctors/{id}
```

## List Doctors

```http
GET /doctors
```

## Update Doctor

```http
PATCH /doctors/{id}
```

## Add Specialty

```http
POST /doctors/{id}/specialties
```

## Remove Specialty

```http
DELETE /doctors/{id}/specialties/{specialtyId}
```

## Get Doctor Specialties

```http
GET /doctors/{id}/specialties
```

---

# 3. Clinic API

## Create Clinic

```http
POST /clinics
```

## Get Clinic

```http
GET /clinics/{id}
```

## List Clinics

```http
GET /clinics
```

## Update Clinic

```http
PATCH /clinics/{id}
```

## Change Clinic Status

```http
POST /clinics/{id}/activate
POST /clinics/{id}/deactivate
```

---

# 4. Specialty API

```http
POST /specialties
GET /specialties
GET /specialties/{id}
PATCH /specialties/{id}
```

---

# 5. Room API

## Create Room

```http
POST /clinics/{clinicId}/rooms
```

## Get Room

```http
GET /rooms/{id}
```

## List Rooms

```http
GET /clinics/{clinicId}/rooms
```

## Update Room

```http
PATCH /rooms/{id}
```

---

# 6. WorkSchedule API

## Create Schedule

```http
POST /work-schedules
```

## Get Schedule

```http
GET /work-schedules/{id}
```

## List Schedules

```http
GET /work-schedules
```

## Approve Schedule

```http
POST /work-schedules/{id}/approve
```

## Cancel Schedule

```http
POST /work-schedules/{id}/cancel
```

---

# 7. LeaveRequest API

## Create Leave Request

```http
POST /leave-requests
```

## Get Leave Request

```http
GET /leave-requests/{id}
```

## List Leave Requests

```http
GET /leave-requests
```

## Approve

```http
POST /leave-requests/{id}/approve
```

## Reject

```http
POST /leave-requests/{id}/reject
```

## Cancel

```http
POST /leave-requests/{id}/cancel
```

---

# 8. ScheduleChangeRequest API

## Create

```http
POST /schedule-change-requests
```

## Get

```http
GET /schedule-change-requests/{id}
```

## Approve

```http
POST /schedule-change-requests/{id}/approve
```

## Reject

```http
POST /schedule-change-requests/{id}/reject
```

## Cancel

```http
POST /schedule-change-requests/{id}/cancel
```

---

# 9. Slot API

## Get Slot

```http
GET /slots/{id}
```

## Find Available Slots

```http
GET /slots
```

## Hold Slot

```http
POST /slots/{id}/hold
```

## Book Slot

```http
POST /slots/{id}/book
```

## Release Slot

```http
POST /slots/{id}/release
```

Slot operations phải được validate theo lifecycle.

---

# 10. Appointment API

## Create Appointment

```http
POST /appointments
```

## Get Appointment

```http
GET /appointments/{id}
```

## List Appointments

```http
GET /appointments
```

## Confirm

```http
POST /appointments/{id}/confirm
```

## Complete

```http
POST /appointments/{id}/complete
```

## Cancel

```http
POST /appointments/{id}/cancel
```

## No-show

```http
POST /appointments/{id}/no-show
```

## Reschedule

```http
POST /appointments/{id}/reschedule
```

---

# 11. ReplacementProposal API

## Create Replacement Proposal

```http
POST /appointments/{appointmentId}/replacement-proposals
```

## Get Proposal

```http
GET /appointments/{appointmentId}/replacement-proposals/{proposalId}
```

## Accept

```http
POST /replacement-proposals/{proposalId}/accept
```

## Reschedule

```http
POST /replacement-proposals/{proposalId}/reschedule
```

## Cancel

```http
POST /replacement-proposals/{proposalId}/cancel
```

Không sử dụng:

```http
PUT /appointments/{id}
```

để reassignment tùy ý.

---

# 12. Replacement Accept API Flow

```text
POST /replacement-proposals/{proposalId}/accept
```

Server thực hiện:

```text
1. Load Proposal
2. Check status = PENDING
3. Check expiresAt
4. Revalidate proposed Slot
5. HOLD proposed Slot
6. Revalidate Appointment context
7. Reassign Appointment
8. BOOK new Slot
9. RELEASE old Slot
10. Proposal = ACCEPTED
11. Create AppointmentHistory
12. Publish events
13. Notify Patient
```

---

# 13. Replacement Failure

Nếu proposed Slot không còn available:

```text
Do not reassign Appointment
Do not mark Proposal ACCEPTED
Keep Appointment unchanged
Notify Patient
Offer another option
```

---

# 14. Appointment Events

```text
Appointment.Created
Appointment.Confirmed
Appointment.Cancelled
Appointment.Rescheduled
Appointment.Reassigned
```

---

# 15. Schedule Events

```text
Schedule.Approved
Schedule.Cancelled
```

---

# 16. Leave Events

```text
LeaveRequest.Approved
```

---

# 17. Replacement Events

```text
ReplacementProposal.Created
ReplacementProposal.Accepted
ReplacementProposal.Rescheduled
ReplacementProposal.Cancelled
ReplacementProposal.Expired
```

---

# 18. Event Envelope

```json
{
  "eventId": "uuid",
  "eventType": "Appointment.Reassigned",
  "occurredAt": "2026-09-17T10:00:00Z",
  "source": "appointment-service",
  "version": 1,
  "payload": {}
}
```

---

# 19. Event Idempotency

Consumer phải kiểm tra:

```text
eventId
```

để tránh xử lý duplicate event nhiều lần.

---

# 20. API Rules

API phải:

* validate input;
* validate business state;
* validate ownership;
* validate lifecycle transition;
* trả lỗi rõ ràng;
* không bypass domain rules.

Không expose API cho phép client tùy ý thay đổi business state.

---

# 21. Status Transition

## Appointment

```text
PENDING
   |
   v
CONFIRMED
   |
   +----> COMPLETED
   |
   +----> CANCELLED
   |
   +----> NO_SHOW
```

## ReplacementProposal

```text
PENDING
   |
   +----> ACCEPTED
   |
   +----> RESCHEDULED
   |
   +----> CANCELLED
   |
   +----> EXPIRED
```

## LeaveRequest

```text
PENDING
   |
   +----> APPROVED
   |
   +----> REJECTED
   |
   +----> CANCELLED
```

## ScheduleChangeRequest

```text
PENDING
   |
   +----> APPROVED
   |
   +----> REJECTED
   |
   +----> CANCELLED
```
