-- =============================================================================
-- Bus Reservation System (BRS) - MySQL Database Schema
-- Compatible with MySQL 8.0+
-- =============================================================================

CREATE DATABASE IF NOT EXISTS `brs_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `brs_db`;

SET FOREIGN_KEY_CHECKS = 0;

-- Drop existing tables if re-initializing
DROP TABLE IF EXISTS `tickets`;
DROP TABLE IF EXISTS `payments`;
DROP TABLE IF EXISTS `passengers`;
DROP TABLE IF EXISTS `booking_seats`;
DROP TABLE IF EXISTS `bookings`;
DROP TABLE IF EXISTS `schedule_seats`;
DROP TABLE IF EXISTS `schedules`;
DROP TABLE IF EXISTS `routes`;
DROP TABLE IF EXISTS `buses`;
DROP TABLE IF EXISTS `users`;

SET FOREIGN_KEY_CHECKS = 1;

-- 1. USERS TABLE
CREATE TABLE `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL,
    `full_name` VARCHAR(100) NOT NULL,
    `phone_number` VARCHAR(20) NOT NULL,
    `role` ENUM('ROLE_USER', 'ROLE_ADMIN') NOT NULL DEFAULT 'ROLE_USER',
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. BUSES TABLE
CREATE TABLE `buses` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `bus_number` VARCHAR(30) NOT NULL UNIQUE,
    `bus_name` VARCHAR(100) NOT NULL,
    `bus_type` ENUM('SEATER', 'SLEEPER', 'AC_SEATER', 'AC_SLEEPER', 'VOLVO_MULTI_AXLE') NOT NULL,
    `total_seats` INT NOT NULL DEFAULT 40,
    `amenities` VARCHAR(255) DEFAULT 'WiFi, Water Bottle, Charging Point',
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. ROUTES TABLE
CREATE TABLE `routes` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `source_city` VARCHAR(80) NOT NULL,
    `destination_city` VARCHAR(80) NOT NULL,
    `distance_km` DECIMAL(8, 2) NOT NULL,
    `estimated_duration_hours` DECIMAL(4, 2) NOT NULL,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_route` (`source_city`, `destination_city`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. SCHEDULES TABLE
CREATE TABLE `schedules` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `bus_id` BIGINT NOT NULL,
    `route_id` BIGINT NOT NULL,
    `departure_time` DATETIME NOT NULL,
    `arrival_time` DATETIME NOT NULL,
    `base_fare` DECIMAL(10, 2) NOT NULL,
    `status` ENUM('SCHEDULED', 'IN_TRANSIT', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'SCHEDULED',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_schedule_bus` FOREIGN KEY (`bus_id`) REFERENCES `buses` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_schedule_route` FOREIGN KEY (`route_id`) REFERENCES `routes` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. SCHEDULE SEATS (Handles seat layout, availability, and temporary 10-minute locking)
CREATE TABLE `schedule_seats` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `schedule_id` BIGINT NOT NULL,
    `seat_number` VARCHAR(10) NOT NULL,
    `seat_type` ENUM('WINDOW', 'AISLE', 'LOWER', 'UPPER') NOT NULL DEFAULT 'WINDOW',
    `fare` DECIMAL(10, 2) NOT NULL,
    `is_booked` BOOLEAN NOT NULL DEFAULT FALSE,
    `is_locked` BOOLEAN NOT NULL DEFAULT FALSE,
    `locked_at` DATETIME DEFAULT NULL,
    `locked_by_user_id` BIGINT DEFAULT NULL,
    CONSTRAINT `fk_seat_schedule` FOREIGN KEY (`schedule_id`) REFERENCES `schedules` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_seat_locked_user` FOREIGN KEY (`locked_by_user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL,
    UNIQUE KEY `uk_schedule_seat` (`schedule_id`, `seat_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. BOOKINGS TABLE
CREATE TABLE `bookings` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `booking_reference` VARCHAR(40) NOT NULL UNIQUE, -- PNR code
    `user_id` BIGINT NOT NULL,
    `schedule_id` BIGINT NOT NULL,
    `total_amount` DECIMAL(10, 2) NOT NULL,
    `status` ENUM('PENDING', 'CONFIRMED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    `booking_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `cancellation_reason` VARCHAR(255) DEFAULT NULL,
    `refund_amount` DECIMAL(10, 2) DEFAULT 0.00,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_booking_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_booking_schedule` FOREIGN KEY (`schedule_id`) REFERENCES `schedules` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. BOOKING SEATS TABLE
CREATE TABLE `booking_seats` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `booking_id` BIGINT NOT NULL,
    `schedule_seat_id` BIGINT NOT NULL,
    `seat_number` VARCHAR(10) NOT NULL,
    `fare` DECIMAL(10, 2) NOT NULL,
    CONSTRAINT `fk_bseat_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_bseat_schedule_seat` FOREIGN KEY (`schedule_seat_id`) REFERENCES `schedule_seats` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. PASSENGERS TABLE
CREATE TABLE `passengers` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `booking_id` BIGINT NOT NULL,
    `seat_number` VARCHAR(10) NOT NULL,
    `full_name` VARCHAR(100) NOT NULL,
    `age` INT NOT NULL,
    `gender` ENUM('MALE', 'FEMALE', 'OTHER') NOT NULL,
    CONSTRAINT `fk_passenger_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. PAYMENTS TABLE (Razorpay test mode tracking)
CREATE TABLE `payments` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `booking_id` BIGINT NOT NULL,
    `razorpay_order_id` VARCHAR(100) DEFAULT NULL,
    `razorpay_payment_id` VARCHAR(100) DEFAULT NULL,
    `razorpay_signature` VARCHAR(255) DEFAULT NULL,
    `amount` DECIMAL(10, 2) NOT NULL,
    `currency` VARCHAR(10) NOT NULL DEFAULT 'INR',
    `status` ENUM('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED') NOT NULL DEFAULT 'PENDING',
    `payment_method` VARCHAR(50) DEFAULT 'RAZORPAY_TEST',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_payment_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. TICKETS TABLE (PDF + QR code)
CREATE TABLE `tickets` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `booking_id` BIGINT NOT NULL UNIQUE,
    `ticket_number` VARCHAR(50) NOT NULL UNIQUE,
    `qr_code_base64` LONGTEXT DEFAULT NULL,
    `pdf_file_path` VARCHAR(255) DEFAULT NULL,
    `status` ENUM('ISSUED', 'CANCELLED') NOT NULL DEFAULT 'ISSUED',
    `issued_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_ticket_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================================================
-- INITIAL SEED DATA (Default Admin & User, Sample Buses, Routes, Schedules)
-- Default passwords hashed with BCrypt (password is "Admin@123" and "User@123")
-- =============================================================================

-- Passwords:
-- Admin@123 -> $2a$10$wO8Q12E3o0fR.Z9bW9VnLeT2z4N8o1S5k6C8c6uR1P9o7aBcDeFgH (BCrypt sample or generated on first run)
INSERT INTO `users` (`id`, `username`, `email`, `password_hash`, `full_name`, `phone_number`, `role`) VALUES
(1, 'admin', 'admin@brs.com', '$2a$10$7ZzM8lM10.19830588697665421547.w0U.347592751543321456789012345', 'System Administrator', '9876543210', 'ROLE_ADMIN'),
(2, 'user', 'user@example.com', '$2a$10$7ZzM8lM10.19ye830588697665421547.w0U.347592751543321456789012345', 'User', '9876543211', 'ROLE_USER')
ON DUPLICATE KEY UPDATE `username`=`username`;

INSERT INTO `buses` (`id`, `bus_number`, `bus_name`, `bus_type`, `total_seats`, `amenities`) VALUES
(1, 'DL-01-AB-1234', 'Express Star Volvo', 'VOLVO_MULTI_AXLE', 32, 'AC, Charging, Blanket, WiFi, Water Bottle'),
(2, 'MH-02-CD-5678', 'Royal Travels Sleeper', 'AC_SLEEPER', 28, 'AC, Sleeper Berths, Pillow, Water Bottle'),
(3, 'KA-03-EF-9012', 'City Cruiser Standard', 'SEATER', 40, 'Push-back seats, Music')
ON DUPLICATE KEY UPDATE `bus_number`=`bus_number`;

INSERT INTO `routes` (`id`, `source_city`, `destination_city`, `distance_km`, `estimated_duration_hours`) VALUES
(1, 'Delhi', 'Jaipur', 280.00, 5.50),
(2, 'Jaipur', 'Delhi', 280.00, 5.50),
(3, 'Mumbai', 'Pune', 150.00, 3.50),
(4, 'Bangalore', 'Hyderabad', 570.00, 9.00)
ON DUPLICATE KEY UPDATE `source_city`=`source_city`;
