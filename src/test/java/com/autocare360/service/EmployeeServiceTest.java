package com.autocare360.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.autocare360.dto.CreateEmployeeRequest;
import com.autocare360.dto.EmployeeResponse;
import com.autocare360.dto.UpdateEmployeeRequest;
import com.autocare360.entity.Role;
import com.autocare360.entity.User;
import com.autocare360.exception.ConflictException;
import com.autocare360.repo.RoleRepository;
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
@DisplayName("EmployeeService Tests")
class EmployeeServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private RoleRepository roleRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private EmployeeService employeeService;

  private Role employeeRole;
  private User testEmployee;
  private CreateEmployeeRequest createRequest;
  private UpdateEmployeeRequest updateRequest;

  @BeforeEach
  void setUp() {
    employeeRole = Role.builder().name("EMPLOYEE").build();

    testEmployee =
        User.builder()
            .id(1L)
            .email("employee@example.com")
            .name("John Employee")
            .passwordHash("hashedPassword")
            .employeeNo("EMP-0001")
            .department("Mechanical")
            .status("ACTIVE")
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .roles(new HashSet<>(Arrays.asList(employeeRole)))
            .build();

    createRequest =
        CreateEmployeeRequest.builder()
            .email("newemployee@example.com")
            .name("New Employee")
            .department("Electrical")
            .build();

    updateRequest =
        UpdateEmployeeRequest.builder()
            .name("Updated Employee")
            .department("Mechanical")
            .status("INACTIVE")
            .build();
  }

  @Test
  @DisplayName("Should create employee successfully")
  void testCreate_Success() {
    when(userRepository.findByEmail(createRequest.getEmail())).thenReturn(Optional.empty());
    when(roleRepository.findByName("EMPLOYEE")).thenReturn(Optional.of(employeeRole));
    when(passwordEncoder.encode("password")).thenReturn("hashedPassword");
    when(userRepository.count()).thenReturn(5L);
    when(userRepository.save(any(User.class))).thenReturn(testEmployee);

    EmployeeResponse response = employeeService.create(createRequest);

    assertNotNull(response);
    assertEquals(1L, response.getId());
    assertEquals("employee@example.com", response.getEmail());
    assertEquals("John Employee", response.getName());
    assertEquals("EMP-0001", response.getEmployeeNo());
    assertEquals("Mechanical", response.getDepartment());
    verify(userRepository, times(1)).findByEmail(createRequest.getEmail());
    verify(roleRepository, times(1)).findByName("EMPLOYEE");
    verify(passwordEncoder, times(1)).encode("password");
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  @DisplayName("Should throw ConflictException when email already exists")
  void testCreate_EmailAlreadyExists() {
    when(userRepository.findByEmail(createRequest.getEmail()))
        .thenReturn(Optional.of(testEmployee));

    ConflictException exception =
        assertThrows(ConflictException.class, () -> employeeService.create(createRequest));

    assertEquals("Email already in use", exception.getMessage());
    verify(userRepository, times(1)).findByEmail(createRequest.getEmail());
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("Should create EMPLOYEE role if not exists")
  void testCreate_CreateRoleIfNotExists() {
    when(userRepository.findByEmail(createRequest.getEmail())).thenReturn(Optional.empty());
    when(roleRepository.findByName("EMPLOYEE")).thenReturn(Optional.empty());
    when(roleRepository.save(any(Role.class))).thenReturn(employeeRole);
    when(passwordEncoder.encode("password")).thenReturn("hashedPassword");
    when(userRepository.count()).thenReturn(0L);
    when(userRepository.save(any(User.class))).thenReturn(testEmployee);

    EmployeeResponse response = employeeService.create(createRequest);

    assertNotNull(response);
    verify(roleRepository, times(1)).findByName("EMPLOYEE");
    verify(roleRepository, times(1)).save(any(Role.class));
  }

  @Test
  @DisplayName("Should generate unique employee number")
  void testCreate_GenerateEmployeeNumber() {
    when(userRepository.findByEmail(createRequest.getEmail())).thenReturn(Optional.empty());
    when(roleRepository.findByName("EMPLOYEE")).thenReturn(Optional.of(employeeRole));
    when(passwordEncoder.encode("password")).thenReturn("hashedPassword");
    when(userRepository.count()).thenReturn(42L);
    when(userRepository.save(any(User.class)))
        .thenAnswer(
            invocation -> {
              User user = invocation.getArgument(0);
              user.setId(1L);
              return user;
            });

    employeeService.create(createRequest);

    verify(userRepository, times(1)).count();
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  @DisplayName("Should update employee successfully")
  void testUpdate_Success() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
    when(userRepository.save(any(User.class))).thenReturn(testEmployee);

    EmployeeResponse response = employeeService.update(1L, updateRequest);

    assertNotNull(response);
    assertEquals("Updated Employee", testEmployee.getName());
    assertEquals("Mechanical", testEmployee.getDepartment());
    assertEquals("INACTIVE", testEmployee.getStatus());
    verify(userRepository, times(1)).findById(1L);
    verify(userRepository, times(1)).save(testEmployee);
  }

  @Test
  @DisplayName("Should throw exception when employee not found during update")
  void testUpdate_EmployeeNotFound() {
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class, () -> employeeService.update(999L, updateRequest));

    verify(userRepository, times(1)).findById(999L);
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("Should reset password successfully")
  void testResetPassword_Success() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
    when(passwordEncoder.encode("password")).thenReturn("newHashedPassword");
    when(userRepository.save(any(User.class))).thenReturn(testEmployee);

    EmployeeResponse response = employeeService.resetPassword(1L);

    assertNotNull(response);
    assertEquals("newHashedPassword", testEmployee.getPasswordHash());
    verify(userRepository, times(1)).findById(1L);
    verify(passwordEncoder, times(1)).encode("password");
    verify(userRepository, times(1)).save(testEmployee);
  }

  @Test
  @DisplayName("Should throw exception when employee not found during password reset")
  void testResetPassword_EmployeeNotFound() {
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class, () -> employeeService.resetPassword(999L));

    verify(userRepository, times(1)).findById(999L);
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("Should list all employees")
  void testList_Success() {
    User employee2 =
        User.builder()
            .id(2L)
            .email("employee2@example.com")
            .name("Jane Employee")
            .employeeNo("EMP-0002")
            .department("Electrical")
            .status("ACTIVE")
            .createdAt(Instant.now())
            .roles(new HashSet<>(Arrays.asList(employeeRole)))
            .build();

    List<User> employees = Arrays.asList(testEmployee, employee2);
    when(userRepository.findDistinctByRoles_Name("EMPLOYEE")).thenReturn(employees);

    List<EmployeeResponse> responses = employeeService.list();

    assertNotNull(responses);
    assertEquals(2, responses.size());
    assertEquals("John Employee", responses.get(0).getName());
    assertEquals("Jane Employee", responses.get(1).getName());
    verify(userRepository, times(1)).findDistinctByRoles_Name("EMPLOYEE");
  }

  @Test
  @DisplayName("Should return empty list when no employees")
  void testList_EmptyList() {
    when(userRepository.findDistinctByRoles_Name("EMPLOYEE")).thenReturn(Arrays.asList());

    List<EmployeeResponse> responses = employeeService.list();

    assertNotNull(responses);
    assertEquals(0, responses.size());
    verify(userRepository, times(1)).findDistinctByRoles_Name("EMPLOYEE");
  }

  @Test
  @DisplayName("Should get employee by id")
  void testGet_Success() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

    EmployeeResponse response = employeeService.get(1L);

    assertNotNull(response);
    assertEquals(1L, response.getId());
    assertEquals("John Employee", response.getName());
    assertEquals("EMP-0001", response.getEmployeeNo());
    verify(userRepository, times(1)).findById(1L);
  }

  @Test
  @DisplayName("Should throw exception when employee not found")
  void testGet_EmployeeNotFound() {
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class, () -> employeeService.get(999L));

    verify(userRepository, times(1)).findById(999L);
  }

  @Test
  @DisplayName("Should delete employee successfully")
  void testDelete_Success() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
    doNothing().when(userRepository).delete(testEmployee);

    employeeService.delete(1L);

    verify(userRepository, times(1)).findById(1L);
    verify(userRepository, times(1)).delete(testEmployee);
  }

  @Test
  @DisplayName("Should throw exception when deleting non-existent employee")
  void testDelete_EmployeeNotFound() {
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class, () -> employeeService.delete(999L));

    verify(userRepository, times(1)).findById(999L);
    verify(userRepository, never()).delete(any(User.class));
  }

  @Test
  @DisplayName("Should set default password to 'password' when creating employee")
  void testCreate_DefaultPassword() {
    when(userRepository.findByEmail(createRequest.getEmail())).thenReturn(Optional.empty());
    when(roleRepository.findByName("EMPLOYEE")).thenReturn(Optional.of(employeeRole));
    when(passwordEncoder.encode("password")).thenReturn("hashedPassword");
    when(userRepository.count()).thenReturn(0L);
    when(userRepository.save(any(User.class))).thenReturn(testEmployee);

    employeeService.create(createRequest);

    verify(passwordEncoder, times(1)).encode("password");
  }

  @Test
  @DisplayName("Should include roles in employee response")
  void testGet_IncludeRoles() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

    EmployeeResponse response = employeeService.get(1L);

    assertNotNull(response);
    assertNotNull(response.getRoles());
    assertEquals(1, response.getRoles().size());
    assertTrue(response.getRoles().contains("employee"));
  }
}

