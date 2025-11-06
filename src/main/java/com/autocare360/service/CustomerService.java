package com.autocare360.service;

import com.autocare360.dto.UserResponse;
import com.autocare360.entity.User;
import com.autocare360.repo.UserRepository;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {

  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public List<UserResponse> listCustomers() {
    return userRepository.findDistinctByRoles_Name("CUSTOMER").stream()
        .map(this::toUserResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public UserResponse getCustomer(Long id) {
    User u = userRepository.findById(id).orElseThrow();
    return toUserResponse(u);
  }

  @Transactional
  public UserResponse updateCustomer(Long id, String name, String phone, String status) {
    User u = userRepository.findById(id).orElseThrow();
    if (name != null) u.setName(name);
    if (phone != null) u.setPhone(phone);
    if (status != null) u.setStatus(status);
    return toUserResponse(userRepository.save(u));
  }

  @Transactional
  public void deleteCustomer(Long id) {
    User u = userRepository.findById(id).orElseThrow();
    userRepository.delete(u);
  }

  private UserResponse toUserResponse(User user) {
    return UserResponse.builder()
        .id(user.getId())
        .email(user.getEmail())
        .name(user.getName())
        .roles(
            user.getRoles().stream()
                .map(r -> r.getName().toLowerCase(Locale.ROOT))
                .collect(Collectors.toList()))
        .status(user.getStatus())
        .phone(user.getPhone())
        .build();
  }
}
