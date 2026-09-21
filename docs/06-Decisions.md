# MEDIQ – ARCHITECTURE & DOMAIN DECISIONS

## D001 – Current Development Scope

MEDIQ hiện tập trung implementation vào:

* Doctor Service
* Clinic/Scheduling Service
* Appointment Service

Patient Service và Identity/Auth Service thuộc hệ thống nhưng ngoài phạm vi phát triển hiện tại.

---

## D002 – Doctor Multiple Specialties

Doctor có thể có nhiều Specialty.

Doctor tối đa một Specialty chính.

---

## D003 – No Shift Entity

Không tạo Shift Entity ở thời điểm hiện tại.

WorkSchedule sử dụng trực tiếp:

```text
date
startTime
endTime
```

Chỉ tạo Shift khi có business requirement rõ ràng về các ca chuẩn hóa.

---

## D004 – Schedule vs Appointment

Schedule và Appointment là hai domain concept khác nhau.

Schedule:

> Doctor assignment.

Appointment:

> Patient booking.

Không gộp thành một entity.

---

## D005 – Leave Approval

Doctor có thể gửi LeaveRequest.

Admin/Manager hoặc role được phân quyền approve/reject.

Doctor không tự approve.

---

## D006 – Replacement Doctor Criteria

Replacement ưu tiên:

* cùng Specialty;
* active;
* professional scope phù hợp;
* không overlap;
* có Slot phù hợp.

---

## D007 – Cross-Clinic Replacement

Replacement Doctor không bắt buộc ở Clinic ban đầu.

Patient có thể được chuyển sang Clinic của Replacement Doctor.

Patient phải xác nhận trước khi reassignment.

---

## D008 – ReplacementProposal

ReplacementProposal thuộc Appointment Service.

Nó là proposal, không phải Appointment mới.

---

## D009 – 24 Hour Response

Patient có 24 giờ để phản hồi ReplacementProposal.

Sau 24 giờ:

```text
PENDING → EXPIRED
```

---

## D010 – No responseAction

Không sử dụng đồng thời:

```text
status
responseAction
```

Chỉ sử dụng:

```text
status
```

Các trạng thái:

```text
PENDING
ACCEPTED
RESCHEDULED
CANCELLED
EXPIRED
```

---

## D011 – No Automatic Action After Expiry

Khi Proposal EXPIRED:

* không auto reassignment;
* không auto cancellation;
* Appointment giữ nguyên.

Admin/Manager xử lý tiếp.

---

## D012 – No 24h Slot Hold

Không HOLD proposed Slot trong 24 giờ.

Proposal chỉ giữ reference:

```text
proposedSlotId
```

Slot chỉ được HOLD khi Patient ACCEPT.

---

## D013 – Revalidate on Accept

Khi Patient ACCEPT:

```text
revalidate proposed Slot
```

Không giả định Slot vẫn AVAILABLE.

Nếu Slot không còn khả dụng:

* không reassignment;
* Appointment giữ nguyên;
* thông báo Patient.

---

## D014 – Atomic Business Reassignment

Reassignment cần đảm bảo business consistency:

```text
HOLD new Slot
→ reassign Appointment
→ BOOK new Slot
→ release old Slot
```

Có compensation nếu operation thất bại.

---

## D015 – Room Ownership

Room thuộc đúng một Clinic.

Room được cấu hình cho một Specialty.

---

## D016 – Doctor Clinic Assignment

Doctor không có Clinic cố định.

Doctor có thể làm việc ở nhiều Clinic thông qua WorkSchedule.

---

## D017 – Appointment Context

Appointment lưu snapshot/context:

```text
doctorId
clinicId
specialtyId
roomId
appointmentDate
startTime
endTime
```

để bảo toàn context tại thời điểm booking.

---

## D018 – ScheduleChangeRequest

ScheduleChangeRequest không có:

```text
requestedClinicId
```

Clinic của Schedule được xác định theo WorkSchedule/Room validation.

---

## D019 – Cross-Service Database Isolation

Không có cross-service DB FK.

Không service nào truy cập trực tiếp DB của service khác.

---

## D020 – No Mandatory Message Broker

Không bắt buộc Kafka/RabbitMQ ở giai đoạn hiện tại.

Event contract có thể được thiết kế trước.

Broker chỉ được thêm khi có requirement.

---

## D021 – Avoid Over-engineering

Không tự thêm:

* Kafka
* RabbitMQ
* Outbox
* Saga
* CQRS
* Event Sourcing
* Kubernetes

nếu chưa cần thiết.

---

## D022 – Booking Consistency

Booking phải bảo đảm Slot và Appointment không rơi vào trạng thái inconsistent.

Flow:

```text
Validate
→ HOLD
→ Create Appointment
→ Confirm
→ BOOK
```

Có compensation nếu failure.

---

## D023 – Event Contract

Event tối thiểu có:

```text
eventId
eventType
occurredAt
source
version
payload
```

Consumer phải idempotent.

---

## D024 – NO_SHOW

NO_SHOW chỉ là Appointment state.

Không phải Schedule state.

---

## D025 – No Hard Delete

Không hard delete entity đã có business activity.

Phải bảo toàn history/audit khi cần.

---

## D026 – Domain Baseline Freeze

Business Rules, Domain Model và Architecture hiện tại được xem là baseline.

Không tiếp tục thay đổi domain chỉ vì preference về code.

Chỉ thay đổi khi:

1. Có business requirement mới;
2. Phát hiện contradiction thực sự;
3. Code hiện tại chứng minh tài liệu không khả thi;
4. Có decision mới được xác nhận.

Mọi thay đổi quan trọng phải cập nhật:

```text
BUSINESS-RULES
DOMAIN-MODEL
ARCHITECTURE
DECISIONS
WORKFLOWS
AI_CONTEXT
CURRENT_STATE
TODO
HANDOVER
```
