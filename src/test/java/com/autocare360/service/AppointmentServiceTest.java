package com.autocare360.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.autocare360.dto.AppointmentRequest;
import com.autocare360.dto.AppointmentResponse;
import com.autocare360.dto.AvailabilityResponse;
import com.autocare360.entity.Appointment;
import com.autocare360.entity.User;
import com.autocare360.repo.AppointmentRepository;
import com.autocare360.repo.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentService Tests")
class AppointmentServiceTest {

  @Mock private AppointmentRepository appointmentRepository;

  @Mock private UserRepository userRepository;

  @Mock private SimpMessagingTemplate messagingTemplate;

  @InjectMocks private AppointmentService appointmentService;

  private User testUser;
  private User technicianUser;
  private Appointment testAppointment;
  private AppointmentRequest appointmentRequest;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setName("Test User");
    testUser.setEmail("test@example.com");

    technicianUser = new User();
    technicianUser.setId(2L);
    technicianUser.setName("John Technician");
    technicianUser.setEmail("tech@example.com");
    technicianUser.setEmployeeNo("EMP-001");

    testAppointment = new Appointment();
    testAppointment.setId(1L);
    testAppointment.setUser(testUser);
    testAppointment.setService("Oil Change");
    testAppointment.setVehicle("Toyota Camry");
    testAppointment.setDate(LocalDate.of(2024, 12, 25));
    testAppointment.setTime(LocalTime.of(10, 0));
    testAppointment.setStatus("PENDING");
    testAppointment.setNotes("Regular maintenance");
    testAppointment.setTechnician("John Technician");

    appointmentRequest =
        AppointmentRequest.builder()
            .userId(1L)
            .service("Oil Change")
            .vehicle("Toyota Camry")
            .date(LocalDate.of(2024, 12, 25))
            .time(LocalTime.of(10, 0))
            .status("PENDING")
            .notes("Regular maintenance")
            .technician("John Technician")
            .build();
  }

  @Test
  @DisplayName("Should list appointments by user")
  void testListByUser_Success() {
    List<Appointment> appointments = Arrays.asList(testAppointment);
    when(appointmentRepository.findByUser_IdOrderByDateDescTimeDesc(1L))
        .thenReturn(appointments);

    List<AppointmentResponse> responses = appointmentService.listByUser(1L);

    assertNotNull(responses);
    assertEquals(1, responses.size());
    assertEquals("Oil Change", responses.get(0).getService());
    verify(appointmentRepository, times(1)).findByUser_IdOrderByDateDescTimeDesc(1L);
  }

  @Test
  @DisplayName("Should list all appointments")
  void testListAll_Success() {
    List<Appointment> appointments = Arrays.asList(testAppointment);
    when(appointmentRepository.findAllByOrderByDateAscTimeAsc()).thenReturn(appointments);

    List<AppointmentResponse> responses = appointmentService.listAll();

    assertNotNull(responses);
    assertEquals(1, responses.size());
    verify(appointmentRepository, times(1)).findAllByOrderByDateAscTimeAsc();
  }

  @Test
  @DisplayName("Should list appointments by employee and status")
  void testListByEmployeeAndStatus_Success() {
    List<String> statuses = Arrays.asList("PENDING", "IN_PROGRESS");
    testAppointment.setAssignedUser(technicianUser);
    List<Appointment> appointments = Arrays.asList(testAppointment);

    when(appointmentRepository.findByAssignedUser_IdAndStatusInOrderByDateAscTimeAsc(
            2L, statuses))
        .thenReturn(appointments);

    List<AppointmentResponse> responses =
        appointmentService.listByEmployeeAndStatus(2L, statuses);

    assertNotNull(responses);
    assertEquals(1, responses.size());
    verify(appointmentRepository, times(1))
        .findByAssignedUser_IdAndStatusInOrderByDateAscTimeAsc(2L, statuses);
  }

  @Test
  @DisplayName("Should create appointment successfully")
  void testCreate_Success() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.findByName("John Technician")).thenReturn(Optional.of(technicianUser));
    when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);

    AppointmentResponse response = appointmentService.create(appointmentRequest);

    assertNotNull(response);
    assertEquals("Oil Change", response.getService());
    assertEquals("Toyota Camry", response.getVehicle());
    verify(userRepository, times(1)).findById(1L);
    verify(userRepository, times(1)).findByName("John Technician");
    verify(appointmentRepository, times(1)).save(any(Appointment.class));
  }

  @Test
  @DisplayName("Should throw exception when user not found during creation")
  void testCreate_UserNotFound() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    RuntimeException exception =
        assertThrows(
            RuntimeException.class, () -> appointmentService.create(appointmentRequest));

    assertEquals("User not found", exception.getMessage());
    verify(userRepository, times(1)).findById(1L);
    verify(appointmentRepository, never()).save(any(Appointment.class));
  }

  @Test
  @DisplayName("Should create appointment without technician")
  void testCreate_WithoutTechnician() {
    appointmentRequest.setTechnician(null);
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);

    AppointmentResponse response = appointmentService.create(appointmentRequest);

    assertNotNull(response);
    verify(userRepository, never()).findByName(anyString());
    verify(appointmentRepository, times(1)).save(any(Appointment.class));
  }

  @Test
  @DisplayName("Should set default status to PENDING if not provided")
  void testCreate_DefaultStatus() {
    appointmentRequest.setStatus(null);
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(appointmentRepository.save(any(Appointment.class)))
        .thenAnswer(
            invocation -> {
              Appointment savedAppointment = invocation.getArgument(0);
              assertEquals("PENDING", savedAppointment.getStatus());
              return testAppointment;
            });

    appointmentService.create(appointmentRequest);

    verify(appointmentRepository, times(1)).save(any(Appointment.class));
  }

  @Test
  @DisplayName("Should update appointment successfully")
  void testUpdate_Success() {
    when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
    when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);

    AppointmentRequest updateRequest =
        AppointmentRequest.builder()
            .service("Tire Rotation")
            .status("COMPLETED")
            .notes("Updated notes")
            .build();

    AppointmentResponse response = appointmentService.update(1L, updateRequest);

    assertNotNull(response);
    assertEquals("Tire Rotation", testAppointment.getService());
    assertEquals("COMPLETED", testAppointment.getStatus());
    assertEquals("Updated notes", testAppointment.getNotes());
    verify(appointmentRepository, times(1)).findById(1L);
    verify(appointmentRepository, times(1)).save(testAppointment);
  }

  @Test
  @DisplayName("Should throw exception when appointment not found during update")
  void testUpdate_AppointmentNotFound() {
    when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());

    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> appointmentService.update(1L, appointmentRequest));

    assertEquals("Appointment not found", exception.getMessage());
    verify(appointmentRepository, times(1)).findById(1L);
    verify(appointmentRepository, never()).save(any(Appointment.class));
  }

  @Test
  @DisplayName("Should update technician and assigned user")
  void testUpdate_UpdateTechnician() {
    when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
    when(userRepository.findByName("John Technician")).thenReturn(Optional.of(technicianUser));
    when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);

    AppointmentRequest updateRequest =
        AppointmentRequest.builder().technician("John Technician").build();

    appointmentService.update(1L, updateRequest);

    assertEquals("John Technician", testAppointment.getTechnician());
    assertEquals(technicianUser, testAppointment.getAssignedUser());
    verify(userRepository, times(1)).findByName("John Technician");
  }

  @Test
  @DisplayName("Should delete appointment successfully")
  void testDelete_Success() {
    doNothing().when(appointmentRepository).deleteById(1L);

    appointmentService.delete(1L);

    verify(appointmentRepository, times(1)).deleteById(1L);
  }

  @Test
  @DisplayName("Should get availability for specific technician")
  void testGetAvailability_SpecificTechnician() {
    LocalDate date = LocalDate.of(2024, 12, 25);
    String technician = "John Technician";

    List<Appointment> bookedAppointments = Arrays.asList(testAppointment);
    when(appointmentRepository.findByDateAndTechnicianAndStatusNot(date, technician, "CANCELLED"))
        .thenReturn(bookedAppointments);

    AvailabilityResponse response = appointmentService.getAvailability(date, technician);

    assertNotNull(response);
    assertNotNull(response.getTimeSlots());
    assertTrue(response.getTimeSlots().size() < 24); // Some slots should be booked
    assertEquals(1, response.getAvailableTechnicians().size());
    assertEquals(technician, response.getAvailableTechnicians().get(0));
    verify(appointmentRepository, times(1))
        .findByDateAndTechnicianAndStatusNot(date, technician, "CANCELLED");
  }

  @Test
  @DisplayName("Should get availability for all technicians")
  void testGetAvailability_AllTechnicians() {
    LocalDate date = LocalDate.of(2024, 12, 25);
    List<Appointment> allAppointments = Arrays.asList(testAppointment);
    List<User> employees = Arrays.asList(technicianUser);

    when(appointmentRepository.findByDateAndTimeAndStatusNot(eq(date), isNull(), eq("CANCELLED")))
        .thenReturn(allAppointments);
    when(userRepository.findAll()).thenReturn(employees);

    AvailabilityResponse response = appointmentService.getAvailability(date, null);

    assertNotNull(response);
    assertNotNull(response.getTimeSlots());
    assertNotNull(response.getAvailableTechnicians());
    assertEquals(1, response.getAvailableTechnicians().size());
    verify(appointmentRepository, times(1))
        .findByDateAndTimeAndStatusNot(eq(date), isNull(), eq("CANCELLED"));
  }

  @Test
  @DisplayName("Should return all time slots when no appointments")
  void testGetAvailability_NoAppointments() {
    LocalDate date = LocalDate.of(2024, 12, 25);
    String technician = "John Technician";

    when(appointmentRepository.findByDateAndTechnicianAndStatusNot(date, technician, "CANCELLED"))
        .thenReturn(Arrays.asList());

    AvailabilityResponse response = appointmentService.getAvailability(date, technician);

    assertNotNull(response);
    assertEquals(24, response.getTimeSlots().size()); // All slots available
  }

  @Test
  @DisplayName("Should broadcast appointment update via WebSocket")
  void testUpdate_BroadcastWebSocket() {
    when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
    when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);
    doNothing()
        .when(messagingTemplate)
        .convertAndSend(anyString(), any(AppointmentResponse.class));
    doNothing()
        .when(messagingTemplate)
        .convertAndSendToUser(anyString(), anyString(), any(AppointmentResponse.class));

    appointmentService.update(1L, appointmentRequest);

    verify(messagingTemplate, times(1))
        .convertAndSend(eq("/topic/admin-appointments"), any(AppointmentResponse.class));
    verify(messagingTemplate, times(1))
        .convertAndSendToUser(
            eq("1"), eq("/queue/appointment-updates"), any(AppointmentResponse.class));
  }
}

