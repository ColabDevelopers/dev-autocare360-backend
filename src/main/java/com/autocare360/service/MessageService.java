package com.autocare360.service;

import com.autocare360.dto.ConversationDTO;
import com.autocare360.dto.MessageDTO;
import com.autocare360.dto.UserSearchDTO;
import com.autocare360.entity.Message;
import com.autocare360.entity.User;
import com.autocare360.repo.UserRepository;
import com.autocare360.repository.MessageRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final SimpMessagingTemplate messagingTemplate;

  /**
   * Send a message and notify receiver via WebSocket For customers: receiverId can be NULL
   * (broadcast to all employees) For employees: receiverId is the specific customer ID
   */
  @Transactional
  public MessageDTO sendMessage(Long senderId, Long receiverId, String messageText) {
    log.info("Sending message from user {} to user {}", senderId, receiverId);

    User sender =
        userRepository
            .findById(senderId)
            .orElseThrow(() -> new RuntimeException("Sender not found"));

    // Check if sender is customer - if so, broadcast to all employees
    boolean isCustomer =
        sender.getRoles().stream().anyMatch(role -> role.getName().equalsIgnoreCase("CUSTOMER"));

    // Create message entity
    Message message = new Message();
    message.setSenderId(senderId);
    message.setReceiverId(receiverId); // Can be NULL for customer messages
    message.setMessage(messageText);
    message.setIsRead(false);

    // Save to database
    Message savedMessage = messageRepository.save(message);
    log.info("Message saved with ID: {}", savedMessage.getId());

    // Convert to DTO
    MessageDTO messageDTO = convertToDTO(savedMessage, sender);

    if (isCustomer) {
      // Customer message: Notify ALL employees via broadcast topic
      log.info("Broadcasting customer message to all employees");
      messagingTemplate.convertAndSend("/topic/employee-messages", messageDTO);
    } else {
      // Employee message: Send to specific customer and also broadcast to the shared employee topic
      if (receiverId != null) {
        // Get the customer's email to send WebSocket message
        User receiver = userRepository.findById(receiverId).orElse(null);

        if (receiver != null) {
          // Use customer's email (username) for WebSocket routing
          messagingTemplate.convertAndSendToUser(
              receiver.getEmail(), "/queue/messages", messageDTO);
          log.info(
              "WebSocket notification sent to customer {} ({})", receiverId, receiver.getEmail());
        } else {
          log.warn("Receiver with ID {} not found, cannot send WebSocket notification", receiverId);
        }
      }

      // Always broadcast employee replies to all employees so everyone sees the same customer
      // thread
      log.info("Broadcasting employee reply to all employees for customer {}", receiverId);
      messagingTemplate.convertAndSend("/topic/employee-messages", messageDTO);
    }

    return messageDTO;
  }

  /** Get conversation between two users */
  @Transactional(readOnly = true)
  public List<MessageDTO> getConversation(Long userId1, Long userId2) {
    log.info("Getting conversation between user {} and user {}", userId1, userId2);

    List<Message> messages = messageRepository.findConversation(userId1, userId2);
    List<MessageDTO> messageDTOs = new ArrayList<>();

    for (Message message : messages) {
      User sender = userRepository.findById(message.getSenderId()).orElse(null);
      if (sender != null) {
        messageDTOs.add(convertToDTO(message, sender));
      }
    }

    log.info("Found {} messages in conversation", messageDTOs.size());
    return messageDTOs;
  }

  /**
   * Get all conversations for a user For employees: Shows ALL customer conversations (shared inbox)
   * For customers: Shows only their own conversation
   */
  @Transactional(readOnly = true)
  public List<ConversationDTO> getConversations(Long userId) {
    log.info("Getting all conversations for user {}", userId);

    User currentUser =
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

    boolean isEmployee =
        currentUser.getRoles().stream()
            .anyMatch(role -> role.getName().equalsIgnoreCase("EMPLOYEE"));

    List<ConversationDTO> conversations = new ArrayList<>();

    if (isEmployee) {
      // Employee: Get ALL customer conversations (shared inbox)
      List<Long> customerIds = messageRepository.findAllCustomersWithMessages();

      for (Long customerId : customerIds) {
        User customer = userRepository.findById(customerId).orElse(null);
        if (customer == null) continue;

        // Latest message for this customer across ALL employees
        List<Message> allForCustomer = messageRepository.findAllCustomerMessages(customerId);
        if (allForCustomer == null || allForCustomer.isEmpty()) continue;
        Message lastMessage = allForCustomer.get(allForCustomer.size() - 1);

        Long unreadCount = messageRepository.countUnreadMessagesFrom(userId, customerId);

        ConversationDTO conversation = new ConversationDTO();
        conversation.setUserId(customerId);
        conversation.setName(customer.getName());
        conversation.setRole(
            customer.getRoles().isEmpty()
                ? "CUSTOMER"
                : customer.getRoles().iterator().next().getName());
        conversation.setLastMessage(lastMessage.getMessage());
        // Pre-format relative time; UI will display directly
        conversation.setTime(formatTimeAgo(lastMessage.getCreatedAt()));
        conversation.setUnreadCount(unreadCount);
        conversation.setAvatar("/placeholder.svg");

        conversations.add(conversation);
      }
    } else {
      // Customer: Only their own conversation with employees
      List<Long> partnerIds = messageRepository.findConversationPartners(userId);

      for (Long partnerId : partnerIds) {
        if (partnerId == null) continue; // Skip NULL receiverIds

        User partner = userRepository.findById(partnerId).orElse(null);
        if (partner == null) continue;

        Message lastMessage = messageRepository.findLastMessage(userId, partnerId);
        if (lastMessage == null) continue;

        Long unreadCount = messageRepository.countUnreadMessagesFrom(userId, partnerId);

        ConversationDTO conversation = new ConversationDTO();
        conversation.setUserId(partnerId);
        conversation.setName(partner.getName());
        conversation.setRole(
            partner.getRoles().isEmpty()
                ? "EMPLOYEE"
                : partner.getRoles().iterator().next().getName());
        conversation.setLastMessage(lastMessage.getMessage());
        conversation.setTime(formatTimeAgo(lastMessage.getCreatedAt()));
        conversation.setUnreadCount(unreadCount);
        conversation.setAvatar("/placeholder.svg");

        conversations.add(conversation);
      }
    }

    log.info("Found {} conversations for user {}", conversations.size(), userId);
    return conversations;
  }

  /** Mark all messages from a sender as read */
  @Transactional
  public void markMessagesAsRead(Long receiverId, Long senderId) {
    log.info("Marking messages as read for receiver {} from sender {}", receiverId, senderId);
    messageRepository.markMessagesAsRead(receiverId, senderId);
  }

  /** Get unread message count */
  @Transactional(readOnly = true)
  public Long getUnreadCount(Long userId) {
    return messageRepository.countUnreadMessages(userId);
  }

  /** Convert Message entity to MessageDTO */
  private MessageDTO convertToDTO(Message message, User sender) {
    MessageDTO dto = new MessageDTO();
    dto.setId(message.getId());
    dto.setSenderId(message.getSenderId());
    dto.setReceiverId(message.getReceiverId());
    dto.setSenderName(sender.getName());
    dto.setSenderRole(
        sender.getRoles().isEmpty() ? "USER" : sender.getRoles().iterator().next().getName());
    dto.setMessage(message.getMessage());
    dto.setCreatedAt(message.getCreatedAt());
    dto.setIsRead(message.getIsRead());
    return dto;
  }

  /** Format timestamp as "X time ago" */
  private String formatTimeAgo(LocalDateTime timestamp) {
    Duration duration = Duration.between(timestamp, LocalDateTime.now());

    long minutes = duration.toMinutes();
    if (minutes < 1) return "Just now";
    if (minutes < 60) return minutes + " min ago";

    long hours = duration.toHours();
    if (hours < 24) return hours + " hour" + (hours > 1 ? "s" : "") + " ago";

    long days = duration.toDays();
    if (days < 7) return days + " day" + (days > 1 ? "s" : "") + " ago";

    long weeks = days / 7;
    return weeks + " week" + (weeks > 1 ? "s" : "") + " ago";
  }

  /** Search users by role for starting new conversations */
  @Transactional(readOnly = true)
  public List<UserSearchDTO> searchUsersByRole(String roleName, String query) {
    log.info("Searching users with role: {} and query: {}", roleName, query);

    List<User> users = userRepository.findAll();

    return users.stream()
        .filter(
            user ->
                user.getRoles().stream()
                    .anyMatch(role -> role.getName().equalsIgnoreCase(roleName)))
        .filter(
            user ->
                query == null
                    || query.isEmpty()
                    || user.getName().toLowerCase().contains(query.toLowerCase())
                    || user.getEmail().toLowerCase().contains(query.toLowerCase()))
        .map(
            user ->
                new UserSearchDTO(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getRoles().isEmpty()
                        ? "USER"
                        : user.getRoles().iterator().next().getName()))
        .collect(java.util.stream.Collectors.toList());
  }

  /** Get designated employee for customer messaging Returns the first available employee */
  @Transactional(readOnly = true)
  public UserSearchDTO getDesignatedEmployee() {
    log.info("Getting designated employee");

    List<User> employees =
        userRepository.findAll().stream()
            .filter(
                user ->
                    user.getRoles().stream()
                        .anyMatch(role -> role.getName().equalsIgnoreCase("EMPLOYEE")))
            .limit(1)
            .toList();

    if (employees.isEmpty()) {
      throw new RuntimeException("No employee available");
    }

    User employee = employees.get(0);
    return new UserSearchDTO(
        employee.getId(),
        employee.getName(),
        employee.getEmail(),
        employee.getRoles().isEmpty()
            ? "EMPLOYEE"
            : employee.getRoles().iterator().next().getName());
  }

  /**
   * Get all messages for a customer (including broadcasts they sent and employee replies) This is
   * simpler than using conversations endpoint for customers
   */
  @Transactional(readOnly = true)
  public List<MessageDTO> getAllCustomerMessages(Long customerId) {
    log.info("Getting all messages for customer {}", customerId);

    // Get all messages where:
    // 1. Customer sent (as broadcast with receiverId=NULL or to specific employee)
    // 2. Employee sent to this customer (receiverId=customerId)
    List<Message> messages = messageRepository.findAllCustomerMessages(customerId);

    List<MessageDTO> messageDTOs = new ArrayList<>();
    for (Message message : messages) {
      User sender = userRepository.findById(message.getSenderId()).orElse(null);
      if (sender != null) {
        messageDTOs.add(convertToDTO(message, sender));
      }
    }

    log.info("Found {} total messages for customer {}", messageDTOs.size(), customerId);
    return messageDTOs;
  }
}
