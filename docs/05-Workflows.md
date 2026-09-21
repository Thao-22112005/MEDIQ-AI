# MEDIQ – WORKFLOWS

## WF-01 – Create and Approve WorkSchedule

### Input

```text
Doctor
Clinic
Specialty
Room
Date
StartTime
EndTime
```

### Flow

```text
Create WorkSchedule
        ↓
Validate Doctor Specialty
        ↓
Validate Room exists
        ↓
Validate Room belongs to Clinic
        ↓
Validate Room Specialty
        ↓
Check Doctor overlap
        ↓
Check Room overlap
        ↓
Create Schedule
        ↓
Approve Schedule
        ↓
Generate Slots
```

### Result

WorkSchedule hợp lệ và Slot được tạo.

---

# WF-02 – Book Appointment

```text
Patient selects Slot
        ↓
Validate Slot
        ↓
Check Slot = AVAILABLE
        ↓
HOLD Slot
        ↓
Create Appointment PENDING
        ↓
Confirm Appointment
        ↓
BOOK Slot
        ↓
Appointment = CONFIRMED
```

Nếu bước sau thất bại:

```text
Compensation
```

để tránh inconsistent state.

---

# WF-03 – Doctor Leave

Khi LeaveRequest được APPROVED:

```text
LeaveRequest.Approved
        ↓
Find affected WorkSchedule
        ↓
Find affected Appointment
```

## Case A – Không có Appointment

```text
Cancel affected Schedule
        ↓
Handle related Slot
```

## Case B – Có Appointment

```text
Do not delete Appointment
        ↓
Find Replacement Doctor
        ↓
Create ReplacementProposal
        ↓
Notify Patient
```

---

# WF-04 – Find Replacement Doctor

Tiêu chí:

```text
1. Same Specialty
2. Doctor Active
3. Professional scope phù hợp
4. Không Schedule overlap
5. Có Schedule/Slot phù hợp
```

Clinic không phải điều kiện bắt buộc.

Replacement Doctor có thể ở Clinic khác.

---

# WF-05 – Create ReplacementProposal

```text
Affected Appointment
        ↓
Find Replacement Doctor
        ↓
Find suitable Slot
        ↓
Create ReplacementProposal
        ↓
status = PENDING
        ↓
expiresAt = createdAt + 24h
        ↓
Notify Patient
```

Không HOLD proposed Slot.

---

# WF-05A – Patient ACCEPT

```text
Patient ACCEPT
        ↓
Load Proposal
        ↓
Check PENDING
        ↓
Check expiresAt
        ↓
Revalidate proposed Slot
        ↓
Slot AVAILABLE?
       / \
     NO   YES
     |     |
     |     v
     |   HOLD Slot
     |     ↓
     |   Revalidate Context
     |     ↓
     |   Reassign Appointment
     |     ↓
     |   BOOK new Slot
     |     ↓
     |   Release old Slot
     |     ↓
     |   Proposal = ACCEPTED
     |     ↓
     |   History
     |     ↓
     |   Event
     |
     v
Appointment unchanged
     ↓
Notify Patient
     ↓
Offer another option
```

---

# WF-05B – Patient RESCHEDULE

```text
Patient RESCHEDULE
        ↓
Check Proposal = PENDING
        ↓
Proposal = RESCHEDULED
        ↓
Normal Reschedule Flow
```

Reschedule flow:

```text
Find new Slot
        ↓
HOLD
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

---

# WF-05C – Patient CANCEL

```text
Patient CANCEL
        ↓
Check Proposal = PENDING
        ↓
Proposal = CANCELLED
```

Appointment không tự động CANCEL nếu business flow không yêu cầu.

Nếu Patient muốn cancel Appointment, sử dụng Appointment Cancel flow.

---

# WF-05D – Proposal Expired

Sau 24 giờ:

```text
PENDING
   ↓
EXPIRED
```

Appointment:

```text
UNCHANGED
```

Không:

```text
AUTO REASSIGN
AUTO CANCEL
```

Admin/Manager xử lý tiếp.

---

# WF-06 – Appointment Reschedule

```text
Patient/Admin requests reschedule
        ↓
Find new Slot
        ↓
Validate new Slot
        ↓
HOLD new Slot
        ↓
BOOK new Slot
        ↓
Release old Slot
        ↓
Update Appointment
        ↓
Create History
        ↓
Publish Appointment.Rescheduled
        ↓
Notify
```

---

# WF-07 – Appointment Cancel

```text
Cancel request
        ↓
Validate Appointment state
        ↓
Appointment = CANCELLED
        ↓
Release Slot
        ↓
Create History
        ↓
Publish Appointment.Cancelled
        ↓
Notify
```

---

# WF-08 – Schedule Change

```text
ScheduleChangeRequest
        ↓
PENDING
        ↓
Admin/Manager review
        ↓
APPROVED
        ↓
Validate new Schedule
        ↓
Find affected Slot
        ↓
Find affected Appointment
```

Nếu không có Appointment:

```text
Update Schedule
```

Nếu có Appointment:

```text
Do not blindly update
        ↓
Handle affected Appointment
        ↓
Reschedule / Replacement / Admin action
```

---

# WF-09 – Multi-Clinic Replacement

Ví dụ:

```text
Original:
Doctor A
Clinic 1
Room 101
Cardiology
10:00
```

Doctor A nghỉ.

System tìm:

```text
Doctor B
Clinic 2
Room 205
Cardiology
10:00
```

Proposal:

```text
originalDoctorId = Doctor A
proposedDoctorId = Doctor B
proposedClinicId = Clinic 2
proposedRoomId = Room 205
```

Patient phải ACCEPT trước khi reassignment.

---

# WF-10 – No Hard Delete

Các entity đã có business activity không bị hard delete.

Thay vào đó:

```text
status
history
audit
```

được sử dụng để bảo toàn lịch sử.

---

# WF-11 – Event Handling

Khi business operation hoàn thành:

```text
Business Action
      ↓
Update State
      ↓
Create History
      ↓
Publish Event
```

Consumer:

```text
Receive Event
      ↓
Check eventId
      ↓
Ignore duplicate if already processed
      ↓
Process
```

---

# WF-12 – Conflict Handling

Nếu code hiện tại khác Business Rules:

```text
STOP
 ↓
Identify conflict
 ↓
Identify affected rule
 ↓
Document conflict
 ↓
Confirm decision
 ↓
Implement
 ↓
Update documentation
```

Không tự thay đổi business rule chỉ để phù hợp với code.
