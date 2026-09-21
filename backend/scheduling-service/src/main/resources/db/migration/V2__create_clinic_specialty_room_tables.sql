-- Migration: V2__create_clinic_specialty_room_tables.sql
-- Description: Create tables for Clinic, Specialty, and Room entities

CREATE TABLE IF NOT EXISTS `specialties` (
    `specialty_id` VARCHAR(36) NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `description` TEXT NULL,
    `status` VARCHAR(30) NOT NULL,
    `default_slot_duration` INT NOT NULL DEFAULT 15,
    `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT `pk_specialties` PRIMARY KEY (`specialty_id`),
    CONSTRAINT `uk_specialties_name` UNIQUE (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `clinics` (
    `clinic_id` VARCHAR(36) NOT NULL,
    `code` VARCHAR(50) NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `address` VARCHAR(500) NULL,
    `phone` VARCHAR(50) NULL,
    `status` VARCHAR(30) NOT NULL,
    `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT `pk_clinics` PRIMARY KEY (`clinic_id`),
    CONSTRAINT `uk_clinics_code` UNIQUE (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `rooms` (
    `room_id` VARCHAR(36) NOT NULL,
    `clinic_id` VARCHAR(36) NOT NULL,
    `specialty_id` VARCHAR(36) NOT NULL,
    `code` VARCHAR(50) NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `status` VARCHAR(30) NOT NULL,
    `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT `pk_rooms` PRIMARY KEY (`room_id`),
    CONSTRAINT `fk_rooms_clinic` FOREIGN KEY (`clinic_id`) REFERENCES `clinics` (`clinic_id`),
    CONSTRAINT `fk_rooms_specialty` FOREIGN KEY (`specialty_id`) REFERENCES `specialties` (`specialty_id`),
    CONSTRAINT `uk_rooms_clinic_code` UNIQUE (`clinic_id`, `code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_rooms_clinic_id` ON `rooms` (`clinic_id`);
CREATE INDEX `idx_rooms_specialty_id` ON `rooms` (`specialty_id`);
