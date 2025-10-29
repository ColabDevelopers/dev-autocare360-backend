package com.autocare360.config;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.autocare360.entity.Employee;
import com.autocare360.entity.User;
import com.autocare360.repo.EmployeeRepository;
import com.autocare360.repo.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // Initialize sample users if none exist
        if (userRepository.count() == 0) {
            User user1 = User.builder()
                .email("john.doe@example.com")
                .name("John Doe")
                .phone("123-456-7890")
                .passwordHash("hashedpassword") // dummy
                .status("ACTIVE")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
            User user2 = User.builder()
                .email("jane.smith@example.com")
                .name("Jane Smith")
                .phone("098-765-4321")
                .passwordHash("hashedpassword") // dummy
                .status("ACTIVE")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
            
            userRepository.save(user1);
            userRepository.save(user2);
            
            System.out.println("users table created");
        }
        
        // Initialize sample employee if none exist
        if (employeeRepository.count() == 0) {
            Employee employee1 = new Employee();
            employee1.setName("Mike Johnson");
            employee1.setEmail("mike.johnson@autocare360.com");
            employee1.setPhoneNumber("555-1234");
            employee1.setSpecialization("General Mechanic");
            employee1.setHireDate(LocalDate.of(2023, 1, 15));
            employee1.setStatus("ACTIVE");
            employee1.setCreatedAt(LocalDateTime.now());
            employee1.setUpdatedAt(LocalDateTime.now());
            
            employeeRepository.save(employee1);
            
            System.out.println("employees table created");
        }
    }
}