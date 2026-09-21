-- Initialize MEDIQ databases for microservices
CREATE DATABASE IF NOT EXISTS `doctor_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `scheduling_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `appointment_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Grant privileges
GRANT ALL PRIVILEGES ON `doctor_db`.* TO 'mediq'@'%';
GRANT ALL PRIVILEGES ON `scheduling_db`.* TO 'mediq'@'%';
GRANT ALL PRIVILEGES ON `appointment_db`.* TO 'mediq'@'%';
FLUSH PRIVILEGES;
