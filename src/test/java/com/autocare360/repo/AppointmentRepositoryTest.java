package com.autocare360.repo;

import static org.junit.jupiter.api.Assertions.*;

import com.autocare360.entity.Appointment;
import com.autocare360.entity.User;
import java.time.LocalDate;
import java.time.LocalTime;
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
@DisplayName("AppointmentRepository Integration Tests")
class AppointmentRepositoryTest {

  @Autowired private TestEntityManager entityManager;

  @Autowired private AppointmentRepository appointmentRepository;

  private User testUser;
  private Appointment testAppointment;

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

    testAppointment = new Appointment();
    testAppointment.setUser(testUser);
    testAppointment.setService("Oil Change");
    testAppointment.setVehicle("Toyota Camry");
    testAppointment.setDate(LocalDate.of(2024, 12, 25));
    testAppointment.setTime(LocalTime.of(10, 0));
    testAppointment.setStatus("PENDING");
    testAppointment.setNotes("Regular maintenance");
    testAppointment.setTechnician("John Tech");
    testAppointment = entityManager.persistAndFlush(testAppointment);
  }

  @Test
  @DisplayName("Should find appointments by user id ordered by date desc")
  void testFindByUser_IdOrderByDateDescTimeDesc_Success() {
    List<Appointment> appointments =
        appointmentRepository.findByUser_IdOrderByDateDescTimeDesc(testUser.getId());

    assertNotNull(appointments);
    assertEquals(1, appointments.size());
    assertEquals("Oil Change", appointments.get(0).getService());
  }

  @Test
  @DisplayName("Should return empty list when no appointments for user")
  void testFindByUser_IdOrderByDateDescTimeDesc_EmptyList() {
    List<Appointment> appointments = appointmentRepository.findByUser_IdOrderByDateDescTimeDesc(999L);

    assertNotNull(appointments);
    assertEquals(0, appointments.size());
  }

  @Test
  @DisplayName("Should find all appointments ordered by date asc")
  void testFindAllByOrderByDateAscTimeAsc_Success() {
    Appointment appointment2 = new Appointment();
    appointment2.setUser(testUser);
    appointment2.setService("Tire Rotation");
    appointment2.setVehicle("Honda Accord");
    appointment2.setDate(LocalDate.of(2024, 12, 26));
    appointment2.setTime(LocalTime.of(14, 0));
    appointment2.setStatus("PENDING");
    entityManager.persistAndFlush(appointment2);

    List<Appointment> appointments = appointmentRepository.findAllByOrderByDateAscTimeAsc();

    assertNotNull(appointments);
    assertEquals(2, appointments.size());
    // First appointment should be earlier date
    assertTrue(appointments.get(0).getDate().isBefore(appointments.get(1).getDate())
        || appointments.get(0).getDate().equals(appointments.get(1).getDate()));
  }

  @Test
  @DisplayName("Should find appointments by assigned user and status")
  void testFindByAssignedUser_IdAndStatusInOrderByDateAscTimeAsc_Success() {
    User techUser =
        User.builder()
            .email("tech@example.com")
            .name("Technician")
            .passwordHash("hashedPassword")
            .phone("+1-555-0200")
            .employeeNo("EMP-001")
            .status("ACTIVE")
            .roles(new HashSet<>())
            .build();
    techUser = entityManager.persistAndFlush(techUser);

    testAppointment.setAssignedUser(techUser);
    entityManager.persistAndFlush(testAppointment);

    List<String> statuses = List.of("PENDING", "IN_PROGRESS");
    List<Appointment> appointments =
        appointmentRepository.findByAssignedUser_IdAndStatusInOrderByDateAscTimeAsc(
            techUser.getId(), statuses);

    assertNotNull(appointments);
    assertEquals(1, appointments.size());
    assertEquals("Oil Change", appointments.get(0).getService());
  }

  @Test
  @DisplayName("Should find appointments by date and technician excluding cancelled")
  void testFindByDateAndTechnicianAndStatusNot_Success() {
    List<Appointment> appointments =
        appointmentRepository.findByDateAndTechnicianAndStatusNot(
            LocalDate.of(2024, 12, 25), "John Tech", "CANCELLED");

    assertNotNull(appointments);
    assertEquals(1, appointments.size());
    assertEquals("Oil Change", appointments.get(0).getService());
  }

  @Test
  @DisplayName("Should not find cancelled appointments")
  void testFindByDateAndTechnicianAndStatusNot_ExcludeCancelled() {
    testAppointment.setStatus("CANCELLED");
    entityManager.persistAndFlush(testAppointment);

    List<Appointment> appointments =
        appointmentRepository.findByDateAndTechnicianAndStatusNot(
            LocalDate.of(2024, 12, 25), "John Tech", "CANCELLED");

    assertNotNull(appointments);
    assertEquals(0, appointments.size());
  }

  @Test
  @DisplayName("Should find appointments by date excluding cancelled")
  void testFindByDateAndTimeAndStatusNot_Success() {
    List<Appointment> appointments =
        appointmentRepository.findByDateAndTimeAndStatusNot(
            LocalDate.of(2024, 12, 25), null, "CANCELLED");

    assertNotNull(appointments);
    assertEquals(1, appointments.size());
  }

  @Test
  @DisplayName("Should save new appointment")
  void testSave_NewAppointment() {
    Appointment newAppointment = new Appointment();
    newAppointment.setUser(testUser);
    newAppointment.setService("Brake Inspection");
    newAppointment.setVehicle("Ford F-150");
    newAppointment.setDate(LocalDate.of(2024, 12, 27));
    newAppointment.setTime(LocalTime.of(15, 0));
    newAppointment.setStatus("PENDING");

    Appointment saved = appointmentRepository.save(newAppointment);

    assertNotNull(saved.getId());
    assertEquals("Brake Inspection", saved.getService());
    assertNotNull(saved.getCreatedAt());
    assertNotNull(saved.getUpdatedAt());
  }

  @Test
  @DisplayName("Should update existing appointment")
  void testSave_UpdateAppointment() {
    testAppointment.setStatus("COMPLETED");
    testAppointment.setNotes("Service completed successfully");

    Appointment updated = appointmentRepository.save(testAppointment);

    assertEquals("COMPLETED", updated.getStatus());
    assertEquals("Service completed successfully", updated.getNotes());
  }

  @Test
  @DisplayName("Should delete appointment")
  void testDelete_Success() {
    appointmentRepository.delete(testAppointment);
    entityManager.flush();

    Optional<Appointment> found = appointmentRepository.findById(testAppointment.getId());
    assertFalse(found.isPresent());
  }

  @Test
  @DisplayName("Should auto-set default status to PENDING")
  void testSave_DefaultStatus() {
    Appointment newAppointment = new Appointment();
    newAppointment.setUser(testUser);
    newAppointment.setService("Test Service");
    newAppointment.setVehicle("Test Vehicle");
    newAppointment.setDate(LocalDate.now());
    newAppointment.setTime(LocalTime.now());
    // Note: status is not explicitly set

    Appointment saved = appointmentRepository.saveAndFlush(newAppointment);

    assertEquals("PENDING", saved.getStatus());
  }

  @Test
  @DisplayName("Should handle appointments with progress tracking")
  void testSave_WithProgress() {
    testAppointment.setProgress(50);
    testAppointment.setDueDate(LocalDate.of(2024, 12, 26));

    Appointment updated = appointmentRepository.save(testAppointment);

    assertEquals(50, updated.getProgress());
    assertNotNull(updated.getDueDate());
  }

  @Test
  @DisplayName("Should order multiple appointments correctly")
  void testFindAllByOrderByDateAscTimeAsc_Ordering() {
    // Create appointment with earlier date
    Appointment earlier = new Appointment();
    earlier.setUser(testUser);
    earlier.setService("Early Service");
    earlier.setVehicle("Test Vehicle");
    earlier.setDate(LocalDate.of(2024, 12, 24));
    earlier.setTime(LocalTime.of(9, 0));
    earlier.setStatus("PENDING");
    entityManager.persistAndFlush(earlier);

    // Create appointment with later date
    Appointment later = new Appointment();
    later.setUser(testUser);
    later.setService("Late Service");
    later.setVehicle("Test Vehicle");
    later.setDate(LocalDate.of(2024, 12, 26));
    later.setTime(LocalTime.of(11, 0));
    later.setStatus("PENDING");
    entityManager.persistAndFlush(later);

    List<Appointment> appointments = appointmentRepository.findAllByOrderByDateAscTimeAsc();

    assertTrue(appointments.size() >= 3);
    // Verify ordering
    for (int i = 0; i < appointments.size() - 1; i++) {
      Appointment current = appointments.get(i);
      Appointment next = appointments.get(i + 1);
      assertTrue(current.getDate().isBefore(next.getDate())
          || (current.getDate().equals(next.getDate())
              && !current.getTime().isAfter(next.getTime())));
    }
  }
}

