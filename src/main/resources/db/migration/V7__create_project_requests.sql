-- Create project_requests table for modification and project requests feature
CREATE TABLE IF NOT EXISTS project_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    project_name VARCHAR(255) NOT NULL,
    project_type VARCHAR(100) NOT NULL,
    vehicle_details VARCHAR(500),
    description TEXT,
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    estimated_cost DECIMAL(10,2),
    estimated_duration_days INT,
    approved_cost DECIMAL(10,2),
    actual_cost DECIMAL(10,2),
    start_date TIMESTAMP NULL,
    completion_date TIMESTAMP NULL,
    admin_notes TEXT,
    rejection_reason TEXT,
    attachments JSON,
    assigned_employee_id BIGINT,
    requested_at TIMESTAMP NULL,
    reviewed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_project_requests_customer 
        FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_project_requests_employee 
        FOREIGN KEY (assigned_employee_id) REFERENCES employees(id) ON DELETE SET NULL,
    
    -- Check constraints for enum values
    CONSTRAINT chk_project_priority 
        CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    CONSTRAINT chk_project_status 
        CHECK (status IN ('PENDING', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT chk_project_type 
        CHECK (project_type IN ('MODIFICATION', 'CUSTOM_WORK', 'UPGRADE', 'REPAIR'))
);

-- Create indexes for better query performance
CREATE INDEX idx_project_requests_customer_id ON project_requests(customer_id);
CREATE INDEX idx_project_requests_status ON project_requests(status);
CREATE INDEX idx_project_requests_assigned_employee ON project_requests(assigned_employee_id);
CREATE INDEX idx_project_requests_created_at ON project_requests(created_at);
CREATE INDEX idx_project_requests_project_type ON project_requests(project_type);
CREATE INDEX idx_project_requests_priority ON project_requests(priority);

-- Create composite indexes for common query patterns
CREATE INDEX idx_project_requests_customer_status ON project_requests(customer_id, status);
CREATE INDEX idx_project_requests_status_created ON project_requests(status, created_at);
CREATE INDEX idx_project_requests_employee_status ON project_requests(assigned_employee_id, status);