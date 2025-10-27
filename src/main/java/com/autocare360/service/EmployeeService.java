package com.autocare360.service;

import com.autocare360.dto.CreateEmployeeRequest;
import com.autocare360.dto.EmployeeResponse;
import com.autocare360.dto.UpdateEmployeeRequest;
import com.autocare360.entity.Role;
import com.autocare360.entity.User;
import com.autocare360.exception.ConflictException;
import com.autocare360.repo.RoleRepository;
import com.autocare360.repo.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployeeService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public EmployeeResponse create(CreateEmployeeRequest request) {
		userRepository.findByEmail(request.getEmail()).ifPresent(u -> { throw new ConflictException("Email already in use"); });
		Role employeeRole = roleRepository.findByName("EMPLOYEE").orElseGet(() -> roleRepository.save(Role.builder().name("EMPLOYEE").build()));
		User user = User.builder()
				.email(request.getEmail())
				.passwordHash(passwordEncoder.encode("password"))
				.name(request.getName())
				.department(request.getDepartment())
				.employeeNo(generateEmployeeNo())
				.status("ACTIVE")
				.build();
		user.getRoles().add(employeeRole);
		User saved = userRepository.save(user);
		return toResponse(saved);
	}

	@Transactional
	public EmployeeResponse update(Long id, UpdateEmployeeRequest request) {
		User user = userRepository.findById(id).orElseThrow();
		user.setName(request.getName());
		user.setDepartment(request.getDepartment());
		user.setStatus(request.getStatus());
		return toResponse(userRepository.save(user));
	}

	@Transactional
	public EmployeeResponse resetPassword(Long id) {
		User user = userRepository.findById(id).orElseThrow();
		user.setPasswordHash(passwordEncoder.encode("password"));
		return toResponse(userRepository.save(user));
	}

	@Transactional
	public List<EmployeeResponse> list() {
		return userRepository.findDistinctByRoles_Name("EMPLOYEE").stream()
				.map(this::toResponse)
				.collect(Collectors.toList());
	}

	@Transactional
	public EmployeeResponse get(Long id) {
		User user = userRepository.findById(id).orElseThrow();
		return toResponse(user);
	}

	@Transactional
	public void delete(Long id) {
		User user = userRepository.findById(id).orElseThrow();
		userRepository.delete(user);
	}

	private String generateEmployeeNo() {
		long count = userRepository.count();
		return String.format(Locale.ROOT, "EMP-%04d", count + 1);
	}

	private EmployeeResponse toResponse(User u) {
		return EmployeeResponse.builder()
				.id(u.getId())
				.email(u.getEmail())
				.name(u.getName())
				.employeeNo(u.getEmployeeNo())
				.department(u.getDepartment())
				.status(u.getStatus())
				.roles(u.getRoles().stream().map(r -> r.getName().toLowerCase(Locale.ROOT)).collect(Collectors.toList()))
				.createdAt(u.getCreatedAt())
				.build();
	}
}


