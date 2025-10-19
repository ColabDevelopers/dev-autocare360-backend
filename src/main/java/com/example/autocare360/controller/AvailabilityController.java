package com.example.autocare360.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.autocare360.entity.Appointment;
import com.example.autocare360.entity.Schedule;
import com.example.autocare360.repository.AppointmentRepository;
import com.example.autocare360.repository.ScheduleRepository;

@RestController
@RequestMapping("/api/availability")
@CrossOrigin(origins = "http://localhost:3000") // Allow frontend requests
public class AvailabilityController {

    @Autowired
    private ScheduleRepository scheduleRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;

    // Get available time slots for a specific date
    @GetMapping
    public ResponseEntity<AvailabilityResponse> getAvailability(@RequestParam LocalDate date,
                                                                @RequestParam(required = false) String technician) {
        try {
            // Get day of week from date
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            String dayName = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase();
            
            // Check if the day is available in schedule
            Optional<Schedule> schedule = scheduleRepository.findByDayOfWeekAndAvailableTrue(dayName);
            
            if (!schedule.isPresent()) {
                return ResponseEntity.ok(new AvailabilityResponse(false, new ArrayList<>(), "Service not available on " + dayName));
            }
            
            Schedule daySchedule = schedule.get();
            
            // Generate time slots (30-minute intervals)
            List<String> availableSlots = generateTimeSlots(daySchedule.getStartTime(), daySchedule.getEndTime());
            
            // Remove booked slots.
            // If a technician is specified, only remove slots where that technician is already booked.
            // If no technician specified, keep existing behavior and remove any booked slot.
            List<Appointment> bookedAppointments = appointmentRepository.findByDateAndStatusNotCancelledOrderByTime(date);
            for (Appointment appointment : bookedAppointments) {
                String bookedTime = appointment.getTime().toString();
                if (technician == null || technician.trim().isEmpty()) {
                    // No technician selected: mark slot unavailable if any appointment exists
                    availableSlots.remove(bookedTime);
                } else {
                    String aptTech = appointment.getTechnician();
                    if (aptTech != null && aptTech.equalsIgnoreCase(technician)) {
                        // Selected technician already has an appointment at this time
                        availableSlots.remove(bookedTime);
                    }
                }
            }
            
            return ResponseEntity.ok(new AvailabilityResponse(true, availableSlots, "Available slots for " + date));
            
        } catch (Exception e) {
            return ResponseEntity.ok(new AvailabilityResponse(false, new ArrayList<>(), "Error checking availability: " + e.getMessage()));
        }
    }
    
    // Get business hours for a specific day
    @GetMapping("/hours")
    public ResponseEntity<BusinessHoursResponse> getBusinessHours(@RequestParam String dayOfWeek) {
        try {
            Optional<Schedule> schedule = scheduleRepository.findByDayOfWeekAndAvailableTrue(dayOfWeek.toUpperCase());
            
            if (!schedule.isPresent()) {
                return ResponseEntity.ok(new BusinessHoursResponse(false, null, null, "Closed on " + dayOfWeek));
            }
            
            Schedule daySchedule = schedule.get();
            return ResponseEntity.ok(new BusinessHoursResponse(true, daySchedule.getStartTime(), daySchedule.getEndTime(), "Open"));
            
        } catch (Exception e) {
            return ResponseEntity.ok(new BusinessHoursResponse(false, null, null, "Error fetching business hours"));
        }
    }
    
    // Get all available schedules
    @GetMapping("/schedule")
    public ResponseEntity<List<Schedule>> getWeeklySchedule() {
        List<Schedule> schedules = scheduleRepository.findByAvailableTrueOrderByDayOfWeek();
        return ResponseEntity.ok(schedules);
    }
    
    // Helper method to generate time slots
    private List<String> generateTimeSlots(LocalTime startTime, LocalTime endTime) {
        List<String> slots = new ArrayList<>();
        LocalTime current = startTime;
        
        while (current.isBefore(endTime)) {
            slots.add(current.toString());
            current = current.plusMinutes(30); // 30-minute intervals
        }
        
        return slots;
    }
    
    // Response classes
    public static class AvailabilityResponse {
        private boolean available;
        private List<String> timeSlots;
        private String message;
        
        public AvailabilityResponse(boolean available, List<String> timeSlots, String message) {
            this.available = available;
            this.timeSlots = timeSlots;
            this.message = message;
        }
        
        // Getters and Setters
        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
        
        public List<String> getTimeSlots() { return timeSlots; }
        public void setTimeSlots(List<String> timeSlots) { this.timeSlots = timeSlots; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
    
    public static class BusinessHoursResponse {
        private boolean open;
        private LocalTime startTime;
        private LocalTime endTime;
        private String message;
        
        public BusinessHoursResponse(boolean open, LocalTime startTime, LocalTime endTime, String message) {
            this.open = open;
            this.startTime = startTime;
            this.endTime = endTime;
            this.message = message;
        }
        
        // Getters and Setters
        public boolean isOpen() { return open; }
        public void setOpen(boolean open) { this.open = open; }
        
        public LocalTime getStartTime() { return startTime; }
        public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
        
        public LocalTime getEndTime() { return endTime; }
        public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}