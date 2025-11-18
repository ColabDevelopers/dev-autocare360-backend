package com.autocare360.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.autocare360.dto.AuthResponse;
import com.autocare360.dto.LoginRequest;
import com.autocare360.dto.RegisterRequest;
import com.autocare360.dto.UserResponse;
import com.autocare360.entity.Role;
import com.autocare360.entity.User;
import com.autocare360.exception.ConflictException;
import com.autocare360.repo.RoleRepository;
import com.autocare360.repo.UserRepository;
import com.autocare360.security.JwtService;
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
@DisplayName("AuthService Tests")
class AuthServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private RoleRepository roleRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private JwtService jwtService;

  @InjectMocks private AuthService authService;

  private RegisterRequest registerRequest;
  private LoginRequest loginRequest;
  private User testUser;
  private Role customerRole;
  private Role adminRole;

  @BeforeEach
  void setUp() {
    registerRequest =
        RegisterRequest.builder()
            .email("newuser@example.com")
            .password("password123")
            .name("New User")
            .phone("+1-555-0200")
            .build();

    loginRequest = LoginRequest.builder().email("test@example.com").password("password").build();

    customerRole = Role.builder().name("CUSTOMER").build();

    adminRole = Role.builder().name("ADMIN").build();

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
  @DisplayName("Should register new user successfully")
  void testRegister_Success() {
    when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
    when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.of(customerRole));
    when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashedPassword");
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    UserResponse response = authService.register(registerRequest);

    assertNotNull(response);
    assertEquals(1L, response.getId());
    assertEquals("test@example.com", response.getEmail());
    assertEquals("Test User", response.getName());
    verify(userRepository, times(1)).findByEmail(registerRequest.getEmail());
    verify(roleRepository, times(1)).findByName("CUSTOMER");
    verify(passwordEncoder, times(1)).encode(registerRequest.getPassword());
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  @DisplayName("Should throw ConflictException when email already exists")
  void testRegister_EmailAlreadyExists() {
    when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(testUser));

    ConflictException exception =
        assertThrows(ConflictException.class, () -> authService.register(registerRequest));

    assertEquals("Email already in use", exception.getMessage());
    verify(userRepository, times(1)).findByEmail(registerRequest.getEmail());
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("Should create CUSTOMER role if not exists during registration")
  void testRegister_CreateRoleIfNotExists() {
    when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
    when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.empty());
    when(roleRepository.save(any(Role.class))).thenReturn(customerRole);
    when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashedPassword");
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    UserResponse response = authService.register(registerRequest);

    assertNotNull(response);
    verify(roleRepository, times(1)).findByName("CUSTOMER");
    verify(roleRepository, times(1)).save(any(Role.class));
  }

  @Test
  @DisplayName("Should login successfully with valid credentials")
  void testLogin_Success() {
    when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPasswordHash()))
        .thenReturn(true);
    when(jwtService.generateToken(anyString(), anyString(), any(String[].class)))
        .thenReturn("jwt-token");

    AuthResponse response = authService.login(loginRequest);

    assertNotNull(response);
    assertEquals("jwt-token", response.getAccessToken());
    assertEquals(3600, response.getExpiresIn());
    assertNotNull(response.getUser());
    assertEquals("test@example.com", response.getUser().getEmail());
    verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
    verify(passwordEncoder, times(1)).matches(loginRequest.getPassword(), testUser.getPasswordHash());
    verify(jwtService, times(1)).generateToken(anyString(), anyString(), any(String[].class));
  }

  @Test
  @DisplayName("Should throw exception when user not found during login")
  void testLogin_UserNotFound() {
    when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> authService.login(loginRequest));

    assertEquals("Invalid credentials", exception.getMessage());
    verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
    verify(passwordEncoder, never()).matches(anyString(), anyString());
  }

  @Test
  @DisplayName("Should throw exception when password is invalid")
  void testLogin_InvalidPassword() {
    when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPasswordHash()))
        .thenReturn(false);

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> authService.login(loginRequest));

    assertEquals("Invalid credentials", exception.getMessage());
    verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
    verify(passwordEncoder, times(1)).matches(loginRequest.getPassword(), testUser.getPasswordHash());
  }

  @Test
  @DisplayName("Should login admin with hardcoded credentials")
  void testLogin_AdminHardcodedCredentials() {
    LoginRequest adminRequest =
        LoginRequest.builder().email("nimal.admin@gmail.com").password("password").build();

    User adminUser =
        User.builder()
            .id(99L)
            .email("nimal.admin@gmail.com")
            .name("System Admin")
            .passwordHash("hashedPassword")
            .phone("+1-555-000-0000")
            .status("ACTIVE")
            .roles(new HashSet<>(Arrays.asList(adminRole)))
            .build();

    when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
    when(userRepository.findByEmail("nimal.admin@gmail.com")).thenReturn(Optional.of(adminUser));
    when(passwordEncoder.encode("password")).thenReturn("hashedPassword");
    when(userRepository.save(any(User.class))).thenReturn(adminUser);
    when(jwtService.generateToken(anyString(), anyString(), any(String[].class)))
        .thenReturn("admin-jwt-token");

    AuthResponse response = authService.login(adminRequest);

    assertNotNull(response);
    assertEquals("admin-jwt-token", response.getAccessToken());
    assertEquals(3600, response.getExpiresIn());
    verify(jwtService, times(1)).generateToken(anyString(), anyString(), any(String[].class));
  }

  @Test
  @DisplayName("Should create admin user if not exists with hardcoded credentials")
  void testLogin_CreateAdminIfNotExists() {
    LoginRequest adminRequest =
        LoginRequest.builder().email("nimal.admin@gmail.com").password("password").build();

    User newAdminUser =
        User.builder()
            .id(99L)
            .email("nimal.admin@gmail.com")
            .name("System Admin")
            .passwordHash("hashedPassword")
            .phone("+1-555-000-0000")
            .status("ACTIVE")
            .roles(new HashSet<>(Arrays.asList(adminRole)))
            .build();

    when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
    when(userRepository.findByEmail("nimal.admin@gmail.com")).thenReturn(Optional.empty());
    when(passwordEncoder.encode("password")).thenReturn("hashedPassword");
    when(userRepository.save(any(User.class))).thenReturn(newAdminUser);
    when(jwtService.generateToken(anyString(), anyString(), any(String[].class)))
        .thenReturn("admin-jwt-token");

    AuthResponse response = authService.login(adminRequest);

    assertNotNull(response);
    assertEquals("admin-jwt-token", response.getAccessToken());
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  @DisplayName("Should get user by id in me endpoint")
  void testMe_Success() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    UserResponse response = authService.me(1L);

    assertNotNull(response);
    assertEquals(1L, response.getId());
    assertEquals("test@example.com", response.getEmail());
    assertEquals("Test User", response.getName());
    verify(userRepository, times(1)).findById(1L);
  }

  @Test
  @DisplayName("Should throw exception when user not found in me endpoint")
  void testMe_UserNotFound() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class, () -> authService.me(1L));

    verify(userRepository, times(1)).findById(1L);
  }

  @Test
  @DisplayName("Should capitalize status correctly")
  void testLogin_StatusCapitalization() {
    testUser.setStatus("active");
    when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPasswordHash()))
        .thenReturn(true);
    when(jwtService.generateToken(anyString(), anyString(), any(String[].class)))
        .thenReturn("jwt-token");

    AuthResponse response = authService.login(loginRequest);

    assertNotNull(response);
    assertEquals("Active", response.getUser().getStatus());
  }

  @Test
  @DisplayName("Should handle null status as Active")
  void testLogin_NullStatus() {
    testUser.setStatus(null);
    when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPasswordHash()))
        .thenReturn(true);
    when(jwtService.generateToken(anyString(), anyString(), any(String[].class)))
        .thenReturn("jwt-token");

    AuthResponse response = authService.login(loginRequest);

    assertNotNull(response);
    assertEquals("Active", response.getUser().getStatus());
  }
}

