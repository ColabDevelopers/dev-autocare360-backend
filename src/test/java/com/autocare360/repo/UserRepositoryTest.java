package com.autocare360.repo;

import static org.junit.jupiter.api.Assertions.*;

import com.autocare360.entity.Role;
import com.autocare360.entity.User;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("UserRepository Integration Tests")
class UserRepositoryTest {

  @Autowired private TestEntityManager entityManager;

  @Autowired private UserRepository userRepository;

  @Autowired private RoleRepository roleRepository;

  private User testUser;
  private Role customerRole;

  @BeforeEach
  void setUp() {
    customerRole = Role.builder().name("CUSTOMER").build();
    customerRole = entityManager.persistAndFlush(customerRole);

    testUser =
        User.builder()
            .email("test@example.com")
            .name("Test User")
            .passwordHash("hashedPassword")
            .phone("+1-555-0100")
            .status("ACTIVE")
            .roles(new HashSet<>())
            .build();
    testUser.getRoles().add(customerRole);
    testUser = entityManager.persistAndFlush(testUser);
  }

  @Test
  @DisplayName("Should find user by email")
  void testFindByEmail_Success() {
    Optional<User> found = userRepository.findByEmail("test@example.com");

    assertTrue(found.isPresent());
    assertEquals("Test User", found.get().getName());
    assertEquals("test@example.com", found.get().getEmail());
  }

  @Test
  @DisplayName("Should return empty when user not found by email")
  void testFindByEmail_NotFound() {
    Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

    assertFalse(found.isPresent());
  }

  @Test
  @DisplayName("Should find user by name")
  void testFindByName_Success() {
    Optional<User> found = userRepository.findByName("Test User");

    assertTrue(found.isPresent());
    assertEquals("test@example.com", found.get().getEmail());
  }

  @Test
  @DisplayName("Should return empty when user not found by name")
  void testFindByName_NotFound() {
    Optional<User> found = userRepository.findByName("Nonexistent User");

    assertFalse(found.isPresent());
  }

  @Test
  @DisplayName("Should find users by role name")
  void testFindDistinctByRoles_Name_Success() {
    List<User> users = userRepository.findDistinctByRoles_Name("CUSTOMER");

    assertNotNull(users);
    assertEquals(1, users.size());
    assertEquals("test@example.com", users.get(0).getEmail());
  }

  @Test
  @DisplayName("Should return empty list when no users with role")
  void testFindDistinctByRoles_Name_EmptyList() {
    List<User> users = userRepository.findDistinctByRoles_Name("ADMIN");

    assertNotNull(users);
    assertEquals(0, users.size());
  }

  @Test
  @DisplayName("Should save new user with roles")
  void testSave_NewUser() {
    User newUser =
        User.builder()
            .email("newuser@example.com")
            .name("New User")
            .passwordHash("hashedPassword")
            .phone("+1-555-0200")
            .status("ACTIVE")
            .roles(new HashSet<>())
            .build();
    newUser.getRoles().add(customerRole);

    User saved = userRepository.save(newUser);

    assertNotNull(saved.getId());
    assertEquals("newuser@example.com", saved.getEmail());
    assertNotNull(saved.getCreatedAt());
    assertNotNull(saved.getUpdatedAt());
  }

  @Test
  @DisplayName("Should update existing user")
  void testSave_UpdateUser() {
    testUser.setName("Updated Name");
    testUser.setPhone("+1-555-9999");

    User updated = userRepository.save(testUser);

    assertEquals("Updated Name", updated.getName());
    assertEquals("+1-555-9999", updated.getPhone());
    assertNotNull(updated.getUpdatedAt());
  }

  @Test
  @DisplayName("Should delete user")
  void testDelete_Success() {
    userRepository.delete(testUser);
    entityManager.flush();

    Optional<User> found = userRepository.findById(testUser.getId());
    assertFalse(found.isPresent());
  }

  @Test
  @DisplayName("Should find all users")
  void testFindAll_Success() {
    User user2 =
        User.builder()
            .email("user2@example.com")
            .name("User Two")
            .passwordHash("hashedPassword")
            .phone("+1-555-0300")
            .status("ACTIVE")
            .roles(new HashSet<>())
            .build();
    user2.getRoles().add(customerRole);
    entityManager.persistAndFlush(user2);

    List<User> users = userRepository.findAll();

    assertNotNull(users);
    assertTrue(users.size() >= 2);
  }

  @Test
  @DisplayName("Should count users")
  void testCount_Success() {
    long count = userRepository.count();

    assertTrue(count >= 1);
  }

  @Test
  @DisplayName("Should handle user with employee fields")
  void testSave_EmployeeUser() {
    Role employeeRole = Role.builder().name("EMPLOYEE").build();
    employeeRole = entityManager.persistAndFlush(employeeRole);

    User employee =
        User.builder()
            .email("employee@example.com")
            .name("Employee User")
            .passwordHash("hashedPassword")
            .phone("+1-555-0400")
            .employeeNo("EMP-001")
            .department("Mechanical")
            .status("ACTIVE")
            .roles(new HashSet<>())
            .build();
    employee.getRoles().add(employeeRole);

    User saved = userRepository.save(employee);

    assertNotNull(saved.getId());
    assertEquals("EMP-001", saved.getEmployeeNo());
    assertEquals("Mechanical", saved.getDepartment());
  }

  @Test
  @DisplayName("Should enforce unique email constraint")
  void testSave_DuplicateEmail() {
    User duplicateUser =
        User.builder()
            .email("test@example.com") // Same email as testUser
            .name("Duplicate User")
            .passwordHash("hashedPassword")
            .phone("+1-555-0500")
            .status("ACTIVE")
            .roles(new HashSet<>())
            .build();

    assertThrows(Exception.class, () -> {
      userRepository.saveAndFlush(duplicateUser);
    });
  }

  @Test
  @DisplayName("Should auto-set timestamps on create")
  void testSave_AutoTimestamps() {
    User newUser =
        User.builder()
            .email("timestamps@example.com")
            .name("Timestamp User")
            .passwordHash("hashedPassword")
            .phone("+1-555-0600")
            .status("ACTIVE")
            .roles(new HashSet<>())
            .build();

    User saved = userRepository.saveAndFlush(newUser);

    assertNotNull(saved.getCreatedAt());
    assertNotNull(saved.getUpdatedAt());
  }

  @Test
  @DisplayName("Should auto-set default status on create")
  void testSave_DefaultStatus() {
    User newUser =
        User.builder()
            .email("status@example.com")
            .name("Status User")
            .passwordHash("hashedPassword")
            .phone("+1-555-0700")
            .roles(new HashSet<>())
            .build();
    // Note: status is not set

    User saved = userRepository.saveAndFlush(newUser);

    assertNotNull(saved.getStatus());
    assertEquals("ACTIVE", saved.getStatus());
  }
}

