-- Create work_items table
CREATE TABLE IF NOT EXISTS work_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(250) NOT NULL,
    type VARCHAR(50) NOT NULL COMMENT 'appointment, project',
    status VARCHAR(40) NOT NULL DEFAULT 'pending' COMMENT 'pending, in_progress, completed, cancelled',
    customer_id BIGINT,
    description TEXT,
    estimated_hours DOUBLE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    scheduled_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    INDEX idx_customer_id (customer_id),
    INDEX idx_status (status),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;