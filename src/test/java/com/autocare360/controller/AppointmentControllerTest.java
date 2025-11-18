package com.autocare360.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.autocare360.dto.AppointmentRequest;
import com.autocare360.dto.AppointmentResponse;
import com.autocare360.dto.AvailabilityResponse;
import com.autocare360.security.JwtService;
import com.autocare360.service.AppointmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AppointmentController.class)
@DisplayName("AppointmentController Tests")
class AppointmentControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private AppointmentService appointmentService;

  @MockBean private JwtService jwtService;

  private AppointmentRequest appointmentRequest;
  private AppointmentResponse appointmentResponse;
  private String validToken;

  @BeforeEach
  void setUp() {
    validToken = "Bearer valid-jwt-token";

    appointmentRequest =
        AppointmentRequest.builder()
            .userId(1L)
            .service("Oil Change")
            .vehicle("Toyota Camry")
            .date(LocalDate.of(2024, 12, 25))
            .time(LocalTime.of(10, 0))
            .status("PENDING")
            .notes("Regular maintenance")
            .build();

    appointmentResponse = new AppointmentResponse();
    appointmentResponse.setId(1L);
    appointmentResponse.setService("Oil Change");
    appointmentResponse.setVehicle("Toyota Camry");
    appointmentResponse.setDate(LocalDate.of(2024, 12, 25));
    appointmentResponse.setTime(LocalTime.of(10, 0));
    appointmentResponse.setStatus("PENDING");
  }

  @Test
  @DisplayName("GET /api/appointments - Should get user appointments")
  void testGetMyAppointments_Success() throws Exception {
    List<AppointmentResponse> appointments = Arrays.asList(appointmentResponse);

    when(jwtService.isTokenValid("valid-jwt-token")).thenReturn(true);
    when(jwtService.extractSubject("valid-jwt-token")).thenReturn("1");
    when(appointmentService.listByUser(1L)).thenReturn(appointments);

    mockMvc
        .perform(get("/api/appointments").header("Authorization", validToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].service").value("Oil Change"));

    verify(appointmentService, times(1)).listByUser(1L);
  }

  @Test
  @DisplayName("GET /api/appointments - Should return 401 without token")
  void testGetMyAppointments_Unauthorized() throws Exception {
    mockMvc.perform(get("/api/appointments")).andExpect(status().isUnauthorized());

    verify(appointmentService, never()).listByUser(anyLong());
  }

  @Test
  @DisplayName("GET /api/appointments - Should return 401 with invalid token")
  void testGetMyAppointments_InvalidToken() throws Exception {
    when(jwtService.isTokenValid("invalid-token")).thenReturn(false);

    mockMvc
        .perform(get("/api/appointments").header("Authorization", "Bearer invalid-token"))
        .andExpect(status().isUnauthorized());

    verify(appointmentService, never()).listByUser(anyLong());
  }

  @Test
  @DisplayName("POST /api/appointments - Should create appointment")
  void testCreateAppointment_Success() throws Exception {
    when(jwtService.isTokenValid("valid-jwt-token")).thenReturn(true);
    when(jwtService.extractSubject("valid-jwt-token")).thenReturn("1");
    when(appointmentService.create(any(AppointmentRequest.class))).thenReturn(appointmentResponse);

    mockMvc
        .perform(
            post("/api/appointments")
                .header("Authorization", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appointmentRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.service").value("Oil Change"));

    verify(appointmentService, times(1)).create(any(AppointmentRequest.class));
  }

  @Test
  @DisplayName("POST /api/appointments - Should return 401 without token")
  void testCreateAppointment_Unauthorized() throws Exception {
    mockMvc
        .perform(
            post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appointmentRequest)))
        .andExpect(status().isUnauthorized());

    verify(appointmentService, never()).create(any(AppointmentRequest.class));
  }

  @Test
  @DisplayName("POST /api/appointments - Should fail validation with missing fields")
  void testCreateAppointment_ValidationError() throws Exception {
    appointmentRequest.setService(null);
    appointmentRequest.setVehicle(null);

    when(jwtService.isTokenValid("valid-jwt-token")).thenReturn(true);
    when(jwtService.extractSubject("valid-jwt-token")).thenReturn("1");

    mockMvc
        .perform(
            post("/api/appointments")
                .header("Authorization", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appointmentRequest)))
        .andExpect(status().isBadRequest());

    verify(appointmentService, never()).create(any(AppointmentRequest.class));
  }

  @Test
  @DisplayName("PUT /api/appointments/{id} - Should update appointment")
  void testUpdateAppointment_Success() throws Exception {
    when(jwtService.isTokenValid("valid-jwt-token")).thenReturn(true);
    when(jwtService.extractSubject("valid-jwt-token")).thenReturn("1");
    when(appointmentService.update(eq(1L), any(AppointmentRequest.class)))
        .thenReturn(appointmentResponse);

    mockMvc
        .perform(
            put("/api/appointments/1")
                .header("Authorization", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appointmentRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1));

    verify(appointmentService, times(1)).update(eq(1L), any(AppointmentRequest.class));
  }

  @Test
  @DisplayName("PUT /api/appointments/{id} - Should return 401 without token")
  void testUpdateAppointment_Unauthorized() throws Exception {
    mockMvc
        .perform(
            put("/api/appointments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appointmentRequest)))
        .andExpect(status().isUnauthorized());

    verify(appointmentService, never()).update(anyLong(), any(AppointmentRequest.class));
  }

  @Test
  @DisplayName("DELETE /api/appointments/{id} - Should delete appointment")
  void testDeleteAppointment_Success() throws Exception {
    when(jwtService.isTokenValid("valid-jwt-token")).thenReturn(true);
    when(jwtService.extractSubject("valid-jwt-token")).thenReturn("1");
    doNothing().when(appointmentService).delete(1L);

    mockMvc
        .perform(delete("/api/appointments/1").header("Authorization", validToken))
        .andExpect(status().isNoContent());

    verify(appointmentService, times(1)).delete(1L);
  }

  @Test
  @DisplayName("DELETE /api/appointments/{id} - Should return 401 without token")
  void testDeleteAppointment_Unauthorized() throws Exception {
    mockMvc.perform(delete("/api/appointments/1")).andExpect(status().isUnauthorized());

    verify(appointmentService, never()).delete(anyLong());
  }

  @Test
  @DisplayName("GET /api/availability - Should get availability")
  void testGetAvailability_Success() throws Exception {
    AvailabilityResponse availabilityResponse = new AvailabilityResponse();
    availabilityResponse.setTimeSlots(Arrays.asList("09:00", "10:00", "11:00"));
    availabilityResponse.setAvailableTechnicians(Arrays.asList("John", "Jane"));

    when(appointmentService.getAvailability(any(LocalDate.class), anyString()))
        .thenReturn(availabilityResponse);

    mockMvc
        .perform(get("/api/availability").param("date", "2024-12-25").param("technician", "John"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.timeSlots[0]").value("09:00"))
        .andExpect(jsonPath("$.availableTechnicians[0]").value("John"));

    verify(appointmentService, times(1)).getAvailability(any(LocalDate.class), eq("John"));
  }

  @Test
  @DisplayName("GET /api/availability - Should get availability without technician")
  void testGetAvailability_WithoutTechnician() throws Exception {
    AvailabilityResponse availabilityResponse = new AvailabilityResponse();
    availabilityResponse.setTimeSlots(Arrays.asList("09:00", "10:00"));
    availabilityResponse.setAvailableTechnicians(Arrays.asList("John", "Jane"));

    when(appointmentService.getAvailability(any(LocalDate.class), isNull()))
        .thenReturn(availabilityResponse);

    mockMvc
        .perform(get("/api/availability").param("date", "2024-12-25"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.timeSlots").isArray())
        .andExpect(jsonPath("$.availableTechnicians").isArray());

    verify(appointmentService, times(1)).getAvailability(any(LocalDate.class), isNull());
  }
}

