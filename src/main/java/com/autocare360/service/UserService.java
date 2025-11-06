package com.autocare360.service;

import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.autocare360.dto.UserResponse;
import com.autocare360.entity.User;
import com.autocare360.repo.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserResponse getCurrent(Long userId) {
    User user = userRepository.findById(userId).orElseThrow();
    boolean isEmployee =
        user.getRoles().stream().anyMatch(r -> r.getName().equalsIgnoreCase("EMPLOYEE"));
    return UserResponse.builder()
        .id(user.getId())
        .email(user.getEmail())
        .name(user.getName())
        .roles(
            user.getRoles().stream()
                .map(r -> r.getName().toLowerCase(Locale.ROOT))
                .collect(Collectors.toList()))
        .status(user.getStatus() == null ? "Active" : user.getStatus())
        .phone(user.getPhone())
        .employeeNo(isEmployee ? user.getEmployeeNo() : null)
        .department(isEmployee ? user.getDepartment() : null)
        .build();
  }

  public void changePassword(Long userId, String currentPassword, String newPassword) {
    User user = userRepository.findById(userId).orElseThrow();
    if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
      throw new IllegalArgumentException("Invalid current password");
    }
    user.setPasswordHash(passwordEncoder.encode(newPassword));
    userRepository.save(user);
  }

  public void updatePhone(Long userId, String phone) {
    User user = userRepository.findById(userId).orElseThrow();
    user.setPhone(phone);
    userRepository.save(user);
  }

  public java.util.List<UserResponse> getAllCustomers() {
    return userRepository.findAll().stream()
        .filter(user -> user.getRoles().stream()
            .anyMatch(role -> role.getName().equalsIgnoreCase("CUSTOMER")))
        .map(user -> UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .roles(user.getRoles().stream()
                .map(r -> r.getName().toLowerCase(Locale.ROOT))
                .collect(Collectors.toList()))
            .status(user.getStatus() == null ? "Active" : user.getStatus())
            .phone(user.getPhone())
            .build())
        .collect(Collectors.toList());
  }
}
