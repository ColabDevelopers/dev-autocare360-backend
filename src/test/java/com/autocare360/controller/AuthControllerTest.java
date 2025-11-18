package com.autocare360.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.autocare360.dto.AuthResponse;
import com.autocare360.dto.LoginRequest;
import com.autocare360.dto.RegisterRequest;
import com.autocare360.dto.UserResponse;
import com.autocare360.exception.ConflictException;
import com.autocare360.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private AuthService authService;

  private RegisterRequest registerRequest;
  private LoginRequest loginRequest;
  private UserResponse userResponse;
  private AuthResponse authResponse;

  @BeforeEach
  void setUp() {
    registerRequest =
        RegisterRequest.builder()
            .email("test@example.com")
            .password("password123")
            .name("Test User")
            .phone("+1-555-0100")
            .build();

    loginRequest = LoginRequest.builder().email("test@example.com").password("password").build();

    userResponse =
        UserResponse.builder()
            .id(1L)
            .email("test@example.com")
            .name("Test User")
            .roles(Arrays.asList("customer"))
            .status("Active")
            .build();

    authResponse =
        AuthResponse.builder().accessToken("jwt-token").expiresIn(Integer.valueOf(3600)).user(userResponse).build();
  }

  @Test
  @DisplayName("POST /auth/register - Should register successfully")
  void testRegister_Success() throws Exception {
    when(authService.register(any(RegisterRequest.class))).thenReturn(userResponse);

    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.email").value("test@example.com"))
        .andExpect(jsonPath("$.name").value("Test User"));

    verify(authService, times(1)).register(any(RegisterRequest.class));
  }

  @Test
  @DisplayName("POST /auth/register - Should fail with conflict when email exists")
  void testRegister_EmailConflict() throws Exception {
    when(authService.register(any(RegisterRequest.class)))
        .thenThrow(new ConflictException("Email already in use"));

    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isConflict());

    verify(authService, times(1)).register(any(RegisterRequest.class));
  }

  @Test
  @DisplayName("POST /auth/register - Should fail validation with invalid email")
  void testRegister_InvalidEmail() throws Exception {
    registerRequest.setEmail("invalid-email");

    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isBadRequest());

    verify(authService, never()).register(any(RegisterRequest.class));
  }

  @Test
  @DisplayName("POST /auth/register - Should fail with missing required fields")
  void testRegister_MissingFields() throws Exception {
    registerRequest.setEmail(null);
    registerRequest.setPassword(null);

    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isBadRequest());

    verify(authService, never()).register(any(RegisterRequest.class));
  }

  @Test
  @DisplayName("POST /auth/login - Should login successfully")
  void testLogin_Success() throws Exception {
    when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("jwt-token"))
        .andExpect(jsonPath("$.expiresIn").value(3600))
        .andExpect(jsonPath("$.user.email").value("test@example.com"));

    verify(authService, times(1)).login(any(LoginRequest.class));
  }

  @Test
  @DisplayName("POST /auth/login - Should fail with invalid credentials")
  void testLogin_InvalidCredentials() throws Exception {
    when(authService.login(any(LoginRequest.class)))
        .thenThrow(new IllegalArgumentException("Invalid credentials"));

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isBadRequest());

    verify(authService, times(1)).login(any(LoginRequest.class));
  }

  @Test
  @DisplayName("POST /auth/login - Should fail validation with missing fields")
  void testLogin_MissingFields() throws Exception {
    loginRequest.setEmail(null);
    loginRequest.setPassword(null);

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isBadRequest());

    verify(authService, never()).login(any(LoginRequest.class));
  }

  @Test
  @DisplayName("POST /auth/refresh - Should return not implemented")
  void testRefresh_NotImplemented() throws Exception {
    mockMvc
        .perform(
            post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"token\"}"))
        .andExpect(status().isNotImplemented());
  }

  @Test
  @DisplayName("POST /auth/logout - Should logout successfully")
  void testLogout_Success() throws Exception {
    mockMvc.perform(post("/auth/logout")).andExpect(status().isNoContent());
  }
}

