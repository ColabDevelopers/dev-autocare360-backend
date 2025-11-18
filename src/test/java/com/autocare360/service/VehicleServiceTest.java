package com.autocare360.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.autocare360.entity.Vehicle;
import com.autocare360.repo.VehicleRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("VehicleService Tests")
class VehicleServiceTest {

  @Mock private VehicleRepository vehicleRepository;

  @InjectMocks private VehicleService vehicleService;

  private Vehicle testVehicle;

  @BeforeEach
  void setUp() {
    testVehicle = new Vehicle();
    testVehicle.setId(1L);
    testVehicle.setMake("Toyota");
    testVehicle.setModel("Camry");
    testVehicle.setYear(2022);
    testVehicle.setVin("1HGBH41JXMN109186");
    testVehicle.setPlateNumber("ABC-1234");
    testVehicle.setColor("Blue");
    testVehicle.setUserId(1L);
  }

  @Test
  @DisplayName("Should check if vehicle exists by VIN and user ID")
  void testExistsByVinAndUserId_True() {
    when(vehicleRepository.existsByVinAndUser_Id("1HGBH41JXMN109186", 1L)).thenReturn(true);

    boolean exists = vehicleService.existsByVinAndUserId("1HGBH41JXMN109186", 1L);

    assertTrue(exists);
    verify(vehicleRepository, times(1)).existsByVinAndUser_Id("1HGBH41JXMN109186", 1L);
  }

  @Test
  @DisplayName("Should return false when vehicle does not exist")
  void testExistsByVinAndUserId_False() {
    when(vehicleRepository.existsByVinAndUser_Id("NONEXISTENT", 1L)).thenReturn(false);

    boolean exists = vehicleService.existsByVinAndUserId("NONEXISTENT", 1L);

    assertFalse(exists);
    verify(vehicleRepository, times(1)).existsByVinAndUser_Id("NONEXISTENT", 1L);
  }

  @Test
  @DisplayName("Should create vehicle successfully")
  void testCreate_Success() {
    when(vehicleRepository.save(testVehicle)).thenReturn(testVehicle);

    Vehicle created = vehicleService.create(testVehicle);

    assertNotNull(created);
    assertEquals("Toyota", created.getMake());
    assertEquals("Camry", created.getModel());
    assertEquals(2022, created.getYear());
    verify(vehicleRepository, times(1)).save(testVehicle);
  }

  @Test
  @DisplayName("Should get vehicle by ID")
  void testGet_Success() {
    when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));

    Vehicle found = vehicleService.get(1L);

    assertNotNull(found);
    assertEquals(1L, found.getId());
    assertEquals("Toyota", found.getMake());
    verify(vehicleRepository, times(1)).findById(1L);
  }

  @Test
  @DisplayName("Should return null when vehicle not found")
  void testGet_NotFound() {
    when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

    Vehicle found = vehicleService.get(999L);

    assertNull(found);
    verify(vehicleRepository, times(1)).findById(999L);
  }

  @Test
  @DisplayName("Should list vehicles by user")
  void testListByUser_Success() {
    Vehicle vehicle2 = new Vehicle();
    vehicle2.setId(2L);
    vehicle2.setMake("Honda");
    vehicle2.setModel("Accord");
    vehicle2.setUserId(1L);

    List<Vehicle> vehicles = Arrays.asList(testVehicle, vehicle2);
    when(vehicleRepository.findByUser_Id(1L)).thenReturn(vehicles);

    List<Vehicle> found = vehicleService.listByUser(1L);

    assertNotNull(found);
    assertEquals(2, found.size());
    assertEquals("Toyota", found.get(0).getMake());
    assertEquals("Honda", found.get(1).getMake());
    verify(vehicleRepository, times(1)).findByUser_Id(1L);
  }

  @Test
  @DisplayName("Should return empty list when user has no vehicles")
  void testListByUser_EmptyList() {
    when(vehicleRepository.findByUser_Id(999L)).thenReturn(Arrays.asList());

    List<Vehicle> found = vehicleService.listByUser(999L);

    assertNotNull(found);
    assertEquals(0, found.size());
    verify(vehicleRepository, times(1)).findByUser_Id(999L);
  }

  @Test
  @DisplayName("Should update vehicle successfully")
  void testUpdate_Success() {
    Vehicle patch = new Vehicle();
    patch.setMake("Honda");
    patch.setColor("Red");

    when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
    when(vehicleRepository.save(any(Vehicle.class))).thenReturn(testVehicle);

    Vehicle updated = vehicleService.update(1L, patch);

    assertNotNull(updated);
    assertEquals("Honda", testVehicle.getMake());
    assertEquals("Red", testVehicle.getColor());
    assertEquals("Camry", testVehicle.getModel()); // Unchanged
    verify(vehicleRepository, times(1)).findById(1L);
    verify(vehicleRepository, times(1)).save(testVehicle);
  }

  @Test
  @DisplayName("Should return null when updating non-existent vehicle")
  void testUpdate_NotFound() {
    Vehicle patch = new Vehicle();
    patch.setMake("Honda");

    when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

    Vehicle updated = vehicleService.update(999L, patch);

    assertNull(updated);
    verify(vehicleRepository, times(1)).findById(999L);
    verify(vehicleRepository, never()).save(any(Vehicle.class));
  }

  @Test
  @DisplayName("Should update only non-null fields")
  void testUpdate_PartialUpdate() {
    Vehicle patch = new Vehicle();
    patch.setYear(2023);
    // Other fields are null

    when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
    when(vehicleRepository.save(any(Vehicle.class))).thenReturn(testVehicle);

    Vehicle updated = vehicleService.update(1L, patch);

    assertNotNull(updated);
    assertEquals(2023, testVehicle.getYear());
    assertEquals("Toyota", testVehicle.getMake()); // Unchanged
    assertEquals("Camry", testVehicle.getModel()); // Unchanged
    verify(vehicleRepository, times(1)).save(testVehicle);
  }

  @Test
  @DisplayName("Should delete vehicle successfully")
  void testDelete_Success() {
    doNothing().when(vehicleRepository).deleteById(1L);

    vehicleService.delete(1L);

    verify(vehicleRepository, times(1)).deleteById(1L);
  }

  @Test
  @DisplayName("Should link vehicle to user successfully")
  void testLinkVehicleToUser_Success() {
    when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
    when(vehicleRepository.save(testVehicle)).thenReturn(testVehicle);

    Vehicle linked = vehicleService.linkVehicleToUser(1L, 5L);

    assertNotNull(linked);
    assertEquals(5L, testVehicle.getUserId());
    verify(vehicleRepository, times(1)).findById(1L);
    verify(vehicleRepository, times(1)).save(testVehicle);
  }

  @Test
  @DisplayName("Should return null when linking non-existent vehicle")
  void testLinkVehicleToUser_VehicleNotFound() {
    when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

    Vehicle linked = vehicleService.linkVehicleToUser(999L, 5L);

    assertNull(linked);
    verify(vehicleRepository, times(1)).findById(999L);
    verify(vehicleRepository, never()).save(any(Vehicle.class));
  }

  @Test
  @DisplayName("Should list all vehicles")
  void testListAll_Success() {
    Vehicle vehicle2 = new Vehicle();
    vehicle2.setId(2L);
    vehicle2.setMake("Honda");

    List<Vehicle> vehicles = Arrays.asList(testVehicle, vehicle2);
    when(vehicleRepository.findAll()).thenReturn(vehicles);

    List<Vehicle> found = vehicleService.listAll();

    assertNotNull(found);
    assertEquals(2, found.size());
    verify(vehicleRepository, times(1)).findAll();
  }
}

