package com.autocare360.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.autocare360.dto.VehicleCreateDTO;
import com.autocare360.entity.Vehicle;
import com.autocare360.security.JwtService;
import com.autocare360.service.VehicleService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@WebMvcTest(VehicleController.class)
@DisplayName("VehicleController Tests")
class VehicleControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private VehicleService vehicleService;

  @MockBean private JwtService jwtService;

  private Vehicle testVehicle;
  private VehicleCreateDTO createDTO;
  private String validToken;

  @BeforeEach
  void setUp() {
    validToken = "Bearer valid-jwt-token";

    testVehicle = new Vehicle();
    testVehicle.setId(1L);
    testVehicle.setMake("Toyota");
    testVehicle.setModel("Camry");
    testVehicle.setYear(2022);
    testVehicle.setVin("1HGBH41JXMN109186");
    testVehicle.setPlateNumber("ABC-1234");
    testVehicle.setColor("Blue");
    testVehicle.setUserId(1L);

    createDTO = new VehicleCreateDTO();
    createDTO.setMake("Toyota");
    createDTO.setModel("Camry");
    createDTO.setYear(2022);
    createDTO.setVin("1HGBH41JXMN109186");
    createDTO.setPlateNumber("ABC-1234");
    createDTO.setColor("Blue");
  }

  @Test
  @DisplayName("GET /api/vehicles - Should get user vehicles")
  void testGetMyVehicles_Success() throws Exception {
    List<Vehicle> vehicles = Arrays.asList(testVehicle);

    when(jwtService.isTokenValid("valid-jwt-token")).thenReturn(true);
    when(jwtService.extractSubject("valid-jwt-token")).thenReturn("1");
    when(vehicleService.listByUser(1L)).thenReturn(vehicles);

    mockMvc
        .perform(get("/api/vehicles").header("Authorization", validToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].make").value("Toyota"));

    verify(vehicleService, times(1)).listByUser(1L);
  }

  @Test
  @DisplayName("GET /api/vehicles - Should return 401 without token")
  void testGetMyVehicles_Unauthorized() throws Exception {
    mockMvc.perform(get("/api/vehicles")).andExpect(status().isUnauthorized());

    verify(vehicleService, never()).listByUser(anyLong());
  }

  @Test
  @DisplayName("POST /api/vehicles - Should create vehicle")
  void testCreateVehicle_Success() throws Exception {
    when(jwtService.isTokenValid("valid-jwt-token")).thenReturn(true);
    when(jwtService.extractSubject("valid-jwt-token")).thenReturn("1");
    when(vehicleService.existsByVinAndUserId(anyString(), anyLong())).thenReturn(false);
    when(vehicleService.create(any(Vehicle.class))).thenReturn(testVehicle);

    mockMvc
        .perform(
            post("/api/vehicles")
                .header("Authorization", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.make").value("Toyota"));

    verify(vehicleService, times(1)).create(any(Vehicle.class));
  }

  @Test
  @DisplayName("POST /api/vehicles - Should return 409 when VIN already exists")
  void testCreateVehicle_DuplicateVIN() throws Exception {
    when(jwtService.isTokenValid("valid-jwt-token")).thenReturn(true);
    when(jwtService.extractSubject("valid-jwt-token")).thenReturn("1");
    when(vehicleService.existsByVinAndUserId("1HGBH41JXMN109186", 1L)).thenReturn(true);

    mockMvc
        .perform(
            post("/api/vehicles")
                .header("Authorization", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isConflict());

    verify(vehicleService, never()).create(any(Vehicle.class));
  }

  @Test
  @DisplayName("GET /api/vehicles/{id} - Should get vehicle by id")
  void testGetVehicle_Success() throws Exception {
    when(jwtService.isTokenValid("valid-jwt-token")).thenReturn(true);
    when(jwtService.extractSubject("valid-jwt-token")).thenReturn("1");
    when(vehicleService.get(1L)).thenReturn(testVehicle);

    mockMvc
        .perform(get("/api/vehicles/1").header("Authorization", validToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.make").value("Toyota"));

    verify(vehicleService, times(1)).get(1L);
  }

  @Test
  @DisplayName("GET /api/vehicles/{id} - Should return 404 when not found")
  void testGetVehicle_NotFound() throws Exception {
    when(jwtService.isTokenValid("valid-jwt-token")).thenReturn(true);
    when(jwtService.extractSubject("valid-jwt-token")).thenReturn("1");
    when(vehicleService.get(999L)).thenReturn(null);

    mockMvc
        .perform(get("/api/vehicles/999").header("Authorization", validToken))
        .andExpect(status().isNotFound());

    verify(vehicleService, times(1)).get(999L);
  }

  @Test
  @DisplayName("PUT /api/vehicles/{id} - Should update vehicle")
  void testUpdateVehicle_Success() throws Exception {
    // For update, we just need to send a vehicle patch object
    Vehicle updateDTO = new Vehicle();
    updateDTO.setColor("Red");

    when(jwtService.isTokenValid("valid-jwt-token")).thenReturn(true);
    when(jwtService.extractSubject("valid-jwt-token")).thenReturn("1");
    when(vehicleService.update(eq(1L), any(Vehicle.class))).thenReturn(testVehicle);

    mockMvc
        .perform(
            put("/api/vehicles/1")
                .header("Authorization", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isOk());

    verify(vehicleService, times(1)).update(eq(1L), any(Vehicle.class));
  }

  @Test
  @DisplayName("DELETE /api/vehicles/{id} - Should delete vehicle")
  void testDeleteVehicle_Success() throws Exception {
    when(jwtService.isTokenValid("valid-jwt-token")).thenReturn(true);
    when(jwtService.extractSubject("valid-jwt-token")).thenReturn("1");
    doNothing().when(vehicleService).delete(1L);

    mockMvc
        .perform(delete("/api/vehicles/1").header("Authorization", validToken))
        .andExpect(status().isNoContent());

    verify(vehicleService, times(1)).delete(1L);
  }
}

