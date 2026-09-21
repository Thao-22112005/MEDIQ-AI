CREATE TABLE IF NOT EXISTS leave_requests (
    leave_request_id CHAR(36) PRIMARY KEY,
    doctor_id CHAR(36) NOT NULL,
    start_date_time DATETIME(6) NOT NULL,
    end_date_time DATETIME(6) NOT NULL,
    reason TEXT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reviewed_by VARCHAR(100) NULL,
    reviewed_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    INDEX idx_leave_doctor_dates (doctor_id, start_date_time, end_date_time),
    INDEX idx_leave_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
