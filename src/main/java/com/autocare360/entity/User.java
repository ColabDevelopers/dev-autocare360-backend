package com.autocare360.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 255)
  private String email;

  @Column(nullable = false, length = 255)
  private String passwordHash;

  @Column(nullable = false, length = 255)
  private String name;

  @Column(length = 50)
  private String phone;

  @Column(name = "employee_no", unique = true, length = 20)
  private String employeeNo; // for employees only

  @Column(length = 100)
  private String department; // for employees only

  @Column(length = 50)
  private String status; // ACTIVE | INACTIVE

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "users_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  @Builder.Default
  private Set<Role> roles = new HashSet<>();

  @PrePersist
  void onCreate() {
    Instant now = Instant.now();
    this.createdAt = now;
    this.updatedAt = now;
    if (this.status == null) this.status = "ACTIVE";
  }

  @PreUpdate
  void onUpdate() {
    this.updatedAt = Instant.now();
  }
}
