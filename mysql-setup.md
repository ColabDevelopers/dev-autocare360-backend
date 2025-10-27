# MySQL Database Setup for AutoCare360

## Prerequisites
- MySQL Server 8.0+ installed and running on localhost:3306
- MySQL client, MySQL Workbench, or command line access

## Quick Setup Steps

### 1. Install MySQL (if not already installed)
Download from: https://dev.mysql.com/downloads/mysql/

### 2. Create Database
Connect to MySQL and run:
```sql
-- Connect as root
mysql -u root -p

-- Create database
CREATE DATABASE autocare360_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user (optional - you can use root)
CREATE USER 'autocare360_user'@'localhost' IDENTIFIED BY 'autocare360_pass';
GRANT ALL PRIVILEGES ON autocare360_db.* TO 'autocare360_user'@'localhost';
FLUSH PRIVILEGES;

-- Verify database creation
SHOW DATABASES;
USE autocare360_db;
```

### 3. Update Application Configuration
The `application.properties` is already configured for:
- **Database:** `autocare360_db`
- **Username:** `root`
- **Password:** `1234`
- **Port:** `3306`

**Change the password in application.properties if your MySQL root password is different!**

### 4. Start the Application
```bash
./mvnw spring-boot:run
```

## What Happens on First Run
- Spring Boot will automatically create all tables (users, appointments, schedules)
- Sample data will be inserted via DataInitializer
- You can view data using MySQL Workbench or command line

## Viewing Data
```sql
-- Connect to database
mysql -u root -p autocare360_db

-- View tables
SHOW TABLES;

-- View sample data
SELECT * FROM users;
SELECT * FROM appointments;
SELECT * FROM schedules;
```

## Troubleshooting
- **Connection refused:** Make sure MySQL is running
- **Access denied:** Check username/password in application.properties
- **Database doesn't exist:** Run the CREATE DATABASE command above
