-- Create job_assignments table
CREATE TABLE IF NOT EXISTS job_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    work_item_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    role_on_job VARCHAR(100) COMMENT 'Technician, Lead, etc.',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    INDEX idx_work_item_id (work_item_id),
    INDEX idx_employee_id (employee_id),
    INDEX idx_active (is_active),
    INDEX idx_employee_active (employee_id, is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;