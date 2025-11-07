-- =====================================================
-- Flyway Migration V7: Workload Monitoring Features
-- Description: Add columns and sample data for workload monitoring
-- =====================================================

-- Add missing columns to appointments table
ALTER TABLE appointments 
ADD COLUMN IF NOT EXISTS progress INT DEFAULT 0 
COMMENT 'Task progress percentage 0-100';

ALTER TABLE appointments 
ADD COLUMN IF NOT EXISTS priority VARCHAR(20) DEFAULT 'MEDIUM' 
COMMENT 'Task priority: LOW, MEDIUM, HIGH, URGENT';

ALTER TABLE appointments 
ADD COLUMN IF NOT EXISTS due_date DATETIME NULL 
COMMENT 'Task due date';

ALTER TABLE appointments 
ADD COLUMN IF NOT EXISTS completed_at DATETIME NULL 
COMMENT 'Completion timestamp';

ALTER TABLE appointments 
ADD COLUMN IF NOT EXISTS estimated_hours INT DEFAULT 2 
COMMENT 'Estimated hours to complete';

-- Add indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_appointments_employee_status 
ON appointments(employee_id, status);

CREATE INDEX IF NOT EXISTS idx_appointments_unassigned 
ON appointments(employee_id, status) 
WHERE employee_id IS NULL;

CREATE INDEX IF NOT EXISTS idx_appointments_date_range 
ON appointments(appointment_date, status);

CREATE INDEX IF NOT EXISTS idx_appointments_priority 
ON appointments(priority);

-- Add index for time logs
CREATE INDEX IF NOT EXISTS idx_time_logs_employee_date 
ON time_logs(employee_id, date);

-- Update existing records with default values
UPDATE appointments 
SET priority = 'MEDIUM' 
WHERE priority IS NULL;

UPDATE appointments 
SET progress = 0 
WHERE progress IS NULL;

UPDATE appointments 
SET estimated_hours = 2 
WHERE estimated_hours IS NULL;

-- Add status to employees if not exists
ALTER TABLE employees 
ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'AVAILABLE' 
COMMENT 'Employee status: AVAILABLE, BUSY, OVERLOADED, ON_LEAVE';

-- Create index on employee status
CREATE INDEX IF NOT EXISTS idx_employees_status 
ON employees(status, active);

-- Insert sample employees for testing (use INSERT IGNORE to avoid duplicates)
INSERT IGNORE INTO employees (id, name, email, phone, department, position, active, status, hire_date, created_at, updated_at) 
VALUES 
(100, 'John Silva', 'john.silva@autocare360.lk', '0771234567', 'Mechanical', 'Senior Technician', TRUE, 'AVAILABLE', NOW(), NOW(), NOW()),
(101, 'Sarah Fernando', 'sarah.fernando@autocare360.lk', '0772345678', 'Electrical', 'Technician', TRUE, 'AVAILABLE', NOW(), NOW(), NOW()),
(102, 'Michael Perera', 'michael.perera@autocare360.lk', '0773456789', 'Diagnostic', 'Lead Technician', TRUE, 'BUSY', NOW(), NOW(), NOW()),
(103, 'Priya Jayawardena', 'priya.jay@autocare360.lk', '0774567890', 'Detailing', 'Detailing Specialist', TRUE, 'AVAILABLE', NOW(), NOW(), NOW()),
(104, 'Rohan Kumar', 'rohan.kumar@autocare360.lk', '0775678901', 'Mechanical', 'Technician', TRUE, 'AVAILABLE', NOW(), NOW(), NOW());

-- Create sample unassigned appointments for testing
-- First, find a customer ID (assuming at least one exists)
SET @customer_id = (SELECT id FROM users WHERE role_id = (SELECT id FROM roles WHERE name = 'CUSTOMER') LIMIT 1);

-- Only insert if customer exists
INSERT INTO appointments (customer_id, appointment_date, service_type, status, priority, progress, due_date, estimated_hours, description, created_at, updated_at)
SELECT 
    @customer_id,
    DATE_ADD(NOW(), INTERVAL 1 DAY) as appointment_date,
    'Oil Change & Filter Replacement',
    'PENDING',
    'MEDIUM',
    0,
    DATE_ADD(NOW(), INTERVAL 2 DAY),
    2,
    'Regular oil change and filter replacement service',
    NOW(),
    NOW()
WHERE @customer_id IS NOT NULL
AND NOT EXISTS (SELECT 1 FROM appointments WHERE service_type = 'Oil Change & Filter Replacement' AND employee_id IS NULL LIMIT 1);

INSERT INTO appointments (customer_id, appointment_date, service_type, status, priority, progress, due_date, estimated_hours, description, created_at, updated_at)
SELECT 
    @customer_id,
    DATE_ADD(NOW(), INTERVAL 2 DAY),
    'Brake System Repair',
    'PENDING',
    'HIGH',
    0,
    DATE_ADD(NOW(), INTERVAL 3 DAY),
    4,
    'Complete brake system inspection and repair',
    NOW(),
    NOW()
WHERE @customer_id IS NOT NULL
AND NOT EXISTS (SELECT 1 FROM appointments WHERE service_type = 'Brake System Repair' AND employee_id IS NULL LIMIT 1);

INSERT INTO appointments (customer_id, appointment_date, service_type, status, priority, progress, due_date, estimated_hours, description, created_at, updated_at)
SELECT 
    @customer_id,
    DATE_ADD(NOW(), INTERVAL 3 DAY),
    'Custom Paint Job',
    'PENDING',
    'HIGH',
    0,
    DATE_ADD(NOW(), INTERVAL 5 DAY),
    40,
    'Full custom paint job with customer design',
    NOW(),
    NOW()
WHERE @customer_id IS NOT NULL
AND NOT EXISTS (SELECT 1 FROM appointments WHERE service_type = 'Custom Paint Job' AND employee_id IS NULL LIMIT 1);

-- Assign some appointments to employees for testing
UPDATE appointments 
SET employee_id = 100, status = 'IN_PROGRESS', progress = 50 
WHERE service_type = 'Engine Diagnostic' 
AND employee_id IS NULL 
LIMIT 1;

UPDATE appointments 
SET employee_id = 102, status = 'IN_PROGRESS', progress = 75 
WHERE service_type = 'AC Service' 
AND employee_id IS NULL 
LIMIT 1;

-- Create some sample time logs
INSERT IGNORE INTO time_logs (employee_id, appointment_id, date, hours_logged, work_description, activity, created_at, updated_at)
SELECT 
    100,
    (SELECT id FROM appointments WHERE employee_id = 100 LIMIT 1),
    CURDATE(),
    8.0,
    'Engine diagnostic and minor repairs',
    'DIAGNOSTIC',
    NOW(),
    NOW()
WHERE EXISTS (SELECT 1 FROM employees WHERE id = 100)
AND EXISTS (SELECT 1 FROM appointments WHERE employee_id = 100);

INSERT IGNORE INTO time_logs (employee_id, appointment_id, date, hours_logged, work_description, activity, created_at, updated_at)
SELECT 
    102,
    (SELECT id FROM appointments WHERE employee_id = 102 LIMIT 1),
    CURDATE(),
    6.5,
    'AC system repair and testing',
    'REPAIR',
    NOW(),
    NOW()
WHERE EXISTS (SELECT 1 FROM employees WHERE id = 102)
AND EXISTS (SELECT 1 FROM appointments WHERE employee_id = 102);

-- Update employee statuses based on workload
UPDATE employees e
SET status = CASE 
    WHEN (SELECT COUNT(*) FROM appointments WHERE employee_id = e.id AND status IN ('PENDING', 'IN_PROGRESS')) > 5 THEN 'OVERLOADED'
    WHEN (SELECT COUNT(*) FROM appointments WHERE employee_id = e.id AND status IN ('PENDING', 'IN_PROGRESS')) > 3 THEN 'BUSY'
    ELSE 'AVAILABLE'
END
WHERE active = TRUE;

-- Add comments for clarity
ALTER TABLE appointments 
MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING' 
COMMENT 'Appointment status: PENDING, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED';

ALTER TABLE employees 
MODIFY COLUMN status VARCHAR(20) DEFAULT 'AVAILABLE' 
COMMENT 'Employee availability: AVAILABLE, BUSY, OVERLOADED, ON_LEAVE';