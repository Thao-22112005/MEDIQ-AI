CREATE TABLE IF NOT EXISTS schedule_change_requests (
    request_id CHAR(36) PRIMARY KEY,
    schedule_id CHAR(36) NOT NULL,
    doctor_id CHAR(36) NOT NULL,
    requested_start_time TIME NOT NULL,
    requested_end_time TIME NOT NULL,
    requested_room_id CHAR(36) NULL,
    reason TEXT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reviewed_by VARCHAR(100) NULL,
    reviewed_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    INDEX idx_scr_schedule (schedule_id),
    INDEX idx_scr_doctor (doctor_id),
    INDEX idx_scr_status (status),
    CONSTRAINT fk_scr_schedule FOREIGN KEY (schedule_id) REFERENCES work_schedules (schedule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
