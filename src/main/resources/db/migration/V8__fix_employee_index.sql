-- Add index on employee_id in appointments table for better join performance
ALTER TABLE appointments ADD INDEX idx_employee_id (employee_id);