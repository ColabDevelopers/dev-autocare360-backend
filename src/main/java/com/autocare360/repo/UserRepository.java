package com.autocare360.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autocare360.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);
	Optional<User> findByName(String name);
	java.util.List<User> findDistinctByRoles_Name(String name);
}


