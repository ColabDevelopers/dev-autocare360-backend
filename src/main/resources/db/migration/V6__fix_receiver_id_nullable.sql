-- Fix receiver_id to allow NULL for broadcast messages
ALTER TABLE messages MODIFY receiver_id BIGINT NULL;
