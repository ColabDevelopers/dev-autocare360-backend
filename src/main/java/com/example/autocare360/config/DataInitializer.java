package com.example.autocare360.config;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.autocare360.entity.Employee;
import com.example.autocare360.entity.Schedule;
import com.example.autocare360.entity.User;
import com.example.autocare360.repository.EmployeeRepository;
import com.example.autocare360.repository.ScheduleRepository;
import com.example.autocare360.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ScheduleRepository scheduleRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // Initialize sample users if none exist
        if (userRepository.count() == 0) {
            User user1 = new User("john.doe@example.com", "John Doe", "123-456-7890");
            User user2 = new User("jane.smith@example.com", "Jane Smith", "098-765-4321");
            
            userRepository.save(user1);
            userRepository.save(user2);
            
            System.out.println("users table created");
        }
        
        // Initialize sample employee if none exist
        if (employeeRepository.count() == 0) {
            Employee employee1 = new Employee("Mike Johnson", "mike.johnson@autocare360.com", "555-1234");
            employee1.setSpecialization("General Mechanic");
            employee1.setHireDate(LocalDate.of(2023, 1, 15));
            employee1.setStatus("ACTIVE");
            
            employeeRepository.save(employee1);
            
            System.out.println("employees table created");
        }
        
        // Initialize schedule if none exist
        if (scheduleRepository.count() == 0) {
            // Create schedule for Monday to Friday (9 AM - 5 PM)
            scheduleRepository.save(new Schedule("MONDAY", LocalTime.of(9, 0), LocalTime.of(17, 0), true));
            scheduleRepository.save(new Schedule("TUESDAY", LocalTime.of(9, 0), LocalTime.of(17, 0), true));
            scheduleRepository.save(new Schedule("WEDNESDAY", LocalTime.of(9, 0), LocalTime.of(17, 0), true));
            scheduleRepository.save(new Schedule("THURSDAY", LocalTime.of(9, 0), LocalTime.of(17, 0), true));
            scheduleRepository.save(new Schedule("FRIDAY", LocalTime.of(9, 0), LocalTime.of(17, 0), true));
            
            // Weekend - closed
            scheduleRepository.save(new Schedule("SATURDAY", LocalTime.of(9, 0), LocalTime.of(17, 0), false));
            scheduleRepository.save(new Schedule("SUNDAY", LocalTime.of(9, 0), LocalTime.of(17, 0), false));
            
            System.out.println("schedules table created");
        }
    }
}