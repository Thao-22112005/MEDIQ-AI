# MEDIQ – BUSINESS RULES

## 1. System Scope

MEDIQ là hệ thống quản lý phòng khám theo kiến trúc microservices.

### 1.1. Business Service trong phạm vi phát triển hiện tại

* Doctor Service
* Clinic/Scheduling Service
* Appointment Service

### 1.2. Service thuộc hệ thống nhưng ngoài phạm vi phát triển hiện tại

* Patient Service
* Identity/Auth Service

Các service này vẫn được tham chiếu trong business flow khi cần, nhưng không thuộc phạm vi implementation hiện tại.

---

# 2. Doctor Rules

## BR-DOCTOR-001 – Doctor có thể có nhiều Specialty

Một Doctor có thể được liên kết với nhiều Specialty.

Một Doctor có tối đa một Specialty chính (`Primary Specialty`).

## BR-DOCTOR-002 – Doctor không bị gắn cố định với một Clinic

Doctor không có `clinicId` cố định.

Doctor có thể làm việc tại nhiều Clinic khác nhau thông qua WorkSchedule.

Không tạo:

* `Doctor.clinicId`
* `Doctor.roomId`

## BR-DOCTOR-003 – Doctor không tự sửa WorkSchedule

Doctor có thể:

* xem lịch làm việc;
* gửi LeaveRequest;
* gửi ScheduleChangeRequest nếu được hỗ trợ.

Doctor không được trực tiếp sửa WorkSchedule.

Việc approve/reject/thay đổi Schedule do Admin/Manager hoặc role được phân quyền thực hiện.

---

# 3. Clinic Rules

## BR-CLINIC-001 – MEDIQ hỗ trợ nhiều Clinic

Một hệ thống MEDIQ có thể có nhiều Clinic.

## BR-CLINIC-002 – Room thuộc đúng một Clinic

Mỗi Room thuộc chính xác một Clinic.

Một Clinic có thể có nhiều Room.

## BR-CLINIC-003 – Room phục vụ một Specialty

Mỗi Room được cấu hình cho một Specialty.

Ví dụ:

* Room A01 → Cardiology
* Room B01 → Dermatology

Không sử dụng Room cho Specialty không được cấu hình.

---

# 4. WorkSchedule Rules

## BR-SCHEDULE-001 – WorkSchedule xác định nơi Doctor làm việc

Một WorkSchedule xác định:

* Doctor
* Clinic
* Specialty
* Room
* Date
* StartTime
* EndTime

## BR-SCHEDULE-002 – Một Schedule chỉ có một Doctor

Một WorkSchedule chỉ thuộc về một Doctor.

## BR-SCHEDULE-003 – Một Schedule chỉ có một Room

Một WorkSchedule chỉ diễn ra tại một Room.

## BR-SCHEDULE-004 – Room phải thuộc đúng Clinic

`WorkSchedule.roomId` phải thuộc `WorkSchedule.clinicId`.

## BR-SCHEDULE-005 – Room phải đúng Specialty

Specialty của Room phải trùng với Specialty của WorkSchedule.

## BR-SCHEDULE-006 – Doctor phải phù hợp Specialty

Doctor được phân vào WorkSchedule phải có Specialty phù hợp với Specialty của Schedule.

## BR-SCHEDULE-007 – Doctor không được Schedule overlap

Một Doctor không được có hai WorkSchedule overlap về thời gian.

## BR-SCHEDULE-008 – Room không được Schedule overlap

Một Room không được có hai WorkSchedule overlap về thời gian.

## BR-SCHEDULE-009 – Schedule đã hoạt động không bị hard delete

Không xóa vật lý WorkSchedule sau khi đã có business activity.

Sử dụng status/history để bảo toàn dữ liệu.

---

# 5. Slot Rules

## BR-SLOT-001 – Slot được tạo từ WorkSchedule hợp lệ

Slot phải thuộc một WorkSchedule hợp lệ.

## BR-SLOT-002 – Past Slot không được booking

Slot đã quá thời gian hiện tại không được booking.

## BR-SLOT-003 – Slot có lifecycle riêng

Slot có các trạng thái:

* AVAILABLE
* HELD
* BOOKED
* BLOCKED
* EXPIRED

## BR-SLOT-004 – Replacement Slot không được HOLD trong 24 giờ

Khi tạo ReplacementProposal:

* không HOLD Slot trong 24 giờ;
* chỉ lưu `proposedSlotId`.

Khi Patient ACCEPT mới revalidate và HOLD Slot.

---

# 6. LeaveRequest Rules

## BR-LEAVE-001 – Doctor có thể gửi LeaveRequest

Doctor có thể gửi yêu cầu nghỉ.

## BR-LEAVE-002 – LeaveRequest cần được xử lý

LeaveRequest có thể:

* PENDING
* APPROVED
* REJECTED
* CANCELLED

Việc approve/reject thuộc Admin/Manager hoặc role được phân quyền.

## BR-LEAVE-003 – Doctor không tự approve LeaveRequest

Doctor không thể tự chuyển LeaveRequest sang APPROVED.

---

# 7. Doctor Leave + Schedule Rules

## BR-LEAVE-004 – Doctor nghỉ không có Appointment

Nếu Schedule bị ảnh hưởng bởi LeaveRequest APPROVED nhưng chưa có Appointment:

* Schedule có thể được CANCELLED;
* Slot liên quan được xử lý theo lifecycle của Slot.

## BR-LEAVE-005 – Doctor nghỉ có Appointment

Nếu Schedule bị ảnh hưởng và đã có Appointment:

* Không xóa Appointment;
* Không tự động CANCEL Appointment;
* Hệ thống tìm Replacement Doctor.

## BR-LEAVE-006 – Replacement Doctor ưu tiên cùng Specialty

Replacement Doctor phải ưu tiên Doctor có cùng Specialty với Appointment hiện tại.

Các tiêu chí cần kiểm tra:

* Specialty phù hợp;
* Doctor đang active;
* không có Schedule overlap;
* có khả năng phục vụ Appointment;
* có Slot phù hợp.

## BR-LEAVE-007 – Replacement Doctor không bắt buộc cùng Clinic

Replacement Doctor có thể thuộc Clinic khác.

Nếu Replacement Doctor ở Clinic khác:

* Patient có thể được chuyển sang Clinic đó;
* Patient phải xác nhận trước khi Appointment thực sự được reassignment.

---

# 8. Appointment Rules

## BR-APPOINTMENT-001 – Appointment khác WorkSchedule

WorkSchedule biểu diễn:

> Doctor làm việc ở đâu và khi nào.

Appointment biểu diễn:

> Patient đặt lịch với Doctor ở đâu và khi nào.

Hai khái niệm này không được gộp thành một entity.

## BR-APPOINTMENT-002 – Appointment Status

Appointment có các trạng thái:

* PENDING
* CONFIRMED
* COMPLETED
* CANCELLED
* NO_SHOW

## BR-APPOINTMENT-003 – NO_SHOW chỉ thuộc Appointment

NO_SHOW không phải trạng thái của:

* WorkSchedule
* Slot
* Doctor

NO_SHOW chỉ thuộc Appointment.

## BR-APPOINTMENT-004 – Appointment phải gắn với Slot

Appointment được tạo từ Slot hợp lệ.

## BR-APPOINTMENT-005 – Appointment lưu context

Appointment lưu context tại thời điểm booking:

* patientId
* doctorId
* clinicId
* specialtyId
* roomId
* slotId
* appointmentDate
* startTime
* endTime

Mục đích là giữ lại thông tin Appointment ngay cả khi Schedule sau đó thay đổi.

---

# 9. ReplacementProposal Rules

## BR-REPLACEMENT-001 – ReplacementProposal thuộc Appointment Service

ReplacementProposal là một entity của Appointment Service.

## BR-REPLACEMENT-002 – ReplacementProposal không phải Appointment mới

ReplacementProposal chỉ là đề xuất thay thế.

Trong thời gian Proposal PENDING:

* Appointment hiện tại không thay đổi;
* Doctor hiện tại không thay đổi;
* Clinic hiện tại không thay đổi;
* Room hiện tại không thay đổi;
* Slot hiện tại không thay đổi.

## BR-REPLACEMENT-003 – ReplacementProposal Status

Chỉ sử dụng:

* PENDING
* ACCEPTED
* RESCHEDULED
* CANCELLED
* EXPIRED

Không sử dụng `responseAction`.

## BR-REPLACEMENT-004 – Patient có 24 giờ để phản hồi

Patient có 24 giờ kể từ thời điểm Proposal được tạo.

Sau thời hạn:

`PENDING → EXPIRED`

## BR-REPLACEMENT-005 – Không tự động xử lý Appointment khi Proposal EXPIRED

Nếu Patient không phản hồi trong 24 giờ:

* Proposal → EXPIRED;
* Appointment giữ nguyên;
* không tự động reassignment;
* không tự động cancellation.

Admin/Manager xử lý tiếp theo business flow.

## BR-REPLACEMENT-006 – Patient có ba lựa chọn

Patient có thể:

* ACCEPT
* RESCHEDULE
* CANCEL

## BR-REPLACEMENT-007 – ACCEPT phải revalidate Slot

Khi Patient ACCEPT:

1. Kiểm tra Proposal còn PENDING.
2. Kiểm tra chưa hết hạn.
3. Kiểm tra proposed Slot còn AVAILABLE.
4. HOLD proposed Slot.
5. Revalidate context.
6. Reassign Appointment.
7. BOOK proposed Slot.
8. Release old Slot.
9. Cập nhật Proposal → ACCEPTED.
10. Tạo History/Event.

## BR-REPLACEMENT-008 – Nếu proposed Slot không còn khả dụng

Nếu proposed Slot không còn AVAILABLE:

* không reassignment;
* Appointment vẫn giữ nguyên;
* Proposal không được đánh dấu ACCEPTED;
* thông báo Patient;
* cung cấp phương án khác/reschedule.

---

# 10. Appointment Lifecycle Rules

## BR-APPOINTMENT-006 – Confirm

Appointment:

`PENDING → CONFIRMED`

## BR-APPOINTMENT-007 – Complete

Appointment:

`CONFIRMED → COMPLETED`

## BR-APPOINTMENT-008 – Cancel

Appointment có thể chuyển sang:

`CANCELLED`

Khi Cancel cần xử lý Slot tương ứng.

## BR-APPOINTMENT-009 – No-show

Appointment có thể chuyển:

`CONFIRMED → NO_SHOW`

NO_SHOW không làm thay đổi trạng thái của WorkSchedule.

---

# 11. Reschedule Rules

Reschedule phải thực hiện theo thứ tự:

1. Tìm Slot mới.
2. Hold Slot mới.
3. Validate.
4. Book Slot mới.
5. Release Slot cũ.
6. Update Appointment.
7. Tạo History.
8. Publish Event.
9. Notify các bên liên quan.

Nếu bước sau thất bại phải có compensation phù hợp.

---

# 12. ScheduleChangeRequest Rules

ScheduleChangeRequest được sử dụng khi cần thay đổi Schedule thông qua workflow được kiểm soát.

Status:

* PENDING
* APPROVED
* REJECTED
* CANCELLED

Không tự động thay đổi Appointment nếu Schedule có Appointment bị ảnh hưởng.

Phải xác định affected Appointment và xử lý theo business flow.

Không có `requestedClinicId` trong model hiện tại.

---

# 13. Data Ownership Rules

Doctor Service sở hữu:

* Doctor
* DoctorSpecialty

Clinic/Scheduling Service sở hữu:

* Clinic
* Specialty
* Room
* WorkSchedule
* Slot
* LeaveRequest
* ScheduleChangeRequest

Appointment Service sở hữu:

* Appointment
* ReplacementProposal
* AppointmentHistory

Patient Service sở hữu Patient.

Identity/Auth Service sở hữu User/Auth.

---

# 14. Cross-Service Rules

Không tạo foreign key database giữa các service.

Không service nào được truy cập trực tiếp database của service khác.

Các service giao tiếp thông qua:

* REST API
* Events

ID được sử dụng để reference entity giữa service.

---

# 15. Delete Rules

Không hard delete dữ liệu đã có business activity.

Ưu tiên:

* Status
* History
* Audit information

Mục tiêu là giữ được lịch sử hoạt động của hệ thống.

---

# 16. Architecture Rules

Không tự thêm:

* Kafka
* RabbitMQ
* Outbox
* Saga
* CQRS
* Event Sourcing
* Kubernetes

nếu chưa có business/technical requirement thực tế.

Ưu tiên:

> Correctness > Simplicity > Testability > Maintainability
