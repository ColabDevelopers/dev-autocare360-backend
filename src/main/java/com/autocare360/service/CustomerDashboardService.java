package com.autocare360.service;

import com.autocare360.dto.CustomerDashboardDTO;
import com.autocare360.dto.CustomerDashboardDTO.NextServiceDTO;
import com.autocare360.dto.VehicleDTO;
import com.autocare360.entity.Appointment;
import com.autocare360.entity.Vehicle;
import com.autocare360.repo.AppointmentRepository;
import com.autocare360.repo.VehicleRepository;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerDashboardService {

  private final AppointmentRepository appointmentRepository;
  private final VehicleRepository vehicleRepository;

  public CustomerDashboardDTO getDashboardData(Long userId) {
    // Count active services (PENDING, IN_PROGRESS)
    List<String> activeStatuses = Arrays.asList("PENDING", "IN_PROGRESS", "SCHEDULED");
    int activeServices =
        (int)
            appointmentRepository.findByUser_Id(userId).stream()
                .filter(a -> activeStatuses.contains(a.getStatus()))
                .count();

    // Count completed services
    int completedServices =
        (int)
            appointmentRepository.findByUser_Id(userId).stream()
                .filter(a -> "COMPLETED".equals(a.getStatus()))
                .count();

    // Count total vehicles
    int totalVehicles = (int) vehicleRepository.countByUser_Id(userId);

    // Get next upcoming service
    NextServiceDTO nextService = getNextService(userId);

    return CustomerDashboardDTO.builder()
        .activeServices(activeServices)
        .completedServices(completedServices)
        .totalVehicles(totalVehicles)
        .nextService(nextService)
        .build();
  }

  public List<VehicleDTO> getCustomerVehicles(Long userId) {
    List<Vehicle> vehicles = vehicleRepository.findByUser_Id(userId);
    return vehicles.stream()
        .map(
            v ->
                VehicleDTO.builder()
                    .id(v.getId())
                    .vin(v.getVin())
                    .make(v.getMake())
                    .model(v.getModel())
                    .year(v.getYear())
                    .plateNumber(v.getPlateNumber())
                    .color(v.getColor())
                    .build())
        .collect(Collectors.toList());
  }

  private NextServiceDTO getNextService(Long userId) {
    List<Appointment> appointments = appointmentRepository.findByUser_Id(userId);

    // Find next upcoming appointment
    Appointment next =
        appointments.stream()
            .filter(
                a ->
                    a.getDate() != null
                        && (a.getDate().isAfter(LocalDate.now())
                            || a.getDate().equals(LocalDate.now())))
            .filter(a -> !"COMPLETED".equals(a.getStatus()) && !"CANCELLED".equals(a.getStatus()))
            .min(
                (a1, a2) -> {
                  int dateCompare = a1.getDate().compareTo(a2.getDate());
                  if (dateCompare != 0) return dateCompare;
                  if (a1.getTime() != null && a2.getTime() != null) {
                    return a1.getTime().compareTo(a2.getTime());
                  }
                  return 0;
                })
            .orElse(null);

    if (next == null) {
      return null;
    }

    return NextServiceDTO.builder()
        .id(next.getId())
        .service(next.getService())
        .vehicle(next.getVehicle())
        .date(next.getDate())
        .time(next.getTime())
        .status(next.getStatus())
        .notes(next.getNotes())
        .build();
  }
}
