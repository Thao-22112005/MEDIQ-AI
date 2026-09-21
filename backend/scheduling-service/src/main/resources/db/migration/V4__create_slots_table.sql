-- Migration: V4__create_slots_table.sql
-- Description: Create table for Slot entity

CREATE TABLE IF NOT EXISTS `slots` (
    `slot_id` VARCHAR(36) NOT NULL,
    `schedule_id` VARCHAR(36) NOT NULL,
    `start_time` TIME NOT NULL,
    `end_time` TIME NOT NULL,
    `status` VARCHAR(30) NOT NULL,
    `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT `pk_slots` PRIMARY KEY (`slot_id`),
    CONSTRAINT `fk_slots_work_schedule` FOREIGN KEY (`schedule_id`) REFERENCES `work_schedules` (`schedule_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_slots_schedule_status` ON `slots` (`schedule_id`, `status`);
