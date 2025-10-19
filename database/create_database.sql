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