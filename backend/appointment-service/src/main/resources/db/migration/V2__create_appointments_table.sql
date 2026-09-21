-- Migration: V2__create_appointments_table.sql
-- Description: Create tables for Appointment and AppointmentHistory entities

CREATE TABLE IF NOT EXISTS `appointments` (
    `appointment_id` VARCHAR(36) NOT NULL,
    `patient_id` VARCHAR(36) NOT NULL,
    `slot_id` VARCHAR(36) NOT NULL,
    `doctor_id` VARCHAR(36) NOT NULL,
    `clinic_id` VARCHAR(36) NOT NULL,
    `specialty_id` VARCHAR(36) NOT NULL,
    `room_id` VARCHAR(36) NOT NULL,
    `appointment_date` DATE NOT NULL,
    `start_time` TIME NOT NULL,
    `end_time` TIME NOT NULL,
    `status` VARCHAR(30) NOT NULL,
    `cancellation_reason` VARCHAR(500) NULL,
    `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT `pk_appointments` PRIMARY KEY (`appointment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_appointments_patient_id` ON `appointments` (`patient_id`);
CREATE INDEX `idx_appointments_slot_id` ON `appointments` (`slot_id`);
CREATE INDEX `idx_appointments_doctor_date` ON `appointments` (`doctor_id`, `appointment_date`);
CREATE INDEX `idx_appointments_clinic_date` ON `appointments` (`clinic_id`, `appointment_date`);

CREATE TABLE IF NOT EXISTS `appointment_history` (
    `history_id` VARCHAR(36) NOT NULL,
    `appointment_id` VARCHAR(36) NOT NULL,
    `action` VARCHAR(50) NOT NULL,
    `old_value` VARCHAR(100) NULL,
    `new_value` VARCHAR(100) NULL,
    `changed_by` VARCHAR(100) NULL,
    `changed_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT `pk_appointment_history` PRIMARY KEY (`history_id`),
    CONSTRAINT `fk_appointment_history_appointment` FOREIGN KEY (`appointment_id`) REFERENCES `appointments` (`appointment_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_appointment_history_appointment_id` ON `appointment_history` (`appointment_id`);
