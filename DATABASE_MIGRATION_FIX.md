# Database Migration Fix

## Problem
Flyway checksum mismatch for V5__add_messages.sql because the migration file was modified after being applied to the database.

## Solution Options

### Option 1: Flyway Repair (Recommended if schema is correct)
If the `messages` table already has the correct structure, just update Flyway's checksum:

```bash
cd dev-autocare360-backend
.\mvnw flyway:repair
```

Then restart the backend:
```bash
.\mvnw spring-boot:run
```

### Option 2: Manual Database Update (If table structure is wrong)
If the messages table is missing `read_by_employee_id` or `receiver_id` is NOT NULL, run this SQL manually:

```sql
-- Connect to your MySQL database on Aiven
USE defaultdb;

-- Check current table structure
DESCRIBE messages;

-- If read_by_employee_id is missing, add it:
ALTER TABLE messages ADD COLUMN read_by_employee_id BIGINT NULL;
ALTER TABLE messages ADD CONSTRAINT fk_messages_read_by 
  FOREIGN KEY (read_by_employee_id) REFERENCES users(id) ON DELETE SET NULL;

-- If receiver_id is NOT NULL, change it to allow NULL:
ALTER TABLE messages MODIFY receiver_id BIGINT NULL;

-- Update Flyway checksum
```

After manual update, still run:
```bash
.\mvnw flyway:repair
```

### Option 3: Create New Migration (Clean approach)
Create V6__update_messages_for_shared_inbox.sql with ALTER TABLE statements.

## Verification

After applying fix, check:
```sql
DESCRIBE messages;
```

Should show:
```
+---------------------+------------+------+-----+-------------------+
| Field               | Type       | Null | Key | Default           |
+---------------------+------------+------+-----+-------------------+
| id                  | bigint     | NO   | PRI | NULL              |
| sender_id           | bigint     | NO   | MUL | NULL              |
| receiver_id         | bigint     | YES  | MUL | NULL              |  <-- Should allow NULL
| message             | text       | NO   |     | NULL              |
| created_at          | timestamp  | YES  |     | CURRENT_TIMESTAMP |
| is_read             | tinyint(1) | YES  |     | 0                 |
| read_by_employee_id | bigint     | YES  | MUL | NULL              |  <-- Should exist
+---------------------+------------+------+-----+-------------------+
```
