-- Create AutoCare360 Database
-- Run this manually in MySQL if automatic creation doesn't work

CREATE DATABASE IF NOT EXISTS autocare360_db;
USE autocare360_db;

-- Verify database creation
SHOW DATABASES;

-- Optional: Create a specific user for the application
-- CREATE USER 'autocare360_user'@'localhost' IDENTIFIED BY 'autocare360_password';
-- GRANT ALL PRIVILEGES ON autocare360_db.* TO 'autocare360_user'@'localhost';
-- FLUSH PRIVILEGES;

-- Vehicles table
CREATE TABLE IF NOT EXISTS vehicles (
	id BIGINT PRIMARY KEY AUTO_INCREMENT,
	user_id BIGINT NOT NULL,
	vin VARCHAR(50),
	make VARCHAR(255) NOT NULL,
	model VARCHAR(255) NOT NULL,
	year INT,
	plate_number VARCHAR(50),
	color VARCHAR(50),
	meta TEXT,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT fk_vehicles_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Services / service records table
CREATE TABLE IF NOT EXISTS services (
	id BIGINT PRIMARY KEY AUTO_INCREMENT,
	vehicle_id BIGINT NOT NULL,
	type VARCHAR(255) NOT NULL,
	status VARCHAR(30) DEFAULT 'requested',
	requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	scheduled_at TIMESTAMP NULL,
	notes TEXT,
	attachments TEXT,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT fk_services_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)
);
