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

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService Tests")
class CustomerServiceTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private CustomerService customerService;

  private User testCustomer;
  private Role customerRole;

  @BeforeEach
  void setUp() {
    customerRole = Role.builder().name("CUSTOMER").build();

    testCustomer =
        User.builder()
            .id(1L)
            .email("customer@example.com")
            .name("Test Customer")
            .passwordHash("hashedPassword")
            .phone("+1-555-0100")
            .status("ACTIVE")
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .roles(new HashSet<>(Arrays.asList(customerRole)))
            .build();
  }

  @Test
  @DisplayName("Should list all customers")
  void testListCustomers_Success() {
    User customer2 =
        User.builder()
            .id(2L)
            .email("customer2@example.com")
            .name("Second Customer")
            .phone("+1-555-0200")
            .status("ACTIVE")
            .roles(new HashSet<>(Arrays.asList(customerRole)))
            .build();

    List<User> customers = Arrays.asList(testCustomer, customer2);
    when(userRepository.findDistinctByRoles_Name("CUSTOMER")).thenReturn(customers);

    List<UserResponse> result = customerService.listCustomers();

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("Test Customer", result.get(0).getName());
    assertEquals("Second Customer", result.get(1).getName());
    verify(userRepository, times(1)).findDistinctByRoles_Name("CUSTOMER");
  }

  @Test
  @DisplayName("Should return empty list when no customers")
  void testListCustomers_EmptyList() {
    when(userRepository.findDistinctByRoles_Name("CUSTOMER")).thenReturn(Arrays.asList());

    List<UserResponse> result = customerService.listCustomers();

    assertNotNull(result);
    assertEquals(0, result.size());
    verify(userRepository, times(1)).findDistinctByRoles_Name("CUSTOMER");
  }

  @Test
  @DisplayName("Should get customer by id")
  void testGetCustomer_Success() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

    UserResponse result = customerService.getCustomer(1L);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("customer@example.com", result.getEmail());
    assertEquals("Test Customer", result.getName());
    assertEquals("ACTIVE", result.getStatus());
    assertEquals("+1-555-0100", result.getPhone());
    verify(userRepository, times(1)).findById(1L);
  }

  @Test
  @DisplayName("Should throw exception when customer not found")
  void testGetCustomer_NotFound() {
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class, () -> customerService.getCustomer(999L));

    verify(userRepository, times(1)).findById(999L);
  }

  @Test
  @DisplayName("Should update customer successfully")
  void testUpdateCustomer_Success() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
    when(userRepository.save(any(User.class))).thenReturn(testCustomer);

    UserResponse result =
        customerService.updateCustomer(1L, "Updated Name", "+1-555-9999", "INACTIVE");

    assertNotNull(result);
    assertEquals("Updated Name", testCustomer.getName());
    assertEquals("+1-555-9999", testCustomer.getPhone());
    assertEquals("INACTIVE", testCustomer.getStatus());
    verify(userRepository, times(1)).findById(1L);
    verify(userRepository, times(1)).save(testCustomer);
  }

  @Test
  @DisplayName("Should update only provided fields")
  void testUpdateCustomer_PartialUpdate() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
    when(userRepository.save(any(User.class))).thenReturn(testCustomer);

    UserResponse result = customerService.updateCustomer(1L, "New Name", null, null);

    assertNotNull(result);
    assertEquals("New Name", testCustomer.getName());
    assertEquals("+1-555-0100", testCustomer.getPhone()); // Should remain unchanged
    assertEquals("ACTIVE", testCustomer.getStatus()); // Should remain unchanged
    verify(userRepository, times(1)).save(testCustomer);
  }

  @Test
  @DisplayName("Should throw exception when updating non-existent customer")
  void testUpdateCustomer_NotFound() {
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(
        NoSuchElementException.class,
        () -> customerService.updateCustomer(999L, "Name", "Phone", "Status"));

    verify(userRepository, times(1)).findById(999L);
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("Should delete customer successfully")
  void testDeleteCustomer_Success() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
    doNothing().when(userRepository).delete(testCustomer);

    customerService.deleteCustomer(1L);

    verify(userRepository, times(1)).findById(1L);
    verify(userRepository, times(1)).delete(testCustomer);
  }

  @Test
  @DisplayName("Should throw exception when deleting non-existent customer")
  void testDeleteCustomer_NotFound() {
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class, () -> customerService.deleteCustomer(999L));

    verify(userRepository, times(1)).findById(999L);
    verify(userRepository, never()).delete(any(User.class));
  }

  @Test
  @DisplayName("Should include roles in customer response")
  void testListCustomers_IncludeRoles() {
    when(userRepository.findDistinctByRoles_Name("CUSTOMER"))
        .thenReturn(Arrays.asList(testCustomer));

    List<UserResponse> result = customerService.listCustomers();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertNotNull(result.get(0).getRoles());
    assertEquals(1, result.get(0).getRoles().size());
    assertTrue(result.get(0).getRoles().contains("customer"));
  }

  @Test
  @DisplayName("Should handle customer with multiple roles")
  void testGetCustomer_MultipleRoles() {
    Role adminRole = Role.builder().name("ADMIN").build();
    testCustomer.getRoles().add(adminRole);

    when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

    UserResponse result = customerService.getCustomer(1L);

    assertNotNull(result);
    assertEquals(2, result.getRoles().size());
    assertTrue(result.getRoles().contains("customer"));
    assertTrue(result.getRoles().contains("admin"));
  }

  @Test
  @DisplayName("Should update phone number format")
  void testUpdateCustomer_PhoneFormat() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
    when(userRepository.save(any(User.class))).thenReturn(testCustomer);

    customerService.updateCustomer(1L, null, "555-1234", null);

    assertEquals("555-1234", testCustomer.getPhone());
    verify(userRepository, times(1)).save(testCustomer);
  }
}

