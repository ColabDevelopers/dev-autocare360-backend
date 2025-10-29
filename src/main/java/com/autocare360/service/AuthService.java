package com.autocare360.service;

import com.autocare360.dto.*;
import com.autocare360.entity.Role;
import com.autocare360.entity.User;
import com.autocare360.repo.RoleRepository;
import com.autocare360.repo.UserRepository;
import com.autocare360.security.JwtService;
import com.autocare360.exception.ConflictException;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(u -> { throw new ConflictException("Email already in use"); });
        Role role = roleRepository.findByName("CUSTOMER").orElseGet(() -> roleRepository.save(Role.builder().name("CUSTOMER").build()));
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhone())
                .build();
        user.getRoles().add(role);
        User saved = userRepository.save(user);
        return toUserResponse(saved);
    }

	public AuthResponse login(LoginRequest request) {
        // Hard-coded admin fallback: simple and reliable
        if ("nimal.admin@gmail.com".equalsIgnoreCase(request.getEmail())
                && "password".equals(request.getPassword())) {
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseGet(() -> roleRepository.save(Role.builder().name("ADMIN").build()));
            User admin = userRepository.findByEmail("nimal.admin@gmail.com").orElse(null);
            if (admin == null) {
                admin = User.builder()
                        .email("nimal.admin@gmail.com")
                        .passwordHash(passwordEncoder.encode("password"))
                        .name("System Admin")
                        .phone("+1-555-000-0000")
                        .status("ACTIVE")
                        .build();
                admin.getRoles().add(adminRole);
                admin = userRepository.save(admin);
            } else {
                // Ensure ADMIN role and known password
                if (admin.getRoles().stream().noneMatch(r -> "ADMIN".equals(r.getName()))) {
                    admin.getRoles().add(adminRole);
                }
                admin.setPasswordHash(passwordEncoder.encode("password"));
                admin = userRepository.save(admin);
            }
            String[] roles = admin.getRoles().stream().map(r -> r.getName().toLowerCase(java.util.Locale.ROOT)).toArray(String[]::new);
            String token = jwtService.generateToken(String.valueOf(admin.getId()), admin.getEmail(), roles);
            return AuthResponse.builder()
                    .accessToken(token)
                    .expiresIn(3600)
                    .user(toUserResponse(admin))
                    .build();
        }

		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
		if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
			throw new IllegalArgumentException("Invalid credentials");
		}
		String[] roles = user.getRoles().stream().map(r -> r.getName().toLowerCase(Locale.ROOT)).toArray(String[]::new);
		String token = jwtService.generateToken(String.valueOf(user.getId()), user.getEmail(), roles);
		return AuthResponse.builder()
				.accessToken(token)
				.expiresIn(3600)
				.user(toUserResponse(user))
				.build();
	}

	public UserResponse me(Long userId) {
		User user = userRepository.findById(userId).orElseThrow();
		return toUserResponse(user);
	}

    // Role inference removed; registration always creates CUSTOMER

	private UserResponse toUserResponse(User user) {
		List<String> roleNames = new ArrayList<>();
		for (Role r : user.getRoles()) {
			roleNames.add(r.getName().toLowerCase(Locale.ROOT));
		}
		return UserResponse.builder()
				.id(user.getId())
				.email(user.getEmail())
				.name(user.getName())
				.roles(roleNames)
				.status(user.getStatus() == null ? "Active" : capitalize(user.getStatus()))
				.build();
	}

	private String capitalize(String s) {
		if (s == null || s.isEmpty()) return s;
		return s.substring(0,1).toUpperCase(Locale.ROOT) + s.substring(1).toLowerCase(Locale.ROOT);
	}
}


