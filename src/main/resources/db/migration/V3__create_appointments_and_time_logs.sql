CREATE TABLE employees (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  employee_no VARCHAR(20) UNIQUE,
  department VARCHAR(100),
  email VARCHAR(255) UNIQUE,
  phone VARCHAR(50),
  status VARCHAR(50) DEFAULT 'ACTIVE',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE appointments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  service VARCHAR(255) NOT NULL,
  vehicle VARCHAR(255) NOT NULL,
  date DATE NOT NULL,
  time_col VARCHAR(8) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  notes VARCHAR(1000),
  technician VARCHAR(255),
  employee_id BIGINT,
  estimated_hours DECIMAL(5,2),
  actual_hours DECIMAL(5,2) DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_appointments_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_appointments_employee FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE TABLE time_logs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  employee_id BIGINT NOT NULL,
  appointment_id BIGINT NOT NULL,
  date DATE NOT NULL,
  hours DECIMAL(5,2) NOT NULL,
  description VARCHAR(2000) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
  is_billable TINYINT(1) DEFAULT 1,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_time_logs_employee FOREIGN KEY (employee_id) REFERENCES employees(id),
  CONSTRAINT fk_time_logs_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id)
);