package com.autocare360.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.autocare360.dto.UserResponse;
import com.autocare360.entity.Role;
import com.autocare360.entity.User;
import com.autocare360.repo.UserRepository;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private UserService userService;

  private User testUser;
  private Role customerRole;
  private Role employeeRole;

  @BeforeEach
  void setUp() {
    customerRole = Role.builder().name("CUSTOMER").build();

    employeeRole = Role.builder().name("EMPLOYEE").build();

    testUser =
        User.builder()
            .id(1L)
            .email("test@example.com")
            .name("Test User")
            .passwordHash("hashedPassword")
            .phone("+1-555-0100")
            .status("ACTIVE")
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .roles(new HashSet<>(Arrays.asList(customerRole)))
            .build();
  }

  @Test
  @DisplayName("Should get current user successfully")
  void testGetCurrent_Success() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    UserResponse response = userService.getCurrent(1L);

    assertNotNull(response);
    assertEquals(1L, response.getId());
    assertEquals("test@example.com", response.getEmail());
    assertEquals("Test User", response.getName());
    assertEquals("Active", response.getStatus());
    assertEquals("+1-555-0100", response.getPhone());
    assertEquals(1, response.getRoles().size());
    assertTrue(response.getRoles().contains("customer"));
    assertNull(response.getEmployeeNo());
    assertNull(response.getDepartment());

    verify(userRepository, times(1)).findById(1L);
  }

  @Test
  @DisplayName("Should get current employee user with employee details")
  void testGetCurrent_EmployeeUser() {
    testUser.getRoles().add(employeeRole);
    testUser.setEmployeeNo("EMP-001");
    testUser.setDepartment("Mechanical");

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    UserResponse response = userService.getCurrent(1L);

    assertNotNull(response);
    assertEquals("EMP-001", response.getEmployeeNo());
    assertEquals("Mechanical", response.getDepartment());
    verify(userRepository, times(1)).findById(1L);
  }

  @Test
  @DisplayName("Should throw exception when user not found")
  void testGetCurrent_UserNotFound() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class, () -> userService.getCurrent(1L));

    verify(userRepository, times(1)).findById(1L);
  }

  @Test
  @DisplayName("Should change password successfully")
  void testChangePassword_Success() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches("oldPassword", "hashedPassword")).thenReturn(true);
    when(passwordEncoder.encode("newPassword")).thenReturn("newHashedPassword");
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    assertDoesNotThrow(() -> userService.changePassword(1L, "oldPassword", "newPassword"));

    verify(userRepository, times(1)).findById(1L);
    verify(passwordEncoder, times(1)).matches("oldPassword", "hashedPassword");
    verify(passwordEncoder, times(1)).encode("newPassword");
    verify(userRepository, times(1)).save(testUser);
  }

  @Test
  @DisplayName("Should throw exception when current password is invalid")
  void testChangePassword_InvalidCurrentPassword() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> userService.changePassword(1L, "wrongPassword", "newPassword"));

    assertEquals("Invalid current password", exception.getMessage());
    verify(userRepository, times(1)).findById(1L);
    verify(passwordEncoder, times(1)).matches("wrongPassword", "hashedPassword");
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("Should update phone successfully")
  void testUpdatePhone_Success() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    userService.updatePhone(1L, "+1-555-9999");

    assertEquals("+1-555-9999", testUser.getPhone());
    verify(userRepository, times(1)).findById(1L);
    verify(userRepository, times(1)).save(testUser);
  }

  @Test
  @DisplayName("Should get all customers successfully")
  void testGetAllCustomers_Success() {
    User customer1 =
        User.builder()
            .id(1L)
            .email("customer1@example.com")
            .name("Customer One")
            .phone("+1-555-0001")
            .status("ACTIVE")
            .roles(new HashSet<>(Arrays.asList(customerRole)))
            .build();

    User customer2 =
        User.builder()
            .id(2L)
            .email("customer2@example.com")
            .name("Customer Two")
            .phone("+1-555-0002")
            .status("INACTIVE")
            .roles(new HashSet<>(Arrays.asList(customerRole)))
            .build();

    User employee =
        User.builder()
            .id(3L)
            .email("employee@example.com")
            .name("Employee")
            .phone("+1-555-0003")
            .status("ACTIVE")
            .roles(new HashSet<>(Arrays.asList(employeeRole)))
            .build();

    when(userRepository.findAll()).thenReturn(Arrays.asList(customer1, customer2, employee));

    List<UserResponse> customers = userService.getAllCustomers();

    assertNotNull(customers);
    assertEquals(2, customers.size());
    assertEquals("customer1@example.com", customers.get(0).getEmail());
    assertEquals("customer2@example.com", customers.get(1).getEmail());
    verify(userRepository, times(1)).findAll();
  }

  @Test
  @DisplayName("Should return empty list when no customers exist")
  void testGetAllCustomers_EmptyList() {
    User employee =
        User.builder()
            .id(3L)
            .email("employee@example.com")
            .name("Employee")
            .phone("+1-555-0003")
            .status("ACTIVE")
            .roles(new HashSet<>(Arrays.asList(employeeRole)))
            .build();

    when(userRepository.findAll()).thenReturn(Arrays.asList(employee));

    List<UserResponse> customers = userService.getAllCustomers();

    assertNotNull(customers);
    assertEquals(0, customers.size());
    verify(userRepository, times(1)).findAll();
  }

  @Test
  @DisplayName("Should handle user with null status")
  void testGetCurrent_NullStatus() {
    testUser.setStatus(null);
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    UserResponse response = userService.getCurrent(1L);

    assertNotNull(response);
    assertEquals("Active", response.getStatus());
    verify(userRepository, times(1)).findById(1L);
  }
}

