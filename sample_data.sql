-- Sample Data for AutoCare360 Database
-- This script adds 10+ sample records to each table

USE autocare360;

-- ========================================
-- 1. USERS TABLE (10 users: 1 admin, 2 managers, 3 mechanics, 4 customers)
-- ========================================
-- Password for all users: "password" (BCrypt hash)
INSERT IGNORE INTO users (email, password_hash, name, phone, status, employee_no, department, created_at, updated_at) VALUES
('admin@autocare360.com', '$2a$10$rH3qKqGzW3qjI5vY5qJKZOYdD5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y', 'John Smith', '0771234567', 'ACTIVE', 'EMP001', 'MANAGEMENT', NOW(), NOW()),
('manager1@autocare360.com', '$2a$10$rH3qKqGzW3qjI5vY5qJKZOYdD5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y', 'Sarah Johnson', '0771234568', 'ACTIVE', 'EMP002', 'OPERATIONS', NOW(), NOW()),
('manager2@autocare360.com', '$2a$10$rH3qKqGzW3qjI5vY5qJKZOYdD5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y', 'Michael Brown', '0771234569', 'ACTIVE', 'EMP003', 'SERVICE', NOW(), NOW()),
('mechanic1@autocare360.com', '$2a$10$rH3qKqGzW3qjI5vY5qJKZOYdD5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y', 'David Wilson', '0771234570', 'ACTIVE', 'EMP004', 'TECHNICAL', NOW(), NOW()),
('mechanic2@autocare360.com', '$2a$10$rH3qKqGzW3qjI5vY5qJKZOYdD5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y', 'James Miller', '0771234571', 'ACTIVE', 'EMP005', 'TECHNICAL', NOW(), NOW()),
('mechanic3@autocare360.com', '$2a$10$rH3qKqGzW3qjI5vY5qJKZOYdD5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y', 'Robert Davis', '0771234572', 'ACTIVE', 'EMP006', 'TECHNICAL', NOW(), NOW()),
('customer1@gmail.com', '$2a$10$rH3qKqGzW3qjI5vY5qJKZOYdD5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y', 'Emily Anderson', '0771234573', 'ACTIVE', NULL, NULL, NOW(), NOW()),
('customer2@gmail.com', '$2a$10$rH3qKqGzW3qjI5vY5qJKZOYdD5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y', 'Daniel Thomas', '0771234574', 'ACTIVE', NULL, NULL, NOW(), NOW()),
('customer3@gmail.com', '$2a$10$rH3qKqGzW3qjI5vY5qJKZOYdD5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y', 'Sophia Martinez', '0771234575', 'ACTIVE', NULL, NULL, NOW(), NOW()),
('customer4@gmail.com', '$2a$10$rH3qKqGzW3qjI5vY5qJKZOYdD5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y', 'Oliver Garcia', '0771234576', 'ACTIVE', NULL, NULL, NOW(), NOW());

-- ========================================
-- 2. ROLES TABLE (if not exists)
-- ========================================
INSERT IGNORE INTO roles (name) VALUES
('ADMIN'),
('MANAGER'),
('MECHANIC'),
('CUSTOMER');

-- ========================================
-- 3. USERS_ROLES TABLE (Assign roles)
-- ========================================
INSERT IGNORE INTO users_roles (user_id, role_id) VALUES
(3, (SELECT id FROM roles WHERE name = 'ADMIN')),
(4, (SELECT id FROM roles WHERE name = 'MANAGER')),
(5, (SELECT id FROM roles WHERE name = 'MANAGER')),
(6, (SELECT id FROM roles WHERE name = 'MECHANIC')),
(7, (SELECT id FROM roles WHERE name = 'MECHANIC')),
(8, (SELECT id FROM roles WHERE name = 'MECHANIC')),
(9, (SELECT id FROM roles WHERE name = 'CUSTOMER')),
(10, (SELECT id FROM roles WHERE name = 'CUSTOMER')),
(11, (SELECT id FROM roles WHERE name = 'CUSTOMER')),
(12, (SELECT id FROM roles WHERE name = 'CUSTOMER'));

-- ========================================
-- 4. VEHICLES TABLE (10 vehicles for customers)
-- ========================================
INSERT INTO vehicles (user_id, vin, make, model, year, plate_number, color, created_at, updated_at) VALUES
(9, '1HGCM82633A123456', 'Honda', 'Accord', 2021, 'CAA-1234', 'Silver', NOW(), NOW()),
(9, '2T1BURHE5HC123456', 'Toyota', 'Camry', 2020, 'CAB-5678', 'White', NOW(), NOW()),
(10, '3VW2B7AJ5HM123456', 'Volkswagen', 'Jetta', 2019, 'CAC-9012', 'Black', NOW(), NOW()),
(10, '5YFBURHE6HP123456', 'Toyota', 'Corolla', 2022, 'CAD-3456', 'Blue', NOW(), NOW()),
(11, 'WBAJF1C58LB123456', 'BMW', '3 Series', 2021, 'CAE-7890', 'Gray', NOW(), NOW()),
(11, '5J6RM4H78KL123456', 'Honda', 'CR-V', 2020, 'CAF-2345', 'Red', NOW(), NOW()),
(12, '1C4RJFAG5LC123456', 'Jeep', 'Cherokee', 2019, 'CAG-6789', 'Green', NOW(), NOW()),
(12, 'KM8J3CA46KU123456', 'Hyundai', 'Tucson', 2021, 'CAH-0123', 'White', NOW(), NOW()),
(9, 'JM1BL1VF9M1123456', 'Mazda', 'Mazda3', 2022, 'CAI-4567', 'Blue', NOW(), NOW()),
(10, '2G1WC5E34H1123456', 'Chevrolet', 'Impala', 2018, 'CAJ-8901', 'Black', NOW(), NOW());

-- ========================================
-- 5. SERVICES TABLE (10 service requests)
-- ========================================
INSERT INTO services (name, type, status, notes, price, duration, vehicle_id, requested_at, scheduled_at, created_at, updated_at) VALUES
('Oil Change', 'MAINTENANCE', 'SCHEDULED', 'Complete engine oil and filter change', 7500.00, 1.0, 1, NOW(), '2025-11-20 09:00:00', NOW(), NOW()),
('Brake Inspection', 'INSPECTION', 'IN_PROGRESS', 'Full brake system inspection', 5000.00, 1.5, 2, NOW(), '2025-11-18 10:30:00', NOW(), NOW()),
('Tire Rotation', 'MAINTENANCE', 'COMPLETED', 'Rotate all four tires', 3000.00, 0.75, 4, NOW(), '2025-11-15 11:00:00', NOW(), NOW()),
('Engine Diagnostic', 'DIAGNOSTIC', 'PENDING', 'Check engine light diagnostic', 15000.00, 2.0, 3, NOW(), '2025-11-19 14:00:00', NOW(), NOW()),
('Air Filter Replacement', 'MAINTENANCE', 'COMPLETED', 'Replace air filters', 4500.00, 0.5, 10, NOW(), '2025-11-17 14:30:00', NOW(), NOW()),
('Battery Check', 'INSPECTION', 'COMPLETED', 'Battery testing and replacement', 2500.00, 0.5, 8, NOW(), '2025-11-14 13:00:00', NOW(), NOW()),
('Wheel Alignment', 'MAINTENANCE', 'SCHEDULED', 'Four-wheel alignment', 8000.00, 1.5, 5, NOW(), '2025-11-21 08:30:00', NOW(), NOW()),
('Transmission Service', 'MAINTENANCE', 'PENDING', 'Transmission fluid change', 12000.00, 2.0, 9, NOW(), '2025-11-23 10:00:00', NOW(), NOW()),
('AC System Check', 'REPAIR', 'COMPLETED', 'AC recharge and inspection', 9000.00, 1.5, 6, NOW(), '2025-11-16 15:00:00', NOW(), NOW()),
('Full Vehicle Inspection', 'INSPECTION', 'SCHEDULED', '100-point inspection', 6000.00, 3.0, 7, NOW(), '2025-11-22 09:00:00', NOW(), NOW());

-- ========================================
-- 6. APPOINTMENTS TABLE (10 appointments with various statuses)
-- ========================================
INSERT INTO appointments (user_id, service, vehicle, date, time, status, notes, progress, due_date, estimated_hours, actual_hours, assigned_user_id, created_at, updated_at) VALUES
(9, 'Oil Change', 'Honda Accord (CAA-1234)', '2025-11-20', '09:00:00', 'SCHEDULED', 'Customer requested synthetic oil', 0, '2025-11-20', 1.0, 0.0, 6, NOW(), NOW()),
(9, 'Brake Inspection', 'Toyota Camry (CAB-5678)', '2025-11-18', '10:30:00', 'IN_PROGRESS', 'Brake pads need replacement', 60, '2025-11-18', 1.5, 0.8, 7, NOW(), NOW()),
(10, 'Engine Diagnostic', 'Volkswagen Jetta (CAC-9012)', '2025-11-19', '14:00:00', 'PENDING', 'Check engine light is on', 0, '2025-11-19', 2.0, 0.0, 6, NOW(), NOW()),
(10, 'Tire Rotation', 'Toyota Corolla (CAD-3456)', '2025-11-15', '11:00:00', 'COMPLETED', 'All tires rotated successfully', 100, '2025-11-15', 0.75, 0.75, 8, NOW(), NOW()),
(11, 'Wheel Alignment', 'BMW 3 Series (CAE-7890)', '2025-11-21', '08:30:00', 'SCHEDULED', 'Steering pulls to the right', 0, '2025-11-21', 1.5, 0.0, 7, NOW(), NOW()),
(11, 'AC System Check', 'Honda CR-V (CAF-2345)', '2025-11-16', '15:00:00', 'COMPLETED', 'AC recharged and working well', 100, '2025-11-16', 1.5, 1.5, 6, NOW(), NOW()),
(12, 'Full Vehicle Inspection', 'Jeep Cherokee (CAG-6789)', '2025-11-22', '09:00:00', 'SCHEDULED', 'Pre-purchase inspection', 0, '2025-11-22', 3.0, 0.0, 8, NOW(), NOW()),
(12, 'Battery Check', 'Hyundai Tucson (CAH-0123)', '2025-11-14', '13:00:00', 'COMPLETED', 'Battery replaced', 100, '2025-11-14', 0.5, 0.5, 7, NOW(), NOW()),
(9, 'Transmission Service', 'Mazda Mazda3 (CAI-4567)', '2025-11-23', '10:00:00', 'PENDING', 'Transmission fluid change due', 0, '2025-11-23', 2.0, 0.0, 6, NOW(), NOW()),
(10, 'Air Filter Replacement', 'Chevrolet Impala (CAJ-8901)', '2025-11-17', '14:30:00', 'COMPLETED', 'Both filters replaced', 100, '2025-11-17', 0.5, 0.5, 8, NOW(), NOW());

-- ========================================
-- 7. EMPLOYEES TABLE (10 employees)
-- ========================================
INSERT INTO employees (name, employee_no, email, phone, department, status, hire_date, specialization, created_at, updated_at) VALUES
('John Smith', 'EMP001', 'admin@autocare360.com', '0771234567', 'MANAGEMENT', 'ACTIVE', '2020-01-15', 'Administration', NOW(), NOW()),
('Sarah Johnson', 'EMP002', 'manager1@autocare360.com', '0771234568', 'OPERATIONS', 'ACTIVE', '2020-03-20', 'Operations Management', NOW(), NOW()),
('Michael Brown', 'EMP003', 'manager2@autocare360.com', '0771234569', 'SERVICE', 'ACTIVE', '2020-05-10', 'Service Management', NOW(), NOW()),
('David Wilson', 'EMP004', 'mechanic1@autocare360.com', '0771234570', 'TECHNICAL', 'ACTIVE', '2020-06-01', 'Engine Specialist', NOW(), NOW()),
('James Miller', 'EMP005', 'mechanic2@autocare360.com', '0771234571', 'TECHNICAL', 'ACTIVE', '2021-02-15', 'Brake Systems', NOW(), NOW()),
('Robert Davis', 'EMP006', 'mechanic3@autocare360.com', '0771234572', 'TECHNICAL', 'ACTIVE', '2021-08-20', 'General Mechanic', NOW(), NOW()),
('Lisa Anderson', 'EMP007', 'receptionist@autocare360.com', '0771234577', 'FRONT_DESK', 'ACTIVE', '2021-10-01', 'Customer Service', NOW(), NOW()),
('Kevin Martinez', 'EMP008', 'inventory@autocare360.com', '0771234578', 'INVENTORY', 'ACTIVE', '2022-01-15', 'Parts Management', NOW(), NOW()),
('Amanda Garcia', 'EMP009', 'accounts@autocare360.com', '0771234579', 'FINANCE', 'ACTIVE', '2022-03-10', 'Accounting', NOW(), NOW()),
('Christopher Lee', 'EMP010', 'supervisor@autocare360.com', '0771234580', 'TECHNICAL', 'ACTIVE', '2022-06-01', 'Workshop Supervision', NOW(), NOW());

-- ========================================
-- 8. NOTIFICATIONS TABLE (10 notifications)
-- ========================================
INSERT INTO notifications (user_id, type, title, message, is_read, created_at) VALUES
(9, 'APPOINTMENT', 'Appointment Scheduled', 'Your oil change appointment is scheduled for Nov 20, 2025 at 09:00 AM', FALSE, NOW()),
(9, 'APPOINTMENT', 'Service In Progress', 'Your brake inspection is currently in progress', FALSE, NOW()),
(10, 'APPOINTMENT', 'Service Completed', 'Your tire rotation has been completed successfully', TRUE, NOW()),
(10, 'REMINDER', 'Upcoming Appointment', 'Reminder: Engine diagnostic scheduled for tomorrow', FALSE, NOW()),
(11, 'APPOINTMENT', 'Service Completed', 'Your AC system check has been completed', TRUE, NOW()),
(11, 'REMINDER', 'Maintenance Due', 'Your BMW is due for scheduled maintenance', FALSE, NOW()),
(12, 'APPOINTMENT', 'Appointment Confirmed', 'Full vehicle inspection confirmed for Nov 22, 2025', FALSE, NOW()),
(9, 'PROMOTION', 'Special Offer', 'Get 20% off on transmission services this month!', FALSE, NOW()),
(10, 'SERVICE', 'Service Recommendation', 'Based on your mileage, we recommend a brake fluid change', FALSE, NOW()),
(12, 'APPOINTMENT', 'Service Completed', 'Battery check completed. New battery installed.', TRUE, NOW());

-- ========================================
-- 9. TIME_LOGS TABLE (10 time entries)
-- ========================================
INSERT INTO time_logs (appointment_id, employee_id, date, hours, description, status, is_billable, created_at, updated_at) VALUES
(4, 1, '2025-11-15', 0.75, 'Tire rotation completed', 'COMPLETED', TRUE, NOW(), NOW()),
(6, 1, '2025-11-16', 1.50, 'AC system recharged', 'COMPLETED', TRUE, NOW(), NOW()),
(8, 2, '2025-11-14', 0.50, 'Battery testing and replacement', 'COMPLETED', TRUE, NOW(), NOW()),
(10, 3, '2025-11-17', 0.50, 'Air filters replaced', 'COMPLETED', TRUE, NOW(), NOW()),
(2, 2, '2025-11-18', 0.80, 'Brake inspection in progress', 'IN_PROGRESS', TRUE, NOW(), NOW()),
(1, 1, '2025-11-20', 1.00, 'Oil change scheduled', 'SCHEDULED', TRUE, NOW(), NOW()),
(3, 1, '2025-11-19', 2.00, 'Diagnostic pending', 'PENDING', TRUE, NOW(), NOW()),
(5, 2, '2025-11-21', 1.50, 'Wheel alignment scheduled', 'SCHEDULED', TRUE, NOW(), NOW()),
(7, 3, '2025-11-22', 3.00, 'Full inspection scheduled', 'SCHEDULED', TRUE, NOW(), NOW()),
(9, 1, '2025-11-23', 2.00, 'Transmission service pending', 'PENDING', TRUE, NOW(), NOW());

-- ========================================
-- 10. MESSAGES TABLE (10 messages)
-- ========================================
INSERT INTO messages (sender_id, receiver_id, message, is_read, created_at) VALUES
(9, 6, 'What time should I bring my car for the oil change?', TRUE, NOW()),
(6, 9, 'Please arrive at 8:45 AM for your 9:00 AM appointment', FALSE, NOW()),
(10, 7, 'Is the brake inspection done?', TRUE, NOW()),
(7, 10, 'Still working on it, will be done in 30 minutes', FALSE, NOW()),
(11, 4, 'Can I reschedule my appointment to next week?', TRUE, NOW()),
(4, 11, 'Yes, I can schedule you for next Monday at 10 AM', FALSE, NOW()),
(12, 8, 'What did you find during the inspection?', TRUE, NOW()),
(8, 12, 'Everything looks good! Battery was the only issue and we replaced it.', FALSE, NOW()),
(9, 3, 'Do you offer pickup and delivery service?', FALSE, NOW()),
(10, 6, 'Thank you for the excellent service!', FALSE, NOW());

-- ========================================
-- Display counts of inserted records
-- ========================================
SELECT 
    (SELECT COUNT(*) FROM users) as total_users,
    (SELECT COUNT(*) FROM vehicles) as total_vehicles,
    (SELECT COUNT(*) FROM appointments) as total_appointments,
    (SELECT COUNT(*) FROM services) as total_services,
    (SELECT COUNT(*) FROM employees) as total_employees,
    (SELECT COUNT(*) FROM notifications) as total_notifications,
    (SELECT COUNT(*) FROM time_logs) as total_time_logs,
    (SELECT COUNT(*) FROM messages) as total_messages;
