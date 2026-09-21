# MEDIQ – DOMAIN MODEL

## 1. Overview

MEDIQ hiện có ba business service trong phạm vi phát triển:

* Doctor Service
* Clinic/Scheduling Service
* Appointment Service

Patient Service và Identity/Auth Service thuộc hệ thống nhưng ngoài phạm vi phát triển hiện tại.

---

# 2. Doctor Service

## 2.1 Doctor

```text
Doctor
-------------------------
id
userId
fullName
licenseNumber
status
createdAt
updatedAt
```

### Relationships

```text
Doctor 1 ---- N DoctorSpecialty
```

Doctor không có trực tiếp:

```text
clinicId
roomId
```

Clinic/Room của Doctor được xác định thông qua WorkSchedule.

---

## 2.2 DoctorSpecialty

```text
DoctorSpecialty
-------------------------
doctorId
specialtyId
isPrimary
createdAt
```

Rules:

* Một Doctor có nhiều Specialty.
* Một Doctor tối đa một Specialty có `isPrimary = true`.
* `specialtyId` tham chiếu Specialty thuộc Scheduling Service.

---

# 3. Clinic/Scheduling Service

## 3.1 Clinic

```text
Clinic
-------------------------
id
code
name
address
phone
status
createdAt
updatedAt
```

Relationships:

```text
Clinic 1 ---- N Room
Clinic 1 ---- N WorkSchedule
```

---

## 3.2 Specialty

```text
Specialty
-------------------------
id
name
description
status
defaultSlotDuration
createdAt
updatedAt
```

Specialty được sử dụng để:

* xác định chuyên môn của Doctor;
* xác định chuyên khoa của Room;
* xác định chuyên khoa của WorkSchedule;
* tìm Replacement Doctor.

---

## 3.3 Room

```text
Room
-------------------------
id
clinicId
code
name
specialtyId
status
createdAt
updatedAt
```

Rules:

* Room thuộc đúng một Clinic.
* Room được cấu hình cho một Specialty.
* Room có thể được sử dụng bởi nhiều WorkSchedule theo các thời điểm khác nhau.
* Hai WorkSchedule của cùng Room không được overlap.

---

## 3.4 WorkSchedule

```text
WorkSchedule
-------------------------
id
doctorId
clinicId
specialtyId
roomId
date
startTime
endTime
status
createdAt
updatedAt
```

### Meaning

WorkSchedule biểu diễn:

> Doctor làm việc tại Clinic nào, Room nào, Specialty nào, vào thời gian nào.

### Rules

```text
doctorId
clinicId
specialtyId
roomId
date
startTime
endTime
```

phải tạo thành một Schedule hợp lệ.

Validation:

```text
Room.clinicId == WorkSchedule.clinicId

Room.specialtyId == WorkSchedule.specialtyId

Doctor có Specialty phù hợp

Doctor Schedule không overlap

Room Schedule không overlap
```

---

# 4. Slot

```text
Slot
-------------------------
id
scheduleId
startTime
endTime
status
createdAt
```

Status:

```text
AVAILABLE
HELD
BOOKED
BLOCKED
EXPIRED
```

Slot thuộc Scheduling Service.

Slot được tạo dựa trên WorkSchedule hợp lệ.

Past Slot không được booking.

---

# 5. LeaveRequest

```text
LeaveRequest
-------------------------
id
doctorId
startDateTime
endDateTime
reason
status
reviewedBy
reviewedAt
createdAt
updatedAt
```

Status:

```text
PENDING
APPROVED
REJECTED
CANCELLED
```

Doctor tạo LeaveRequest.

Admin/Manager hoặc role được phân quyền review.

---

# 6. ScheduleChangeRequest

```text
ScheduleChangeRequest
-------------------------
id
scheduleId
doctorId
requestedStartTime
requestedEndTime
requestedRoomId
reason
status
reviewedBy
reviewedAt
createdAt
updatedAt
```

Status:

```text
PENDING
APPROVED
REJECTED
CANCELLED
```

Không có:

```text
requestedClinicId
```

Nếu Room được chọn thuộc Clinic khác thì business rule phải xử lý theo validation của WorkSchedule.

---

# 7. Appointment Service

## 7.1 Appointment

```text
Appointment
-------------------------
id
patientId
slotId
doctorId
clinicId
specialtyId
roomId
appointmentDate
startTime
endTime
status
cancellationReason
createdAt
updatedAt
```

Status:

```text
PENDING
CONFIRMED
COMPLETED
CANCELLED
NO_SHOW
```

### Appointment Context

Appointment lưu:

```text
doctorId
clinicId
specialtyId
roomId
appointmentDate
startTime
endTime
```

để giữ context tại thời điểm booking.

---

# 8. ReplacementProposal

```text
ReplacementProposal
-------------------------
id
appointmentId
originalDoctorId
proposedDoctorId
proposedClinicId
proposedSpecialtyId
proposedRoomId
proposedSlotId
status
expiresAt
respondedAt
createdAt
updatedAt
```

Status:

```text
PENDING
ACCEPTED
RESCHEDULED
CANCELLED
EXPIRED
```

Không có:

```text
responseAction
```

## 8.1 Meaning

ReplacementProposal là:

> Đề xuất thay thế Appointment hiện tại.

Nó không phải Appointment mới.

---

## 8.2 PENDING

Khi:

```text
status = PENDING
```

thì:

* Appointment không đổi;
* Doctor không đổi;
* Clinic không đổi;
* Room không đổi;
* Slot hiện tại không đổi;
* proposed Slot không được HOLD 24h.

---

## 8.3 ACCEPT

Khi Patient ACCEPT:

```text
PENDING
   ↓
validate
   ↓
revalidate proposed Slot
   ↓
HOLD
   ↓
reassign Appointment
   ↓
BOOK new Slot
   ↓
release old Slot
   ↓
ACCEPTED
```

Nếu Slot không còn AVAILABLE:

* không reassignment;
* Appointment giữ nguyên;
* thông báo Patient;
* cung cấp phương án khác.

---

# 9. AppointmentHistory

```text
AppointmentHistory
-------------------------
id
appointmentId
action
oldValue
newValue
changedBy
changedAt
```

Dùng để lưu lịch sử thay đổi Appointment.

Ví dụ:

```text
REASSIGNED
RESCHEDULED
CANCELLED
CONFIRMED
COMPLETED
NO_SHOW
```

---

# 10. Entity Ownership

## Doctor Service

```text
Doctor
DoctorSpecialty
```

## Clinic/Scheduling Service

```text
Clinic
Specialty
Room
WorkSchedule
Slot
LeaveRequest
ScheduleChangeRequest
```

## Appointment Service

```text
Appointment
ReplacementProposal
AppointmentHistory
```

## Patient Service

```text
Patient
```

## Identity/Auth Service

```text
User
Authentication
Authorization
```

---

# 11. Cross-Service Relationship

Không sử dụng database foreign key giữa các service.

Ví dụ:

```text
Appointment.doctorId
```

chỉ là ID reference đến Doctor Service.

Không:

```text
appointment_db -> doctor_db
```

Các service giao tiếp bằng:

* REST
* Events

---

# 12. Domain Boundary

```text
Doctor Service
    |
    +-- Doctor
    +-- DoctorSpecialty


Clinic/Scheduling Service
    |
    +-- Clinic
    +-- Specialty
    +-- Room
    +-- WorkSchedule
    +-- Slot
    +-- LeaveRequest
    +-- ScheduleChangeRequest


Appointment Service
    |
    +-- Appointment
    +-- ReplacementProposal
    +-- AppointmentHistory
```

---

# 13. Important Domain Distinctions

## Schedule

```text
Doctor + Clinic + Room + Specialty + Time
```

Ý nghĩa:

> Doctor được phân công làm việc.

## Appointment

```text
Patient + Doctor + Clinic + Room + Slot + Time
```

Ý nghĩa:

> Patient đặt lịch khám.

## Slot

```text
Một khoảng thời gian có thể được booking.
```

## ReplacementProposal

```text
Một đề xuất thay thế Appointment.
```

Không gộp bốn khái niệm trên thành một entity.
