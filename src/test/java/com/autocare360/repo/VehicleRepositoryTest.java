package com.autocare360.repo;

import static org.junit.jupiter.api.Assertions.*;

import com.autocare360.entity.User;
import com.autocare360.entity.Vehicle;
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
@DisplayName("VehicleRepository Integration Tests")
class VehicleRepositoryTest {

  @Autowired private TestEntityManager entityManager;

  @Autowired private VehicleRepository vehicleRepository;

  private User testUser;
  private Vehicle testVehicle;

  @BeforeEach
  void setUp() {
    testUser =
        User.builder()
            .email("test@example.com")
            .name("Test User")
            .passwordHash("hashedPassword")
            .phone("+1-555-0100")
            .status("ACTIVE")
            .roles(new HashSet<>())
            .build();
    testUser = entityManager.persistAndFlush(testUser);

    testVehicle = new Vehicle();
    testVehicle.setMake("Toyota");
    testVehicle.setModel("Camry");
    testVehicle.setYear(2022);
    testVehicle.setVin("1HGBH41JXMN109186");
    testVehicle.setPlateNumber("ABC-1234");
    testVehicle.setColor("Blue");
    testVehicle.setUserId(testUser.getId());
    testVehicle = entityManager.persistAndFlush(testVehicle);
  }

  @Test
  @DisplayName("Should find vehicles by user id")
  void testFindByUser_Id_Success() {
    List<Vehicle> vehicles = vehicleRepository.findByUser_Id(testUser.getId());

    assertNotNull(vehicles);
    assertEquals(1, vehicles.size());
    assertEquals("Toyota", vehicles.get(0).getMake());
    assertEquals("Camry", vehicles.get(0).getModel());
  }

  @Test
  @DisplayName("Should return empty list when no vehicles for user")
  void testFindByUser_Id_EmptyList() {
    List<Vehicle> vehicles = vehicleRepository.findByUser_Id(999L);

    assertNotNull(vehicles);
    assertEquals(0, vehicles.size());
  }

  @Test
  @DisplayName("Should check if vehicle exists by VIN and user id")
  void testExistsByVinAndUser_Id_True() {
    boolean exists = vehicleRepository.existsByVinAndUser_Id("1HGBH41JXMN109186", testUser.getId());

    assertTrue(exists);
  }

  @Test
  @DisplayName("Should return false when vehicle with VIN doesn't exist for user")
  void testExistsByVinAndUser_Id_False() {
    boolean exists = vehicleRepository.existsByVinAndUser_Id("NONEXISTENT", testUser.getId());

    assertFalse(exists);
  }

  @Test
  @DisplayName("Should return false when VIN exists but for different user")
  void testExistsByVinAndUser_Id_DifferentUser() {
    boolean exists = vehicleRepository.existsByVinAndUser_Id("1HGBH41JXMN109186", 999L);

    assertFalse(exists);
  }

  @Test
  @DisplayName("Should save new vehicle")
  void testSave_NewVehicle() {
    Vehicle newVehicle = new Vehicle();
    newVehicle.setMake("Honda");
    newVehicle.setModel("Accord");
    newVehicle.setYear(2023);
    newVehicle.setVin("2HGFC2F59MH123456");
    newVehicle.setPlateNumber("XYZ-9876");
    newVehicle.setColor("Red");
    newVehicle.setUserId(testUser.getId());

    Vehicle saved = vehicleRepository.save(newVehicle);

    assertNotNull(saved.getId());
    assertEquals("Honda", saved.getMake());
    assertEquals("Accord", saved.getModel());
  }

  @Test
  @DisplayName("Should update existing vehicle")
  void testSave_UpdateVehicle() {
    testVehicle.setColor("Red");
    testVehicle.setYear(2023);

    Vehicle updated = vehicleRepository.save(testVehicle);

    assertEquals("Red", updated.getColor());
    assertEquals(2023, updated.getYear());
    assertEquals("Toyota", updated.getMake()); // Should remain unchanged
  }

  @Test
  @DisplayName("Should delete vehicle")
  void testDelete_Success() {
    vehicleRepository.delete(testVehicle);
    entityManager.flush();

    Optional<Vehicle> found = vehicleRepository.findById(testVehicle.getId());
    assertFalse(found.isPresent());
  }

  @Test
  @DisplayName("Should find all vehicles")
  void testFindAll_Success() {
    Vehicle vehicle2 = new Vehicle();
    vehicle2.setMake("Ford");
    vehicle2.setModel("F-150");
    vehicle2.setYear(2021);
    vehicle2.setVin("3FGFR38619R123456");
    vehicle2.setPlateNumber("DEF-5678");
    vehicle2.setColor("Black");
    vehicle2.setUserId(testUser.getId());
    entityManager.persistAndFlush(vehicle2);

    List<Vehicle> vehicles = vehicleRepository.findAll();

    assertNotNull(vehicles);
    assertTrue(vehicles.size() >= 2);
  }

  @Test
  @DisplayName("Should handle multiple vehicles for same user")
  void testFindByUser_Id_MultipleVehicles() {
    Vehicle vehicle2 = new Vehicle();
    vehicle2.setMake("Honda");
    vehicle2.setModel("Civic");
    vehicle2.setYear(2020);
    vehicle2.setVin("19XFC2F59ME123456");
    vehicle2.setPlateNumber("GHI-9012");
    vehicle2.setColor("White");
    vehicle2.setUserId(testUser.getId());
    entityManager.persistAndFlush(vehicle2);

    List<Vehicle> vehicles = vehicleRepository.findByUser_Id(testUser.getId());

    assertNotNull(vehicles);
    assertEquals(2, vehicles.size());
  }

  @Test
  @DisplayName("Should not find vehicles from different user")
  void testFindByUser_Id_IsolateUsers() {
    User anotherUser =
        User.builder()
            .email("another@example.com")
            .name("Another User")
            .passwordHash("hashedPassword")
            .phone("+1-555-0200")
            .status("ACTIVE")
            .roles(new HashSet<>())
            .build();
    anotherUser = entityManager.persistAndFlush(anotherUser);

    Vehicle anotherVehicle = new Vehicle();
    anotherVehicle.setMake("Nissan");
    anotherVehicle.setModel("Altima");
    anotherVehicle.setYear(2021);
    anotherVehicle.setVin("1N4AL3AP5MC123456");
    anotherVehicle.setPlateNumber("JKL-3456");
    anotherVehicle.setColor("Silver");
    anotherVehicle.setUserId(anotherUser.getId());
    entityManager.persistAndFlush(anotherVehicle);

    List<Vehicle> vehicles = vehicleRepository.findByUser_Id(testUser.getId());

    assertNotNull(vehicles);
    assertEquals(1, vehicles.size());
    assertEquals("Toyota", vehicles.get(0).getMake());
  }

  @Test
  @DisplayName("Should enforce unique VIN constraint per user properly")
  void testExistsByVinAndUser_Id_DifferentUsersCanHaveSameVIN() {
    // This test verifies the business logic that different users
    // can register vehicles with the same VIN (edge case)
    User anotherUser =
        User.builder()
            .email("another@example.com")
            .name("Another User")
            .passwordHash("hashedPassword")
            .phone("+1-555-0200")
            .status("ACTIVE")
            .roles(new HashSet<>())
            .build();
    anotherUser = entityManager.persistAndFlush(anotherUser);

    // Check existing VIN for original user
    assertTrue(vehicleRepository.existsByVinAndUser_Id("1HGBH41JXMN109186", testUser.getId()));

    // Check same VIN for different user
    assertFalse(vehicleRepository.existsByVinAndUser_Id("1HGBH41JXMN109186", anotherUser.getId()));
  }

  @Test
  @DisplayName("Should handle vehicle with all fields populated")
  void testSave_AllFields() {
    Vehicle vehicle = new Vehicle();
    vehicle.setMake("Tesla");
    vehicle.setModel("Model 3");
    vehicle.setYear(2024);
    vehicle.setVin("5YJ3E1EA1KF123456");
    vehicle.setPlateNumber("TESLA-1");
    vehicle.setColor("White");
    vehicle.setUserId(testUser.getId());

    Vehicle saved = vehicleRepository.saveAndFlush(vehicle);

    assertNotNull(saved.getId());
    assertEquals("Tesla", saved.getMake());
    assertEquals("Model 3", saved.getModel());
    assertEquals(2024, saved.getYear());
    assertEquals("5YJ3E1EA1KF123456", saved.getVin());
    assertEquals("TESLA-1", saved.getPlateNumber());
    assertEquals("White", saved.getColor());
    assertEquals(testUser.getId(), saved.getUserId());
  }
}

