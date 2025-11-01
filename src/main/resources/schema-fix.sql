-- Startup initializer to ensure required tables exist when Flyway history is inconsistent
-- This will run when spring.sql.init.mode=always is enabled at runtime.

CREATE TABLE IF NOT EXISTS vehicles (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  make VARCHAR(255),
  model VARCHAR(255),
  year INT,
  vin VARCHAR(255),
  license_plate VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS services (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  vehicle_id BIGINT NOT NULL,
  attachments TEXT,
  description VARCHAR(1000),
  status VARCHAR(50),
  cost DECIMAL(10,2) DEFAULT 0.00,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_services_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE
);
