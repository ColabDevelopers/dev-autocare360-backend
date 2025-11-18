package com.autocare360.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.autocare360.dto.ConversationDTO;
import com.autocare360.dto.MessageDTO;
import com.autocare360.dto.UserSearchDTO;
import com.autocare360.entity.Message;
import com.autocare360.entity.Role;
import com.autocare360.entity.User;
import com.autocare360.repo.UserRepository;
import com.autocare360.repository.MessageRepository;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageService Tests")
class MessageServiceTest {

  @Mock private MessageRepository messageRepository;

  @Mock private UserRepository userRepository;

  @Mock private SimpMessagingTemplate messagingTemplate;

  @InjectMocks private MessageService messageService;

  private User customerUser;
  private User employeeUser;
  private Message testMessage;
  private Role customerRole;
  private Role employeeRole;

  @BeforeEach
  void setUp() {
    customerRole = Role.builder().name("CUSTOMER").build();
    employeeRole = Role.builder().name("EMPLOYEE").build();

    customerUser = new User();
    customerUser.setId(1L);
    customerUser.setName("Customer User");
    customerUser.setEmail("customer@example.com");
    customerUser.setRoles(new HashSet<>(Arrays.asList(customerRole)));

    employeeUser = new User();
    employeeUser.setId(2L);
    employeeUser.setName("Employee User");
    employeeUser.setEmail("employee@example.com");
    employeeUser.setRoles(new HashSet<>(Arrays.asList(employeeRole)));

    testMessage = new Message();
    testMessage.setId(1L);
    testMessage.setSenderId(1L);
    testMessage.setReceiverId(2L);
    testMessage.setMessage("Test message");
    testMessage.setIsRead(false);
    testMessage.setCreatedAt(LocalDateTime.now());
  }

  @Test
  @DisplayName("Should send message from customer to employees")
  void testSendMessage_CustomerToEmployees() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(customerUser));
    when(messageRepository.save(any(Message.class))).thenReturn(testMessage);
    doNothing().when(messagingTemplate).convertAndSend(anyString(), any(MessageDTO.class));

    MessageDTO result = messageService.sendMessage(1L, null, "Test message");

    assertNotNull(result);
    assertEquals("Test message", result.getMessage());
    verify(userRepository, times(1)).findById(1L);
    verify(messageRepository, times(1)).save(any(Message.class));
    verify(messagingTemplate, times(1))
        .convertAndSend(eq("/topic/employee-messages"), any(MessageDTO.class));
  }

  @Test
  @DisplayName("Should send message from employee to customer")
  void testSendMessage_EmployeeToCustomer() {
    testMessage.setSenderId(2L);
    testMessage.setReceiverId(1L);

    when(userRepository.findById(2L)).thenReturn(Optional.of(employeeUser));
    when(userRepository.findById(1L)).thenReturn(Optional.of(customerUser));
    when(messageRepository.save(any(Message.class))).thenReturn(testMessage);
    doNothing()
        .when(messagingTemplate)
        .convertAndSendToUser(anyString(), anyString(), any(MessageDTO.class));
    doNothing().when(messagingTemplate).convertAndSend(anyString(), any(MessageDTO.class));

    MessageDTO result = messageService.sendMessage(2L, 1L, "Reply message");

    assertNotNull(result);
    verify(userRepository, times(1)).findById(2L);
    verify(userRepository, times(1)).findById(1L);
    verify(messageRepository, times(1)).save(any(Message.class));
    verify(messagingTemplate, times(1))
        .convertAndSendToUser(
            eq("customer@example.com"), eq("/queue/messages"), any(MessageDTO.class));
    verify(messagingTemplate, times(1))
        .convertAndSend(eq("/topic/employee-messages"), any(MessageDTO.class));
  }

  @Test
  @DisplayName("Should throw exception when sender not found")
  void testSendMessage_SenderNotFound() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    RuntimeException exception =
        assertThrows(
            RuntimeException.class, () -> messageService.sendMessage(1L, 2L, "Test message"));

    assertEquals("Sender not found", exception.getMessage());
    verify(userRepository, times(1)).findById(1L);
    verify(messageRepository, never()).save(any(Message.class));
  }

  @Test
  @DisplayName("Should get conversation between two users")
  void testGetConversation_Success() {
    Message message2 = new Message();
    message2.setId(2L);
    message2.setSenderId(2L);
    message2.setReceiverId(1L);
    message2.setMessage("Reply");
    message2.setCreatedAt(LocalDateTime.now());

    List<Message> messages = Arrays.asList(testMessage, message2);

    when(messageRepository.findConversation(1L, 2L)).thenReturn(messages);
    when(userRepository.findById(1L)).thenReturn(Optional.of(customerUser));
    when(userRepository.findById(2L)).thenReturn(Optional.of(employeeUser));

    List<MessageDTO> result = messageService.getConversation(1L, 2L);

    assertNotNull(result);
    assertEquals(2, result.size());
    verify(messageRepository, times(1)).findConversation(1L, 2L);
  }

  @Test
  @DisplayName("Should filter out messages with null sender")
  void testGetConversation_FilterNullSender() {
    when(messageRepository.findConversation(1L, 2L)).thenReturn(Arrays.asList(testMessage));
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    List<MessageDTO> result = messageService.getConversation(1L, 2L);

    assertNotNull(result);
    assertEquals(0, result.size());
    verify(messageRepository, times(1)).findConversation(1L, 2L);
  }

  @Test
  @DisplayName("Should get conversations for employee (shared inbox)")
  void testGetConversations_Employee() {
    when(userRepository.findById(2L)).thenReturn(Optional.of(employeeUser));
    when(messageRepository.findAllCustomersWithMessages()).thenReturn(Arrays.asList(1L));
    when(userRepository.findById(1L)).thenReturn(Optional.of(customerUser));
    when(messageRepository.findAllCustomerMessages(1L)).thenReturn(Arrays.asList(testMessage));
    when(messageRepository.countUnreadMessagesFrom(2L, 1L)).thenReturn(2L);

    List<ConversationDTO> result = messageService.getConversations(2L);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("Customer User", result.get(0).getName());
    assertEquals(2L, result.get(0).getUnreadCount());
    verify(userRepository, times(1)).findById(2L);
    verify(messageRepository, times(1)).findAllCustomersWithMessages();
  }

  @Test
  @DisplayName("Should get conversations for customer")
  void testGetConversations_Customer() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(customerUser));
    when(messageRepository.findConversationPartners(1L)).thenReturn(Arrays.asList(2L));
    when(userRepository.findById(2L)).thenReturn(Optional.of(employeeUser));
    when(messageRepository.findLastMessage(1L, 2L)).thenReturn(testMessage);
    when(messageRepository.countUnreadMessagesFrom(1L, 2L)).thenReturn(1L);

    List<ConversationDTO> result = messageService.getConversations(1L);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("Employee User", result.get(0).getName());
    verify(userRepository, times(1)).findById(1L);
    verify(messageRepository, times(1)).findConversationPartners(1L);
  }

  @Test
  @DisplayName("Should mark messages as read")
  void testMarkMessagesAsRead() {
    doNothing().when(messageRepository).markMessagesAsRead(1L, 2L);

    messageService.markMessagesAsRead(1L, 2L);

    verify(messageRepository, times(1)).markMessagesAsRead(1L, 2L);
  }

  @Test
  @DisplayName("Should get unread count")
  void testGetUnreadCount() {
    when(messageRepository.countUnreadMessages(1L)).thenReturn(5L);

    Long count = messageService.getUnreadCount(1L);

    assertEquals(5L, count);
    verify(messageRepository, times(1)).countUnreadMessages(1L);
  }

  @Test
  @DisplayName("Should search users by role")
  void testSearchUsersByRole_Success() {
    when(userRepository.findAll()).thenReturn(Arrays.asList(customerUser, employeeUser));

    List<UserSearchDTO> result = messageService.searchUsersByRole("EMPLOYEE", "emp");

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("Employee User", result.get(0).getName());
    verify(userRepository, times(1)).findAll();
  }

  @Test
  @DisplayName("Should search users with empty query")
  void testSearchUsersByRole_EmptyQuery() {
    when(userRepository.findAll()).thenReturn(Arrays.asList(customerUser, employeeUser));

    List<UserSearchDTO> result = messageService.searchUsersByRole("CUSTOMER", null);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("Customer User", result.get(0).getName());
  }

  @Test
  @DisplayName("Should get designated employee")
  void testGetDesignatedEmployee_Success() {
    when(userRepository.findAll()).thenReturn(Arrays.asList(employeeUser));

    UserSearchDTO result = messageService.getDesignatedEmployee();

    assertNotNull(result);
    assertEquals("Employee User", result.getName());
    assertEquals("employee@example.com", result.getEmail());
    verify(userRepository, times(1)).findAll();
  }

  @Test
  @DisplayName("Should throw exception when no employee available")
  void testGetDesignatedEmployee_NoEmployees() {
    when(userRepository.findAll()).thenReturn(Arrays.asList(customerUser));

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> messageService.getDesignatedEmployee());

    assertEquals("No employee available", exception.getMessage());
    verify(userRepository, times(1)).findAll();
  }

  @Test
  @DisplayName("Should get all customer messages")
  void testGetAllCustomerMessages_Success() {
    Message message2 = new Message();
    message2.setId(2L);
    message2.setSenderId(2L);
    message2.setReceiverId(1L);
    message2.setMessage("Reply");

    List<Message> messages = Arrays.asList(testMessage, message2);
    when(messageRepository.findAllCustomerMessages(1L)).thenReturn(messages);
    when(userRepository.findById(1L)).thenReturn(Optional.of(customerUser));
    when(userRepository.findById(2L)).thenReturn(Optional.of(employeeUser));

    List<MessageDTO> result = messageService.getAllCustomerMessages(1L);

    assertNotNull(result);
    assertEquals(2, result.size());
    verify(messageRepository, times(1)).findAllCustomerMessages(1L);
  }

  @Test
  @DisplayName("Should format time correctly for just now")
  void testGetConversations_FormatTimeJustNow() {
    testMessage.setCreatedAt(LocalDateTime.now());
    when(userRepository.findById(1L)).thenReturn(Optional.of(customerUser));
    when(messageRepository.findConversationPartners(1L)).thenReturn(Arrays.asList(2L));
    when(userRepository.findById(2L)).thenReturn(Optional.of(employeeUser));
    when(messageRepository.findLastMessage(1L, 2L)).thenReturn(testMessage);
    when(messageRepository.countUnreadMessagesFrom(1L, 2L)).thenReturn(0L);

    List<ConversationDTO> result = messageService.getConversations(1L);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertTrue(result.get(0).getTime().contains("now") || result.get(0).getTime().contains("min"));
  }

  @Test
  @DisplayName("Should skip null conversation partners")
  void testGetConversations_SkipNullPartners() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(customerUser));
    when(messageRepository.findConversationPartners(1L)).thenReturn(Arrays.asList(null, 2L));
    when(userRepository.findById(2L)).thenReturn(Optional.of(employeeUser));
    when(messageRepository.findLastMessage(1L, 2L)).thenReturn(testMessage);
    when(messageRepository.countUnreadMessagesFrom(1L, 2L)).thenReturn(0L);

    List<ConversationDTO> result = messageService.getConversations(1L);

    assertNotNull(result);
    assertEquals(1, result.size());
  }

  @Test
  @DisplayName("Should broadcast to all employees when customer sends message")
  void testSendMessage_CustomerBroadcast() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(customerUser));
    when(messageRepository.save(any(Message.class))).thenReturn(testMessage);
    doNothing().when(messagingTemplate).convertAndSend(anyString(), any(MessageDTO.class));

    messageService.sendMessage(1L, null, "Customer broadcast");

    verify(messagingTemplate, times(1))
        .convertAndSend(eq("/topic/employee-messages"), any(MessageDTO.class));
    verify(messagingTemplate, never())
        .convertAndSendToUser(anyString(), anyString(), any(MessageDTO.class));
  }
}

