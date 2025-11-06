package com.autocare360.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autocare360.dto.AppointmentRequest;
import com.autocare360.dto.AppointmentResponse;
import com.autocare360.dto.AvailabilityResponse;
import com.autocare360.entity.Appointment;
import com.autocare360.entity.User;
import com.autocare360.repo.AppointmentRepository;
import com.autocare360.repo.EmployeeRepository;
import com.autocare360.repo.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentService {

	private final AppointmentRepository appointmentRepository;
	private final UserRepository userRepository;
	private final EmployeeRepository employeeRepository;
	private final SimpMessagingTemplate messagingTemplate;

	@Transactional(readOnly = true)
	public List<AppointmentResponse> listByUser(Long userId) {
		List<Appointment> appointments = appointmentRepository.findByUser_IdOrderByDateDescTimeDesc(userId);
		return appointments.stream()
				.map(this::toResponse)
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<AppointmentResponse> listAll() {
		List<Appointment> appointments = appointmentRepository.findAll();
		return appointments.stream()
				.map(this::toResponse)
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<AppointmentResponse> listByEmployeeAndStatus(Long employeeId, List<String> statuses) {
		List<Appointment> appointments = appointmentRepository
				.findByAssignedEmployee_IdAndStatusInOrderByDateAscTimeAsc(employeeId, statuses);
		return appointments.stream()
				.map(this::toResponse)
				.collect(Collectors.toList());
	}

	@Transactional
	public AppointmentResponse create(AppointmentRequest request) {
		User user = userRepository.findById(request.getUserId())
				.orElseThrow(() -> new RuntimeException("User not found"));

		Appointment appointment = new Appointment();
		appointment.setUser(user);
		appointment.setService(request.getService());
		appointment.setVehicle(request.getVehicle());
		appointment.setDate(request.getDate());
		appointment.setTime(request.getTime());
		appointment.setStatus(request.getStatus() != null ? request.getStatus() : "PENDING");
		appointment.setNotes(request.getNotes());
		appointment.setTechnician(request.getTechnician());

		// If technician is specified, try to find and assign employee from Employee table
		if (request.getTechnician() != null && !request.getTechnician().isEmpty()) {
			final Appointment appt = appointment; // Make final for lambda
			employeeRepository.findByName(request.getTechnician()).ifPresent(employee -> {
				appt.setAssignedEmployee(employee);
			});
		}

		appointment = appointmentRepository.save(appointment);

		// Broadcast new appointment to admin dashboard listeners
		try {
			AppointmentResponse resp = toResponse(appointment);
			messagingTemplate.convertAndSend("/topic/admin-appointments", resp);
		} catch (Exception e) {
			// Do not fail the request if messaging fails - just log
			// (logging framework available via Lombok? using System.err as fallback)
			System.err.println("Failed to broadcast appointment to admins: " + e.getMessage());
		}

		return toResponse(appointment);
	}

	@Transactional
	public AppointmentResponse update(Long id, AppointmentRequest request) {
		Appointment appointment = appointmentRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Appointment not found"));

		if (request.getService() != null) appointment.setService(request.getService());
		if (request.getVehicle() != null) appointment.setVehicle(request.getVehicle());
		if (request.getDate() != null) appointment.setDate(request.getDate());
		if (request.getTime() != null) appointment.setTime(request.getTime());
		if (request.getStatus() != null) appointment.setStatus(request.getStatus());
		if (request.getNotes() != null) appointment.setNotes(request.getNotes());
		if (request.getTechnician() != null) {
			appointment.setTechnician(request.getTechnician());
			// Update assigned employee if technician name is provided from Employee table
			final Appointment appt = appointment; // Make final for lambda
			employeeRepository.findByName(request.getTechnician()).ifPresent(employee -> {
				appt.setAssignedEmployee(employee);
			});
		}

		appointment = appointmentRepository.save(appointment);
		AppointmentResponse response = toResponse(appointment);
		
		// Broadcast status update to admin dashboard
		messagingTemplate.convertAndSend("/topic/admin-appointments", response);
		System.out.println("Broadcasted to admin topic: " + response.getId() + " - " + response.getStatus());
		
		// Broadcast to the specific customer if user exists
		if (appointment.getUser() != null) {
			String userId = appointment.getUser().getId().toString();
			messagingTemplate.convertAndSendToUser(
				userId,
				"/queue/appointment-updates",
				response
			);
			System.out.println("Broadcasted to user " + userId + " appointment: " + response.getId() + " - " + response.getStatus());
		} else {
			System.out.println("No user associated with appointment " + response.getId());
		}
		
		return response;
	}

	@Transactional
	public void delete(Long id) {
		appointmentRepository.deleteById(id);
	}

	@Transactional(readOnly = true)
	public AvailabilityResponse getAvailability(LocalDate date, String technician) {
		AvailabilityResponse response = new AvailabilityResponse();

		// Define working hours (9 AM to 5 PM with 1-hour slots)
		List<String> allTimeSlots = generateTimeSlots();

		if (technician != null && !technician.isEmpty()) {
			// Get booked times for this specific technician
			List<Appointment> bookedAppointments = appointmentRepository
					.findByDateAndTechnicianAndStatusNot(date, technician, "CANCELLED");
			
			List<String> bookedTimes = bookedAppointments.stream()
					.map(a -> a.getTime().toString().substring(0, 5))
					.collect(Collectors.toList());

			// Filter out booked times
			List<String> availableSlots = allTimeSlots.stream()
					.filter(slot -> !bookedTimes.contains(slot))
					.collect(Collectors.toList());

			response.setTimeSlots(availableSlots);
			response.setAvailableTechnicians(List.of(technician));
		} else {
			// Get all appointments for this date
			List<Appointment> allAppointments = appointmentRepository
					.findByDateAndTimeAndStatusNot(date, null, "CANCELLED");

			// Get all users with employee_no (employees)
			List<User> allEmployees = userRepository.findAll().stream()
					.filter(u -> u.getEmployeeNo() != null && !u.getEmployeeNo().isEmpty())
					.collect(Collectors.toList());
			List<String> allTechnicianNames = allEmployees.stream()
					.map(User::getName)
					.collect(Collectors.toList());

			// For each time slot, find available technicians
			List<String> availableSlots = new ArrayList<>();
			for (String timeSlot : allTimeSlots) {
				LocalTime slotTime = LocalTime.parse(timeSlot + ":00");
				List<String> busyTechnicians = allAppointments.stream()
						.filter(a -> a.getTime().equals(slotTime))
						.map(Appointment::getTechnician)
						.filter(t -> t != null && !t.isEmpty())
						.collect(Collectors.toList());

				// If there are available technicians for this slot, include it
				if (busyTechnicians.size() < allTechnicianNames.size()) {
					availableSlots.add(timeSlot);
				}
			}

			response.setTimeSlots(availableSlots);
			response.setAvailableTechnicians(allTechnicianNames);
		}

		return response;
	}

	private List<String> generateTimeSlots() {
		List<String> slots = new ArrayList<>();
		// Generate slots from 00:00 to 23:00 (24 hours) with 1-hour gap
		for (int hour = 0; hour < 24; hour++) {
			slots.add(String.format("%02d:00", hour));
		}
		return slots;
	}

	private AppointmentResponse toResponse(Appointment appointment) {
		AppointmentResponse response = new AppointmentResponse();
		response.setId(appointment.getId());
		response.setService(appointment.getService());
		response.setVehicle(appointment.getVehicle());
		response.setDate(appointment.getDate());
		response.setTime(appointment.getTime());
		response.setStatus(appointment.getStatus());
		response.setNotes(appointment.getNotes());
		response.setTechnician(appointment.getTechnician());

		if (appointment.getUser() != null) {
			AppointmentResponse.UserInfo userInfo = new AppointmentResponse.UserInfo();
			userInfo.setId(appointment.getUser().getId());
			userInfo.setName(appointment.getUser().getName());
			userInfo.setEmail(appointment.getUser().getEmail());
			response.setUser(userInfo);
		}

		return response;
	}
}
