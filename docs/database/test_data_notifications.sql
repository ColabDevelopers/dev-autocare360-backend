-- ============================================
-- AutoCare360 - Test Data for Notifications Module
-- Author: Lasini Induma
-- Date: October 30, 2025
-- ============================================

-- This script adds test data for comprehensive API testing
-- Run this in DBeaver after the application has started and created the schema

-- ============================================
-- 1. Create Test Users (Customer & Employee)
-- ============================================

-- Check if test customer exists
INSERT IGNORE INTO users (email, password_hash, name, phone, status) VALUES
('john.customer@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye8IVO4rWdRdLT0.L6aDXlQOvlHmM9h1y', 'John Customer', '+1-555-111-1111', 'ACTIVE');

-- Check if test employee exists
INSERT IGNORE INTO users (email, password_hash, name, phone, status, employee_no, department) VALUES
('jane.employee@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye8IVO4rWdRdLT0.L6aDXlQOvlHmM9h1y', 'Jane Employee', '+1-555-222-2222', 'ACTIVE', 'EMP001', 'Service Department');

-- Assign CUSTOMER role to John
INSERT IGNORE INTO users_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'john.customer@gmail.com' AND r.name = 'CUSTOMER';

-- Assign EMPLOYEE role to Jane
INSERT IGNORE INTO users_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'jane.employee@gmail.com' AND r.name = 'EMPLOYEE';

-- ============================================
-- 2. Create Sample Notifications for Testing
-- ============================================

-- Get admin user ID (for testing)
SET @admin_id = (SELECT id FROM users WHERE email = 'nimal.admin@gmail.com' LIMIT 1);
SET @customer_id = (SELECT id FROM users WHERE email = 'john.customer@gmail.com' LIMIT 1);
SET @employee_id = (SELECT id FROM users WHERE email = 'jane.employee@gmail.com' LIMIT 1);

-- Service Update Notifications
INSERT INTO notifications (user_id, type, title, message, data, is_read, created_at) VALUES
(@customer_id, 'SERVICE_UPDATE', 'Service Started',
 'Your Oil Change service for vehicle ABC-1234 has been started',
 '{"serviceId":1,"vehicleNumber":"ABC-1234","status":"In Progress","progressPercentage":25,"currentStage":"Initial Inspection"}',
 false, DATE_SUB(NOW(), INTERVAL 2 HOUR));

INSERT INTO notifications (user_id, type, title, message, data, is_read, created_at) VALUES
(@customer_id, 'SERVICE_UPDATE', 'Service Progress Update',
 'Your Oil Change for vehicle ABC-1234 is now 50% complete',
 '{"serviceId":1,"vehicleNumber":"ABC-1234","status":"In Progress","progressPercentage":50,"currentStage":"Oil Draining"}',
 false, DATE_SUB(NOW(), INTERVAL 1 HOUR));

INSERT INTO notifications (user_id, type, title, message, data, is_read, created_at) VALUES
(@customer_id, 'SERVICE_UPDATE', 'Service Completed',
 'Your Oil Change for vehicle ABC-1234 has been completed successfully',
 '{"serviceId":1,"vehicleNumber":"ABC-1234","status":"Completed","progressPercentage":100,"currentStage":"Quality Check Done"}',
 false, DATE_SUB(NOW(), INTERVAL 30 MINUTE));

-- Appointment Notifications
INSERT INTO notifications (user_id, type, title, message, data, is_read, created_at) VALUES
(@customer_id, 'APPOINTMENT_UPDATE', 'Appointment Confirmed',
 'Your appointment for Brake Service on 2025-11-05 at 10:00 AM has been confirmed',
 '{"appointmentId":1,"appointmentDate":"2025-11-05","appointmentTime":"10:00","serviceType":"Brake Service","vehicleNumber":"ABC-1234","status":"Confirmed"}',
 false, DATE_SUB(NOW(), INTERVAL 3 HOUR));

INSERT INTO notifications (user_id, type, title, message, data, is_read, created_at) VALUES
(@customer_id, 'APPOINTMENT_UPDATE', 'Appointment Reminder',
 'Reminder: Your appointment is tomorrow at 10:00 AM',
 '{"appointmentId":1,"appointmentDate":"2025-11-05","appointmentTime":"10:00","serviceType":"Brake Service","vehicleNumber":"ABC-1234","status":"Confirmed"}',
 false, DATE_SUB(NOW(), INTERVAL 10 MINUTE));

-- Project/Modification Request Notifications
INSERT INTO notifications (user_id, type, title, message, data, is_read, created_at) VALUES
(@customer_id, 'PROJECT_UPDATE', 'Modification Request Received',
 'Your modification request "Custom Spoiler Installation" has been received and is under review',
 '{"projectId":1,"projectName":"Custom Spoiler Installation","status":"Pending"}',
 true, DATE_SUB(NOW(), INTERVAL 5 HOUR));

INSERT INTO notifications (user_id, type, title, message, data, is_read, created_at) VALUES
(@customer_id, 'PROJECT_UPDATE', 'Project Request Approved',
 'Your modification request "Custom Spoiler Installation" has been approved',
 '{"projectId":1,"projectName":"Custom Spoiler Installation","status":"Approved"}',
 false, DATE_SUB(NOW(), INTERVAL 4 HOUR));

-- System Announcements
INSERT INTO notifications (user_id, type, title, message, data, is_read, created_at) VALUES
(@customer_id, 'ANNOUNCEMENT', 'Service Center Holiday Notice',
 'Our service center will be closed on November 10th for maintenance',
 '{"announcementType":"Holiday","validUntil":"2025-11-10"}',
 false, DATE_SUB(NOW(), INTERVAL 6 HOUR));

-- Test notification for admin
INSERT INTO notifications (user_id, type, title, message, data, is_read, created_at) VALUES
(@admin_id, 'TEST', 'System Test Notification',
 'This is a test notification for the admin user',
 '{"testData":"Admin test data"}',
 false, DATE_SUB(NOW(), INTERVAL 15 MINUTE));

-- ============================================
-- 3. Create Notification Preferences
-- ============================================

-- Preferences for admin user (all enabled)
INSERT INTO notification_preferences (user_id, email_notifications, push_notifications, service_updates, appointment_reminders)
VALUES (@admin_id, true, true, true, true)
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- Preferences for customer (selective)
INSERT INTO notification_preferences (user_id, email_notifications, push_notifications, service_updates, appointment_reminders)
VALUES (@customer_id, true, true, true, false)
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- Preferences for employee (email only)
INSERT INTO notification_preferences (user_id, email_notifications, push_notifications, service_updates, appointment_reminders)
VALUES (@employee_id, true, false, false, false)
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- ============================================
-- 4. Verification Queries
-- ============================================

-- View all test users with their roles
SELECT
    u.id,
    u.email,
    u.name,
    u.status,
    GROUP_CONCAT(r.name) as roles
FROM users u
LEFT JOIN users_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
GROUP BY u.id, u.email, u.name, u.status
ORDER BY u.id;

-- View all notifications
SELECT
    n.id,
    u.email as user_email,
    n.type,
    n.title,
    n.is_read,
    n.created_at
FROM notifications n
JOIN users u ON n.user_id = u.id
ORDER BY n.created_at DESC;

-- Count notifications by type
SELECT type, COUNT(*) as count, SUM(is_read) as read_count, SUM(!is_read) as unread_count
FROM notifications
GROUP BY type;

-- View notification preferences
SELECT
    u.email,
    np.email_notifications,
    np.push_notifications,
    np.service_updates,
    np.appointment_reminders
FROM notification_preferences np
JOIN users u ON np.user_id = u.id;

-- ============================================
-- NOTES:
-- ============================================
-- Password for all test users: "password" (bcrypt hash: $2a$10$N9qo8uLOickgx2ZMRZoMye8IVO4rWdRdLT0.L6aDXlQOvlHmM9h1y)
--
-- Test Users:
-- 1. nimal.admin@gmail.com / password (ADMIN) - Created by DataSeeder
-- 2. john.customer@gmail.com / password (CUSTOMER) - Created by this script
-- 3. jane.employee@gmail.com / password (EMPLOYEE) - Created by this script
--
-- You can login as any of these users to test notifications
-- Each user will see their own notifications based on user_id

