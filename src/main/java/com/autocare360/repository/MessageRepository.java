package com.autocare360.repository;

import com.autocare360.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    /**
     * Get all messages between two users (conversation)
     * Also handles NULL receiverId for customer-to-employee-pool messages
     */
    @Query("SELECT m FROM Message m WHERE " +
           "(m.senderId = :userId1 AND (m.receiverId = :userId2 OR m.receiverId IS NULL)) OR " +
           "(m.senderId = :userId2 AND (m.receiverId = :userId1 OR m.receiverId IS NULL)) " +
           "ORDER BY m.createdAt ASC")
    List<Message> findConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
    
    /**
     * Get all customer conversations for employees (shows all customers who sent messages)
     */
    @Query("SELECT DISTINCT m.senderId FROM Message m " +
           "JOIN User u ON m.senderId = u.id " +
           "JOIN u.roles r " +
           "WHERE r.name = 'CUSTOMER' " +
           "AND (m.receiverId IS NULL OR m.receiverId IN " +
           "  (SELECT u2.id FROM User u2 JOIN u2.roles r2 ON r2.name = 'EMPLOYEE')) " +
           "ORDER BY (SELECT MAX(m2.createdAt) FROM Message m2 WHERE m2.senderId = m.senderId) DESC")
    List<Long> findAllCustomersWithMessages();
    
    /**
     * Get all conversations for a user (list of unique conversation partners)
     */
    @Query("SELECT DISTINCT CASE " +
           "WHEN m.senderId = :userId THEN m.receiverId " +
           "ELSE m.senderId END " +
           "FROM Message m WHERE m.senderId = :userId OR m.receiverId = :userId OR m.receiverId IS NULL")
    List<Long> findConversationPartners(@Param("userId") Long userId);
    
    /**
     * Get unread message count for a user
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiverId = :userId AND m.isRead = false")
    Long countUnreadMessages(@Param("userId") Long userId);
    
    /**
     * Get unread messages from a specific sender
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiverId = :receiverId " +
           "AND m.senderId = :senderId AND m.isRead = false")
    Long countUnreadMessagesFrom(@Param("receiverId") Long receiverId, @Param("senderId") Long senderId);
    
    /**
     * Get the last message in a conversation (including NULL receiverId)
     */
    @Query("SELECT m FROM Message m WHERE " +
           "(m.senderId = :userId1 AND (m.receiverId = :userId2 OR m.receiverId IS NULL)) OR " +
           "(m.senderId = :userId2 AND (m.receiverId = :userId1 OR m.receiverId IS NULL)) " +
           "ORDER BY m.createdAt DESC LIMIT 1")
    Message findLastMessage(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
    
    /**
     * Mark all messages from a sender as read
     */
    @Query("UPDATE Message m SET m.isRead = true WHERE m.receiverId = :receiverId " +
           "AND m.senderId = :senderId AND m.isRead = false")
    void markMessagesAsRead(@Param("receiverId") Long receiverId, @Param("senderId") Long senderId);
}
