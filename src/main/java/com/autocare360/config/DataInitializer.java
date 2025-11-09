package com.autocare360.config;

import com.autocare360.entity.Employee;
import com.autocare360.entity.User;
import com.autocare360.repo.EmployeeRepository;
import com.autocare360.repo.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

  @Autowired private UserRepository userRepository;

  @Autowired private EmployeeRepository employeeRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Override
  public void run(String... args) throws Exception {
    // Initialize sample users if none exist
    if (userRepository.count() == 0) {
      User user1 = new User();
      user1.setEmail("john.doe@example.com");
      user1.setName("John Doe");
      user1.setPhone("123-456-7890");
      user1.setPasswordHash(passwordEncoder.encode("SecureDefault123!"));
      user1.setStatus("ACTIVE");
      user1.setCreatedAt(Instant.now());
      user1.setUpdatedAt(Instant.now());

      User user2 = new User();
      user2.setEmail("jane.smith@example.com");
      user2.setName("Jane Smith");
      user2.setPhone("098-765-4321");
      user2.setPasswordHash(passwordEncoder.encode("SecureDefault123!"));
      user2.setStatus("ACTIVE");
      user2.setCreatedAt(Instant.now());
      user2.setUpdatedAt(Instant.now());

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
