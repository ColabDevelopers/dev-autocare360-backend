package com.autocare360.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.autocare360.entity.ServiceRecord;
import com.autocare360.repo.ServiceRecordRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
@DisplayName("ServiceRecordService Tests")
class ServiceRecordServiceTest {

  @Mock private ServiceRecordRepository repo;

  @InjectMocks private ServiceRecordService serviceRecordService;

  private ServiceRecord testRecord;

  @BeforeEach
  void setUp() {
    testRecord = new ServiceRecord();
    testRecord.setId(1L);
    testRecord.setVehicleId(100L);
    testRecord.setName("Oil Change");
    testRecord.setType("MAINTENANCE");
    testRecord.setStatus("PENDING");
    testRecord.setScheduledAt(LocalDateTime.now().plusDays(1));
    testRecord.setNotes("Regular oil change");
    testRecord.setPrice(49.99);
    testRecord.setDuration(60.0);
    testRecord.setRequestedAt(LocalDateTime.now());
  }

  @Test
  @DisplayName("Should list all service records")
  void testListAll_Success() {
    ServiceRecord record2 = new ServiceRecord();
    record2.setId(2L);
    record2.setName("Tire Rotation");

    List<ServiceRecord> records = Arrays.asList(testRecord, record2);
    when(repo.findAll()).thenReturn(records);

    List<ServiceRecord> result = serviceRecordService.listAll();

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("Oil Change", result.get(0).getName());
    assertEquals("Tire Rotation", result.get(1).getName());
    verify(repo, times(1)).findAll();
  }

  @Test
  @DisplayName("Should return empty list when no records")
  void testListAll_EmptyList() {
    when(repo.findAll()).thenReturn(Arrays.asList());

    List<ServiceRecord> result = serviceRecordService.listAll();

    assertNotNull(result);
    assertEquals(0, result.size());
    verify(repo, times(1)).findAll();
  }

  @Test
  @DisplayName("Should list service records for vehicle")
  void testListForVehicle_Success() {
    ServiceRecord record2 = new ServiceRecord();
    record2.setId(2L);
    record2.setVehicleId(100L);
    record2.setName("Tire Rotation");

    List<ServiceRecord> records = Arrays.asList(testRecord, record2);
    when(repo.findByVehicleIdOrderByRequestedAtDesc(100L)).thenReturn(records);

    List<ServiceRecord> result = serviceRecordService.listForVehicle(100L);

    assertNotNull(result);
    assertEquals(2, result.size());
    verify(repo, times(1)).findByVehicleIdOrderByRequestedAtDesc(100L);
  }

  @Test
  @DisplayName("Should return empty list for vehicle with no records")
  void testListForVehicle_EmptyList() {
    when(repo.findByVehicleIdOrderByRequestedAtDesc(999L)).thenReturn(Arrays.asList());

    List<ServiceRecord> result = serviceRecordService.listForVehicle(999L);

    assertNotNull(result);
    assertEquals(0, result.size());
    verify(repo, times(1)).findByVehicleIdOrderByRequestedAtDesc(999L);
  }

  @Test
  @DisplayName("Should list service records by status")
  void testListByStatus_Success() {
    ServiceRecord record2 = new ServiceRecord();
    record2.setId(2L);
    record2.setStatus("PENDING");

    List<ServiceRecord> records = Arrays.asList(testRecord, record2);
    when(repo.findByStatus("PENDING")).thenReturn(records);

    List<ServiceRecord> result = serviceRecordService.listByStatus("PENDING");

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("PENDING", result.get(0).getStatus());
    verify(repo, times(1)).findByStatus("PENDING");
  }

  @Test
  @DisplayName("Should get service record by id")
  void testGet_Success() {
    when(repo.findById(1L)).thenReturn(Optional.of(testRecord));

    ServiceRecord result = serviceRecordService.get(1L);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("Oil Change", result.getName());
    verify(repo, times(1)).findById(1L);
  }

  @Test
  @DisplayName("Should return null when service record not found")
  void testGet_NotFound() {
    when(repo.findById(999L)).thenReturn(Optional.empty());

    ServiceRecord result = serviceRecordService.get(999L);

    assertNull(result);
    verify(repo, times(1)).findById(999L);
  }

  @Test
  @DisplayName("Should create service record successfully")
  void testCreate_Success() {
    when(repo.save(testRecord)).thenReturn(testRecord);

    ServiceRecord result = serviceRecordService.create(testRecord);

    assertNotNull(result);
    assertEquals("Oil Change", result.getName());
    assertEquals("MAINTENANCE", result.getType());
    verify(repo, times(1)).save(testRecord);
  }

  @Test
  @DisplayName("Should update service record successfully")
  void testUpdate_Success() {
    ServiceRecord patch = new ServiceRecord();
    patch.setName("Premium Oil Change");
    patch.setStatus("IN_PROGRESS");
    patch.setPrice(79.99);

    when(repo.findById(1L)).thenReturn(Optional.of(testRecord));
    when(repo.save(any(ServiceRecord.class))).thenReturn(testRecord);

    ServiceRecord result = serviceRecordService.update(1L, patch);

    assertNotNull(result);
    assertEquals("Premium Oil Change", testRecord.getName());
    assertEquals("IN_PROGRESS", testRecord.getStatus());
    assertEquals(79.99, testRecord.getPrice(), 0.001);
    verify(repo, times(1)).findById(1L);
    verify(repo, times(1)).save(testRecord);
  }

  @Test
  @DisplayName("Should return null when updating non-existent record")
  void testUpdate_NotFound() {
    ServiceRecord patch = new ServiceRecord();
    patch.setName("Updated Name");

    when(repo.findById(999L)).thenReturn(Optional.empty());

    ServiceRecord result = serviceRecordService.update(999L, patch);

    assertNull(result);
    verify(repo, times(1)).findById(999L);
    verify(repo, never()).save(any(ServiceRecord.class));
  }

  @Test
  @DisplayName("Should update only non-null fields")
  void testUpdate_PartialUpdate() {
    ServiceRecord patch = new ServiceRecord();
    patch.setStatus("COMPLETED");
    // Other fields are null

    when(repo.findById(1L)).thenReturn(Optional.of(testRecord));
    when(repo.save(any(ServiceRecord.class))).thenReturn(testRecord);

    ServiceRecord result = serviceRecordService.update(1L, patch);

    assertNotNull(result);
    assertEquals("COMPLETED", testRecord.getStatus());
    assertEquals("Oil Change", testRecord.getName()); // Should remain unchanged
    assertEquals("MAINTENANCE", testRecord.getType()); // Should remain unchanged
    verify(repo, times(1)).save(testRecord);
  }

  @Test
  @DisplayName("Should update all fields when provided")
  void testUpdate_AllFields() {
    ServiceRecord patch = new ServiceRecord();
    patch.setName("New Name");
    patch.setType("REPAIR");
    patch.setStatus("COMPLETED");
    patch.setScheduledAt(LocalDateTime.now().plusDays(2));
    patch.setNotes("Updated notes");
    patch.setPrice(99.99);
    patch.setDuration(120.0);

    when(repo.findById(1L)).thenReturn(Optional.of(testRecord));
    when(repo.save(any(ServiceRecord.class))).thenReturn(testRecord);

    serviceRecordService.update(1L, patch);

    assertEquals("New Name", testRecord.getName());
    assertEquals("REPAIR", testRecord.getType());
    assertEquals("COMPLETED", testRecord.getStatus());
    assertEquals("Updated notes", testRecord.getNotes());
    assertEquals(99.99, testRecord.getPrice(), 0.001);
    assertEquals(120.0, testRecord.getDuration(), 0.001);
    verify(repo, times(1)).save(testRecord);
  }

  @Test
  @DisplayName("Should delete service record successfully")
  void testDelete_Success() {
    when(repo.findById(1L)).thenReturn(Optional.of(testRecord));
    doNothing().when(repo).delete(testRecord);

    boolean result = serviceRecordService.delete(1L);

    assertTrue(result);
    verify(repo, times(1)).findById(1L);
    verify(repo, times(1)).delete(testRecord);
  }

  @Test
  @DisplayName("Should return false when deleting non-existent record")
  void testDelete_NotFound() {
    when(repo.findById(999L)).thenReturn(Optional.empty());

    boolean result = serviceRecordService.delete(999L);

    assertFalse(result);
    verify(repo, times(1)).findById(999L);
    verify(repo, never()).delete(any(ServiceRecord.class));
  }
}

