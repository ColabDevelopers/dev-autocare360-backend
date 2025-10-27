package com.example.autocare360.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.autocare360.entity.Appointment;
import com.example.autocare360.entity.User;
import com.example.autocare360.repository.AppointmentRepository;
import com.example.autocare360.repository.UserRepository;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "http://localhost:3000") // Allow frontend requests
public class AppointmentController {

    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private UserRepository userRepository;

    // Get all appointments
    @GetMapping
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        List<Appointment> appointments = appointmentRepository.findAll();
        return ResponseEntity.ok(appointments);
    }

    // Get appointments by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Appointment>> getAppointmentsByUserId(@PathVariable Long userId) {
        List<Appointment> appointments = appointmentRepository.findByUser_IdOrderByDateAscTimeAsc(userId);
        return ResponseEntity.ok(appointments);
    }

    // Get appointment by ID
    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable Long id) {
        Optional<Appointment> appointment = appointmentRepository.findById(id);
        if (appointment.isPresent()) {
            return ResponseEntity.ok(appointment.get());
        }
        return ResponseEntity.notFound().build();
    }

    // Create new appointment
    @PostMapping
    public ResponseEntity<?> createAppointment(@RequestBody AppointmentRequest request) {
        try {
            // Validate user exists
            Optional<User> user = userRepository.findById(request.getUserId());
            if (!user.isPresent()) {
                return ResponseEntity.badRequest().body("User not found");
            }

            // Check if time slot is available for the selected technician.
            String reqTech = request.getTechnician();
            if (reqTech != null && !reqTech.trim().isEmpty()) {
                List<Appointment> techConflicts = appointmentRepository.findByDateAndTimeAndTechnicianAndStatusNotCancelled(request.getDate(), request.getTime(), reqTech);
                if (!techConflicts.isEmpty()) {
                    return ResponseEntity.badRequest().body("Selected technician is already booked for this time slot");
                }
            } else {
                // No technician selected: keep previous behavior and block slot if any appointment exists
                List<Appointment> conflictingAppointments = appointmentRepository.findByDateAndTimeAndStatusNotCancelled(request.getDate(), request.getTime());
                if (!conflictingAppointments.isEmpty()) {
                    return ResponseEntity.badRequest().body("Time slot is already booked");
                }
            }

            // Create appointment
            Appointment appointment = new Appointment();
            appointment.setUser(user.get()); // Set the actual User object
            appointment.setService(request.getService());
            appointment.setVehicle(request.getVehicle());
            appointment.setDate(request.getDate());
            appointment.setTime(request.getTime());
            appointment.setStatus("PENDING");
            appointment.setNotes(request.getNotes());
            // Set technician if provided
            appointment.setTechnician(request.getTechnician());

            Appointment savedAppointment = appointmentRepository.save(appointment);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedAppointment);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error creating appointment: " + e.getMessage());
        }
    }

    // Update appointment
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAppointment(@PathVariable Long id, @RequestBody AppointmentRequest request) {
        try {
            Optional<Appointment> existingAppointment = appointmentRepository.findById(id);
            if (!existingAppointment.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Appointment appointment = existingAppointment.get();
            
            // Check for time/technician conflicts if date/time/technician is being changed
            boolean dateChanged = !appointment.getDate().equals(request.getDate());
            boolean timeChanged = !appointment.getTime().equals(request.getTime());
            boolean techChanged = (appointment.getTechnician() == null && request.getTechnician() != null)
                                  || (appointment.getTechnician() != null && !appointment.getTechnician().equalsIgnoreCase(request.getTechnician()));

            if (dateChanged || timeChanged || techChanged) {
                String reqTech = request.getTechnician();
                if (reqTech != null && !reqTech.trim().isEmpty()) {
                    List<Appointment> techConflicts = appointmentRepository.findByDateAndTimeAndTechnicianAndStatusNotCancelled(request.getDate(), request.getTime(), reqTech);
                    // Remove current appointment from conflict check
                    techConflicts.removeIf(appt -> appt.getId().equals(id));
                    if (!techConflicts.isEmpty()) {
                        return ResponseEntity.badRequest().body("Selected technician is already booked for this time slot");
                    }
                } else {
                    List<Appointment> conflictingAppointments = appointmentRepository.findByDateAndTimeAndStatusNotCancelled(request.getDate(), request.getTime());
                    conflictingAppointments.removeIf(appt -> appt.getId().equals(id));
                    if (!conflictingAppointments.isEmpty()) {
                        return ResponseEntity.badRequest().body("Time slot is already booked");
                    }
                }
            }

            // Update fields
            appointment.setService(request.getService());
            appointment.setVehicle(request.getVehicle());
            appointment.setDate(request.getDate());
            appointment.setTime(request.getTime());
            appointment.setStatus(request.getStatus());
            appointment.setNotes(request.getNotes());
            appointment.setTechnician(request.getTechnician());

            Appointment updatedAppointment = appointmentRepository.save(appointment);
            return ResponseEntity.ok(updatedAppointment);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating appointment: " + e.getMessage());
        }
    }

    // Delete appointment
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAppointment(@PathVariable Long id) {
        try {
            if (!appointmentRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            
            appointmentRepository.deleteById(id);
            return ResponseEntity.ok().body("Appointment deleted successfully");
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error deleting appointment: " + e.getMessage());
        }
    }

    // Get appointments by date range
    @GetMapping("/date-range")
    public ResponseEntity<List<Appointment>> getAppointmentsByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        
        List<Appointment> appointments = appointmentRepository.findByDateBetweenOrderByDateAscTimeAsc(startDate, endDate);
        return ResponseEntity.ok(appointments);
    }

    // Get appointments by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Appointment>> getAppointmentsByStatus(@PathVariable String status) {
        List<Appointment> appointments = appointmentRepository.findByStatusOrderByDateAscTimeAsc(status);
        return ResponseEntity.ok(appointments);
    }

    // Inner class for request body
    public static class AppointmentRequest {
        private Long userId;
        private String service;
        private String vehicle;
        private LocalDate date;
        private LocalTime time;
        private String status;
        private String notes;
        private String technician;

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getService() { return service; }
        public void setService(String service) { this.service = service; }

        public String getVehicle() { return vehicle; }
        public void setVehicle(String vehicle) { this.vehicle = vehicle; }

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }

        public LocalTime getTime() { return time; }
        public void setTime(LocalTime time) { this.time = time; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }

        public String getTechnician() { return technician; }
        public void setTechnician(String technician) { this.technician = technician; }
    }
}