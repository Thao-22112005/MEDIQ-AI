-- Migration: V3__create_work_schedules_table.sql
-- Description: Create table for WorkSchedule entity

CREATE TABLE IF NOT EXISTS `work_schedules` (
    `schedule_id` VARCHAR(36) NOT NULL,
    `doctor_id` VARCHAR(36) NOT NULL,
    `clinic_id` VARCHAR(36) NOT NULL,
    `specialty_id` VARCHAR(36) NOT NULL,
    `room_id` VARCHAR(36) NOT NULL,
    `date` DATE NOT NULL,
    `start_time` TIME NOT NULL,
    `end_time` TIME NOT NULL,
    `status` VARCHAR(30) NOT NULL,
    `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT `pk_work_schedules` PRIMARY KEY (`schedule_id`),
    CONSTRAINT `fk_work_schedules_clinic` FOREIGN KEY (`clinic_id`) REFERENCES `clinics` (`clinic_id`),
    CONSTRAINT `fk_work_schedules_specialty` FOREIGN KEY (`specialty_id`) REFERENCES `specialties` (`specialty_id`),
    CONSTRAINT `fk_work_schedules_room` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_work_schedules_doctor_date` ON `work_schedules` (`doctor_id`, `date`);
CREATE INDEX `idx_work_schedules_room_date` ON `work_schedules` (`room_id`, `date`);
CREATE INDEX `idx_work_schedules_clinic_date` ON `work_schedules` (`clinic_id`, `date`);
