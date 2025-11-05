-- Idempotent version of admin analytics setup that safely checks existence first
-- and only creates/alters what's missing, preserving existing data

-- Function to check if a table exists
DROP PROCEDURE IF EXISTS create_if_not_exists;
DELIMITER //
CREATE PROCEDURE create_if_not_exists()
BEGIN
    -- Create admin_analytics if not exists
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'admin_analytics') THEN
        CREATE TABLE admin_analytics (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            report_date DATE NOT NULL,
            report_type VARCHAR(50) NOT NULL,
            total_employees INT NOT NULL,
            active_employees INT NOT NULL,
            total_appointments INT NOT NULL,
            completed_appointments INT NOT NULL,
            pending_appointments INT NOT NULL,
            in_progress_appointments INT NOT NULL,
            average_completion_time DECIMAL(10,2),
            employee_utilization_rate DECIMAL(5,2),
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            INDEX idx_report_date (report_date),
            INDEX idx_report_type (report_type)
        );
    END IF;

    -- Create workload_metrics if not exists
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'workload_metrics') THEN
        CREATE TABLE workload_metrics (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            employee_id BIGINT NOT NULL,
            date DATE NOT NULL,
            total_tasks INT NOT NULL DEFAULT 0,
            completed_tasks INT NOT NULL DEFAULT 0,
            total_hours_logged DECIMAL(10,2) NOT NULL DEFAULT 0,
            efficiency_rate DECIMAL(5,2),
            utilization_rate DECIMAL(5,2),
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
            INDEX idx_employee_date (employee_id, date),
            INDEX idx_date (date)
        );
    END IF;

    -- Create task_assignments if not exists
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'task_assignments') THEN
        CREATE TABLE task_assignments (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            appointment_id BIGINT NOT NULL,
            assigned_by_id BIGINT NOT NULL,
            assigned_to_id BIGINT NOT NULL,
            assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
            priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
            notes TEXT,
            reviewed_at TIMESTAMP NULL,
            FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE,
            FOREIGN KEY (assigned_by_id) REFERENCES users(id) ON DELETE CASCADE,
            FOREIGN KEY (assigned_to_id) REFERENCES employees(id) ON DELETE CASCADE,
            INDEX idx_assigned_to (assigned_to_id, status),
            INDEX idx_appointment (appointment_id)
        );
    END IF;

    -- Add columns to appointments table if they don't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'appointments' AND column_name = 'progress') THEN
        ALTER TABLE appointments ADD COLUMN progress INT DEFAULT 0;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'appointments' AND column_name = 'priority') THEN
        ALTER TABLE appointments ADD COLUMN priority VARCHAR(20) DEFAULT 'MEDIUM';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'appointments' AND column_name = 'due_date') THEN
        ALTER TABLE appointments ADD COLUMN due_date DATE NULL;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'appointments' AND column_name = 'special_instructions') THEN
        ALTER TABLE appointments ADD COLUMN special_instructions TEXT NULL;
    END IF;

    -- Create department_stats view (only if it doesn't contain our expected columns)
    -- First drop the view if it exists but doesn't match our requirements
    IF EXISTS (
        SELECT 1 FROM information_schema.views 
        WHERE table_schema = DATABASE() 
        AND table_name = 'department_stats'
        AND NOT EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_schema = DATABASE() 
            AND table_name = 'department_stats' 
            AND column_name IN ('total_employees', 'total_assignments', 'avg_efficiency', 'avg_utilization')
        )
    ) THEN
        DROP VIEW department_stats;
    END IF;

    -- Create the view if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.views WHERE table_schema = DATABASE() AND table_name = 'department_stats') THEN
        CREATE VIEW department_stats AS
        SELECT 
            e.department,
            COUNT(DISTINCT e.id) as total_employees,
            COUNT(DISTINCT a.id) as total_assignments,
            AVG(wm.efficiency_rate) as avg_efficiency,
            AVG(wm.utilization_rate) as avg_utilization
        FROM employees e
        LEFT JOIN workload_metrics wm ON e.id = wm.employee_id
        LEFT JOIN appointments a ON e.id = a.employee_id
        WHERE e.status = 'ACTIVE'
        GROUP BY e.department;
    END IF;
END //
DELIMITER ;

-- Execute the procedure
CALL create_if_not_exists();

-- Clean up
DROP PROCEDURE IF EXISTS create_if_not_exists;