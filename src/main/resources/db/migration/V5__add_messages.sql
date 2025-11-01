-- Create messages table for real-time customer-employee messaging
-- Customers send to shared employee pool (receiverId can be NULL for broadcast or specific employee)
CREATE TABLE IF NOT EXISTS messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NULL,  -- NULL means sent to employee pool, specific ID means assigned to that employee
    message TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE,
    read_by_employee_id BIGINT NULL,  -- Which employee read/responded to this
    
    CONSTRAINT fk_messages_sender
        FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_receiver
        FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_read_by
        FOREIGN KEY (read_by_employee_id) REFERENCES users(id) ON DELETE SET NULL,
        
    INDEX idx_sender_receiver (sender_id, receiver_id),
    INDEX idx_receiver_read (receiver_id, is_read),
    INDEX idx_sender_created (sender_id, created_at DESC),
    INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
