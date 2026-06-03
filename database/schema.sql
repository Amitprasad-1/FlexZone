-- FlexZone Asansol Gym Management System Schema
-- Target Database: MySQL

CREATE DATABASE IF NOT EXISTS flexzone_db;
USE flexzone_db;

-- 1. Users Table (Core Auth Entity)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL, -- 'ADMIN', 'TRAINER', 'MEMBER'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_username (username),
    INDEX idx_user_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Membership Plans Table
CREATE TABLE IF NOT EXISTS membership_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    duration_days INT NOT NULL, -- duration of membership (e.g. 30, 90, 365)
    INDEX idx_plan_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Trainer Profiles Table (Extends User with Role=TRAINER)
CREATE TABLE IF NOT EXISTS trainer_profiles (
    id BIGINT PRIMARY KEY,
    specialization VARCHAR(100),
    bio TEXT,
    experience_years INT NOT NULL DEFAULT 0,
    FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Member Profiles Table (Extends User with Role=MEMBER)
CREATE TABLE IF NOT EXISTS member_profiles (
    id BIGINT PRIMARY KEY,
    membership_status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- 'ACTIVE', 'EXPIRED', 'PENDING'
    membership_plan_id BIGINT,
    membership_start_date DATE,
    membership_end_date DATE,
    assigned_trainer_id BIGINT,
    FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (membership_plan_id) REFERENCES membership_plans(id) ON DELETE SET NULL,
    FOREIGN KEY (assigned_trainer_id) REFERENCES trainer_profiles(id) ON DELETE SET NULL,
    INDEX idx_member_status (membership_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Time Slots (Hourly Gym Slots to limit capacity)
CREATE TABLE IF NOT EXISTS time_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    max_capacity INT NOT NULL DEFAULT 30,
    CONSTRAINT chk_slot_time CHECK (start_time < end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Class Schedules (Special Classes e.g. Zumba, Yoga)
CREATE TABLE IF NOT EXISTS class_schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_name VARCHAR(100) NOT NULL,
    description TEXT,
    trainer_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    max_capacity INT NOT NULL DEFAULT 20,
    FOREIGN KEY (trainer_id) REFERENCES trainer_profiles(id) ON DELETE CASCADE,
    CONSTRAINT chk_class_time CHECK (start_time < end_time),
    INDEX idx_class_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Bookings Table (Members booking time slots or special classes)
CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    booking_type VARCHAR(20) NOT NULL, -- 'SLOT' or 'CLASS'
    time_slot_id BIGINT NULL,
    class_schedule_id BIGINT NULL,
    booking_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES member_profiles(id) ON DELETE CASCADE,
    FOREIGN KEY (time_slot_id) REFERENCES time_slots(id) ON DELETE CASCADE,
    FOREIGN KEY (class_schedule_id) REFERENCES class_schedules(id) ON DELETE CASCADE,
    -- Prevent double booking of slot on same day
    CONSTRAINT uq_member_slot_date UNIQUE (member_id, time_slot_id, booking_date),
    -- Prevent double booking of class on same day
    CONSTRAINT uq_member_class UNIQUE (member_id, class_schedule_id),
    INDEX idx_booking_date (booking_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Attendance Table (Contactless Check-In)
CREATE TABLE IF NOT EXISTS attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    check_in_time DATETIME NOT NULL,
    verified_by_admin_id BIGINT NOT NULL,
    FOREIGN KEY (member_id) REFERENCES member_profiles(id) ON DELETE CASCADE,
    FOREIGN KEY (verified_by_admin_id) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_attendance_date (check_in_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Products Table (E-Shop Gym Supplies)
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    image_url VARCHAR(255),
    stock INT NOT NULL DEFAULT 0,
    INDEX idx_product_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Orders Table
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    order_date DATETIME NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- 'PENDING', 'PAID', 'SHIPPED', 'CANCELLED'
    FOREIGN KEY (member_id) REFERENCES member_profiles(id) ON DELETE CASCADE,
    INDEX idx_order_date (order_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Order Items Table
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. Payments Table (Mock payment gate logger)
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_date DATETIME NOT NULL,
    payment_type VARCHAR(50) NOT NULL, -- 'MEMBERSHIP_RENEWAL', 'PRODUCT_PURCHASE'
    payment_method VARCHAR(50) NOT NULL, -- 'RAZORPAY', 'PAYTM', 'CASH'
    transaction_id VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL, -- 'SUCCESS', 'FAILED'
    FOREIGN KEY (member_id) REFERENCES member_profiles(id) ON DELETE CASCADE,
    INDEX idx_payment_date (payment_date),
    INDEX idx_payment_tx (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. BMI Logs Table (Personalized progress tracking)
CREATE TABLE IF NOT EXISTS bmi_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    height_cm DECIMAL(5, 2) NOT NULL,
    weight_kg DECIMAL(5, 2) NOT NULL,
    calculated_bmi DECIMAL(4, 2) NOT NULL,
    logged_date DATE NOT NULL,
    FOREIGN KEY (member_id) REFERENCES member_profiles(id) ON DELETE CASCADE,
    INDEX idx_bmi_member (member_id, logged_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. Initial Data Seed (Standard Roles and Sample Subscriptions)
INSERT INTO users (username, password, email, full_name, role) VALUES 
('admin', '$2a$10$wD/zS3k1k/6o3uQ2yRkMJuF7PskkFzNf6L9Fmly0oWf5l548c.w6W', 'admin@flexzone.com', 'System Admin', 'ADMIN');
-- Note: password for admin is 'admin123' bcrypt hashed

INSERT INTO membership_plans (name, description, price, duration_days) VALUES
('Monthly Starter', 'Access to gym area and general training.', 1500.00, 30),
('Quarterly Fit', 'Includes group class entry and personal training review.', 4000.00, 90),
('Yearly Elite', 'Unlimited access to all classes, personal training, steam rooms.', 12000.00, 365);

INSERT INTO time_slots (start_time, end_time, max_capacity) VALUES
('06:00:00', '07:00:00', 25),
('07:00:00', '08:00:00', 25),
('08:00:00', '09:00:00', 25),
('09:00:00', '10:00:00', 20),
('16:00:00', '17:00:00', 25),
('17:00:00', '18:00:00', 30),
('18:00:00', '19:00:00', 30),
('19:00:00', '20:00:00', 30),
('20:00:00', '21:00:00', 25);

INSERT INTO products (name, description, price, image_url, stock) VALUES
('Premium Whey Protein (1kg)', 'Ultra-filtered premium chocolate whey protein concentrate.', 2800.00, 'whey_protein.jpg', 50),
('Ergonomic Gym Shaker (700ml)', 'Leak-proof protein shaker bottle with mixing ball.', 350.00, 'gym_shaker.jpg', 120),
('Pre-Workout Explode (300g)', 'High-energy powder formulation for intense pump.', 1900.00, 'pre_workout.jpg', 40),
('L-Carnitine Liquid (500ml)', 'Supports energy metabolism and fat loss.', 1200.00, 'l_carnitine.jpg', 30),
('Comfort Fit Gym Gloves', 'Padded training gloves with wrist support wrap.', 450.00, 'gym_gloves.jpg', 80);
