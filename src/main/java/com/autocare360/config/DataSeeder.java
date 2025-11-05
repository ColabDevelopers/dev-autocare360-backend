package com.autocare360.config;

import com.autocare360.entity.Role;
import com.autocare360.entity.User;
import com.autocare360.repo.RoleRepository;
import com.autocare360.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Order(0)
@Profile("!test")
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

	private final RoleRepository roleRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public void run(ApplicationArguments args) {
		ensureRoles();
		ensureAdmin();
	}

	private void ensureRoles() {
		ensureRole("CUSTOMER");
		ensureRole("EMPLOYEE");
		ensureRole("ADMIN");
	}

	private void ensureRole(String name) {
		roleRepository.findByName(name).orElseGet(() -> roleRepository.save(Role.builder().name(name).build()));
	}

	private void ensureAdmin() {
		final String adminEmail = "nimal.admin@gmail.com";
		User admin = userRepository.findByEmail(adminEmail).orElse(null);
		if (admin == null) {
			Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
			admin = User.builder()
					.email(adminEmail)
					.passwordHash(passwordEncoder.encode("password"))
					.name("System Admin")
					.phone("+1-555-000-0000")
					.status("ACTIVE")
					.build();
			admin.getRoles().add(adminRole);
			userRepository.save(admin);
		}
	}
}


