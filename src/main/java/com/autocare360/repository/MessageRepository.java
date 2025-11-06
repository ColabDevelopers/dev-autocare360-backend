package com.autocare360.repository;

import com.autocare360.entity.Message;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

  /**
   * Get all messages between two users (conversation) - Customer messages: senderId = customerId
   * AND receiverId IS NULL (broadcast) - Employee replies: senderId = employeeId AND receiverId =
   * customerId
   */
  @Query(
      "SELECT m FROM Message m WHERE "
          + "((m.senderId = :userId1 AND m.receiverId = :userId2) OR "
          + " (m.senderId = :userId2 AND m.receiverId IS NULL) OR "
          + " (m.senderId = :userId2 AND m.receiverId = :userId1)) "
          + "ORDER BY m.createdAt ASC")
  List<Message> findConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

  /** Get all customer conversations for employees (shows all customers who sent messages) */
  @Query(
      "SELECT DISTINCT m.senderId FROM Message m "
          + "JOIN User u ON m.senderId = u.id "
          + "JOIN u.roles r "
          + "WHERE r.name = 'CUSTOMER' "
          + "AND (m.receiverId IS NULL OR m.receiverId IN "
          + "  (SELECT u2.id FROM User u2 JOIN u2.roles r2 ON r2.name = 'EMPLOYEE')) "
          + "ORDER BY (SELECT MAX(m2.createdAt) FROM Message m2 WHERE m2.senderId = m.senderId) DESC")
  List<Long> findAllCustomersWithMessages();

  /** Get all conversations for a user (list of unique conversation partners) */
  @Query(
      "SELECT DISTINCT CASE "
          + "WHEN m.senderId = :userId THEN m.receiverId "
          + "ELSE m.senderId END "
          + "FROM Message m WHERE m.senderId = :userId OR m.receiverId = :userId OR m.receiverId IS NULL")
  List<Long> findConversationPartners(@Param("userId") Long userId);

  /** Get unread message count for a user */
  @Query("SELECT COUNT(m) FROM Message m WHERE m.receiverId = :userId AND m.isRead = false")
  Long countUnreadMessages(@Param("userId") Long userId);

  /**
   * Get unread messages from a specific sender Includes broadcast messages (receiverId IS NULL) for
   * employees
   */
  @Query(
      "SELECT COUNT(m) FROM Message m WHERE m.senderId = :senderId "
          + "AND m.isRead = false AND (m.receiverId = :receiverId OR m.receiverId IS NULL)")
  Long countUnreadMessagesFrom(
      @Param("receiverId") Long receiverId, @Param("senderId") Long senderId);

  /**
   * Get the last message in a conversation - Customer messages: senderId = customerId AND
   * receiverId IS NULL - Employee replies: senderId = employeeId AND receiverId = customerId
   */
  @Query(
      "SELECT m FROM Message m WHERE "
          + "((m.senderId = :userId1 AND m.receiverId = :userId2) OR "
          + " (m.senderId = :userId2 AND m.receiverId IS NULL) OR "
          + " (m.senderId = :userId2 AND m.receiverId = :userId1)) "
          + "ORDER BY m.createdAt DESC LIMIT 1")
  Message findLastMessage(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

  /** Mark all messages from a sender as read Handles both direct messages and broadcast messages */
  @Modifying
  @Query(
      "UPDATE Message m SET m.isRead = true WHERE "
          + "m.senderId = :senderId AND m.isRead = false "
          + "AND (m.receiverId = :receiverId OR m.receiverId IS NULL)")
  void markMessagesAsRead(@Param("receiverId") Long receiverId, @Param("senderId") Long senderId);

  /**
   * Get ALL messages for a specific customer Returns: 1) Messages customer sent (receiverId IS NULL
   * or to specific employee) 2) Messages employees sent to this customer (receiverId = customerId)
   */
  @Query(
      "SELECT m FROM Message m WHERE "
          + "m.senderId = :customerId OR m.receiverId = :customerId "
          + "ORDER BY m.createdAt ASC")
  List<Message> findAllCustomerMessages(@Param("customerId") Long customerId);
}
