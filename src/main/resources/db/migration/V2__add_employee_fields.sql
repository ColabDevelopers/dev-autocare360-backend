-- Add employee-specific fields to users table (hosted DB)
ALTER TABLE users ADD COLUMN employee_no VARCHAR(20) UNIQUE;
ALTER TABLE users ADD COLUMN department VARCHAR(100);


