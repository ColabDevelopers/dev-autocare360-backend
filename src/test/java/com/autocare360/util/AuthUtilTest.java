package com.autocare360.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.autocare360.entity.User;
import com.autocare360.repo.UserRepository;
import java.util.HashSet;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthUtil Tests")
class AuthUtilTest {

  @Mock private UserRepository userRepository;

  @Mock private Authentication authentication;

  @InjectMocks private AuthUtil authUtil;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser =
        User.builder()
            .id(1L)
            .email("test@example.com")
            .name("Test User")
            .passwordHash("hashedPassword")
            .phone("+1-555-0100")
            .status("ACTIVE")
            .roles(new HashSet<>())
            .build();
  }

  @Test
  @DisplayName("Should get user ID from authentication successfully")
  void testGetUserIdFromAuth_Success() {
    when(authentication.getPrincipal()).thenReturn("test@example.com");
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

    Long userId = authUtil.getUserIdFromAuth(authentication);

    assertEquals(1L, userId);
    verify(authentication, times(1)).getPrincipal();
    verify(userRepository, times(1)).findByEmail("test@example.com");
  }

  @Test
  @DisplayName("Should throw exception when authentication is null")
  void testGetUserIdFromAuth_NullAuthentication() {
    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> authUtil.getUserIdFromAuth(null));

    assertEquals("User not authenticated", exception.getMessage());
    verify(userRepository, never()).findByEmail(anyString());
  }

  @Test
  @DisplayName("Should throw exception when principal is null")
  void testGetUserIdFromAuth_NullPrincipal() {
    when(authentication.getPrincipal()).thenReturn(null);

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> authUtil.getUserIdFromAuth(authentication));

    assertEquals("User not authenticated", exception.getMessage());
    verify(userRepository, never()).findByEmail(anyString());
  }

  @Test
  @DisplayName("Should throw exception when user not found by email")
  void testGetUserIdFromAuth_UserNotFound() {
    when(authentication.getPrincipal()).thenReturn("nonexistent@example.com");
    when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> authUtil.getUserIdFromAuth(authentication));

    assertTrue(exception.getMessage().contains("User not found with email"));
    verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
  }

  @Test
  @DisplayName("Should get full user from authentication successfully")
  void testGetUserFromAuth_Success() {
    when(authentication.getPrincipal()).thenReturn("test@example.com");
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

    User user = authUtil.getUserFromAuth(authentication);

    assertNotNull(user);
    assertEquals(1L, user.getId());
    assertEquals("test@example.com", user.getEmail());
    assertEquals("Test User", user.getName());
    verify(authentication, times(1)).getPrincipal();
    verify(userRepository, times(1)).findByEmail("test@example.com");
  }

  @Test
  @DisplayName("Should throw exception when getting user from null authentication")
  void testGetUserFromAuth_NullAuthentication() {
    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> authUtil.getUserFromAuth(null));

    assertEquals("User not authenticated", exception.getMessage());
    verify(userRepository, never()).findByEmail(anyString());
  }

  @Test
  @DisplayName("Should throw exception when getting user from null principal")
  void testGetUserFromAuth_NullPrincipal() {
    when(authentication.getPrincipal()).thenReturn(null);

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> authUtil.getUserFromAuth(authentication));

    assertEquals("User not authenticated", exception.getMessage());
    verify(userRepository, never()).findByEmail(anyString());
  }

  @Test
  @DisplayName("Should throw exception when user not found in getUserFromAuth")
  void testGetUserFromAuth_UserNotFound() {
    when(authentication.getPrincipal()).thenReturn("nonexistent@example.com");
    when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> authUtil.getUserFromAuth(authentication));

    assertTrue(exception.getMessage().contains("User not found with email"));
    verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
  }

  @Test
  @DisplayName("Should get user ID from email successfully")
  void testGetUserIdFromEmail_Success() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

    Long userId = authUtil.getUserIdFromEmail("test@example.com");

    assertEquals(1L, userId);
    verify(userRepository, times(1)).findByEmail("test@example.com");
  }

  @Test
  @DisplayName("Should throw exception when user not found by email in getUserIdFromEmail")
  void testGetUserIdFromEmail_UserNotFound() {
    when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

    RuntimeException exception =
        assertThrows(
            RuntimeException.class, () -> authUtil.getUserIdFromEmail("nonexistent@example.com"));

    assertTrue(exception.getMessage().contains("User not found with email"));
    verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
  }

  @Test
  @DisplayName("Should handle different email formats")
  void testGetUserIdFromEmail_DifferentFormats() {
    User user1 =
        User.builder()
            .id(10L)
            .email("user.name@example.com")
            .name("User Name")
            .passwordHash("hash")
            .roles(new HashSet<>())
            .build();

    User user2 =
        User.builder()
            .id(20L)
            .email("user+tag@example.com")
            .name("User Tag")
            .passwordHash("hash")
            .roles(new HashSet<>())
            .build();

    when(userRepository.findByEmail("user.name@example.com")).thenReturn(Optional.of(user1));
    when(userRepository.findByEmail("user+tag@example.com")).thenReturn(Optional.of(user2));

    assertEquals(10L, authUtil.getUserIdFromEmail("user.name@example.com"));
    assertEquals(20L, authUtil.getUserIdFromEmail("user+tag@example.com"));
  }

  @Test
  @DisplayName("Should handle authentication with email as string principal")
  void testGetUserIdFromAuth_EmailPrincipal() {
    String email = "user@domain.com";
    User user =
        User.builder()
            .id(5L)
            .email(email)
            .name("User")
            .passwordHash("hash")
            .roles(new HashSet<>())
            .build();

    when(authentication.getPrincipal()).thenReturn(email);
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

    Long userId = authUtil.getUserIdFromAuth(authentication);

    assertEquals(5L, userId);
  }
}

