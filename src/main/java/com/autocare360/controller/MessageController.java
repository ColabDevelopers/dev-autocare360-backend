package com.autocare360.controller;

import com.autocare360.dto.ConversationDTO;
import com.autocare360.dto.MessageDTO;
import com.autocare360.dto.SendMessageRequest;
import com.autocare360.dto.UserSearchDTO;
import com.autocare360.service.MessageService;
import com.autocare360.util.AuthUtil;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

  private final MessageService messageService;
  private final AuthUtil authUtil;

  /**
   * Send a message via REST API For customers: receiverId can be NULL (broadcast to employee pool)
   * For employees: receiverId must be the customer ID
   */
  @PostMapping
  public ResponseEntity<MessageDTO> sendMessage(
      @RequestBody SendMessageRequest request, Authentication authentication) {

    log.info("REST: Sending message to user {}", request.getReceiverId());

    Long senderId = authUtil.getUserIdFromAuth(authentication);
    MessageDTO message =
        messageService.sendMessage(
            senderId,
            request.getReceiverId(), // Can be NULL for customer messages
            request.getMessage());

    return ResponseEntity.ok(message);
  }

  /** Send a message via WebSocket */
  @MessageMapping("/chat/send")
  public void sendMessageWS(@Payload SendMessageRequest request, Principal principal) {
    log.info("WebSocket: Sending message from {}", principal.getName());

    // Extract user ID from principal (email)
    Long senderId = authUtil.getUserIdFromEmail(principal.getName());

    messageService.sendMessage(senderId, request.getReceiverId(), request.getMessage());
  }

  /** Get conversation with another user */
  @GetMapping("/conversation/{otherUserId}")
  public ResponseEntity<List<MessageDTO>> getConversation(
      @PathVariable Long otherUserId, Authentication authentication) {

    log.info("Getting conversation with user {}", otherUserId);

    Long userId = authUtil.getUserIdFromAuth(authentication);
    List<MessageDTO> messages = messageService.getConversation(userId, otherUserId);

    return ResponseEntity.ok(messages);
  }

  /** Get all conversations for current user */
  @GetMapping("/conversations")
  public ResponseEntity<List<ConversationDTO>> getConversations(Authentication authentication) {
    log.info("Getting all conversations");

    Long userId = authUtil.getUserIdFromAuth(authentication);
    List<ConversationDTO> conversations = messageService.getConversations(userId);

    return ResponseEntity.ok(conversations);
  }

  /** Mark messages as read */
  @PutMapping("/read/{senderId}")
  public ResponseEntity<Void> markAsRead(
      @PathVariable Long senderId, Authentication authentication) {

    log.info("Marking messages from user {} as read", senderId);

    Long receiverId = authUtil.getUserIdFromAuth(authentication);
    messageService.markMessagesAsRead(receiverId, senderId);

    return ResponseEntity.ok().build();
  }

  /** Get unread message count */
  @GetMapping("/unread/count")
  public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
    log.info("Getting unread message count");

    Long userId = authUtil.getUserIdFromAuth(authentication);
    Long count = messageService.getUnreadCount(userId);

    return ResponseEntity.ok(count);
  }

  /**
   * Search users by role (for starting new conversations) Customers can search EMPLOYEE role,
   * Employees can search CUSTOMER role
   */
  @GetMapping("/search-users")
  public ResponseEntity<List<UserSearchDTO>> searchUsers(
      @RequestParam String role,
      @RequestParam(required = false) String query,
      Authentication authentication) {

    log.info("Searching users with role: {} and query: {}", role, query);

    List<UserSearchDTO> users = messageService.searchUsersByRole(role, query);

    return ResponseEntity.ok(users);
  }

  /**
   * Get designated employee for customer to message Returns the first available employee (support
   * representative)
   */
  @GetMapping("/designated-employee")
  public ResponseEntity<UserSearchDTO> getDesignatedEmployee(Authentication authentication) {
    log.info("Getting designated employee for customer");

    UserSearchDTO employee = messageService.getDesignatedEmployee();

    return ResponseEntity.ok(employee);
  }

  /**
   * Get all messages for current customer This endpoint is specifically for customers to load their
   * complete chat history including broadcasts they sent and all employee replies
   */
  @GetMapping("/customer/all")
  public ResponseEntity<List<MessageDTO>> getAllCustomerMessages(Authentication authentication) {
    log.info("Getting all customer messages");

    Long customerId = authUtil.getUserIdFromAuth(authentication);
    List<MessageDTO> messages = messageService.getAllCustomerMessages(customerId);

    return ResponseEntity.ok(messages);
  }

  /**
   * Get all messages for a specific customer (employee view) Allows any authenticated employee to
   * view the complete conversation thread for a customer, including messages from other employees.
   */
  @GetMapping("/employee/customer/{customerId}/all")
  public ResponseEntity<List<MessageDTO>> getAllMessagesForCustomerAsEmployee(
      @PathVariable Long customerId, Authentication authentication) {
    log.info("Employee requesting all messages for customer {}", customerId);

    // Optional: ensure the requester exists; role checks can be added if needed
    authUtil.getUserFromAuth(authentication);

    List<MessageDTO> messages = messageService.getAllCustomerMessages(customerId);
    return ResponseEntity.ok(messages);
  }
}
