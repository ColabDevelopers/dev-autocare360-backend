-- Spring Boot initializer default name: schema.sql
-- Ensures vehicles and services tables exist with attachments column

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

-- If the services table already exists but is missing columns (e.g. attachments), add them
ALTER TABLE services ADD COLUMN IF NOT EXISTS attachments TEXT;
