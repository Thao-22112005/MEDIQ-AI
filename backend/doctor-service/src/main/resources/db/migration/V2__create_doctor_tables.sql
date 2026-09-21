-- Migration: V2__create_doctor_tables.sql
-- Description: Create tables for Doctor and DoctorSpecialty entities

CREATE TABLE IF NOT EXISTS `doctors` (
    `doctor_id` VARCHAR(36) NOT NULL,
    `user_id` VARCHAR(36) NOT NULL,
    `full_name` VARCHAR(255) NOT NULL,
    `license_number` VARCHAR(100) NOT NULL,
    `status` VARCHAR(30) NOT NULL,
    `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT `pk_doctors` PRIMARY KEY (`doctor_id`),
    CONSTRAINT `uk_doctors_license_number` UNIQUE (`license_number`),
    CONSTRAINT `uk_doctors_user_id` UNIQUE (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `doctor_specialties` (
    `doctor_specialty_id` VARCHAR(36) NOT NULL,
    `doctor_id` VARCHAR(36) NOT NULL,
    `specialty_id` VARCHAR(36) NOT NULL,
    `is_primary` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT `pk_doctor_specialties` PRIMARY KEY (`doctor_specialty_id`),
    CONSTRAINT `fk_doctor_specialties_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctors` (`doctor_id`) ON DELETE CASCADE,
    CONSTRAINT `uk_doctor_specialty` UNIQUE (`doctor_id`, `specialty_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_doctor_specialties_specialty_id` ON `doctor_specialties` (`specialty_id`);
