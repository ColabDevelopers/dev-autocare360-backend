package com.autocare360.controller;

import com.autocare360.dto.ChangePasswordRequest;
import com.autocare360.dto.UserResponse;
import com.autocare360.security.JwtService;
import com.autocare360.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final JwtService jwtService;

  @GetMapping("/me")
  public ResponseEntity<UserResponse> me(
      @RequestHeader(value = "Authorization", required = false) String authorization) {
    if (authorization == null || !authorization.startsWith("Bearer ")) {
      return ResponseEntity.status(401).build();
    }
    String token = authorization.substring(7);
    if (!jwtService.isTokenValid(token)) {
      return ResponseEntity.status(401).build();
    }
    Long userId = Long.valueOf(jwtService.extractSubject(token));
    return ResponseEntity.ok(userService.getCurrent(userId));
  }

  @org.springframework.web.bind.annotation.PatchMapping("/me")
  public ResponseEntity<Void> updateMe(
      @RequestHeader(value = "Authorization", required = false) String authorization,
      @org.springframework.web.bind.annotation.RequestBody java.util.Map<String, Object> body) {
    if (authorization == null || !authorization.startsWith("Bearer ")) {
      return ResponseEntity.status(401).build();
    }
    String token = authorization.substring(7);
    if (!jwtService.isTokenValid(token)) {
      return ResponseEntity.status(401).build();
    }
    Long userId = Long.valueOf(jwtService.extractSubject(token));
    Object phone = body.get("phone");
    if (phone == null || !(phone instanceof String) || body.size() != 1) {
      return ResponseEntity.badRequest().build();
    }
    userService.updatePhone(userId, (String) phone);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/change-password")
  public ResponseEntity<Void> changePassword(
      @RequestHeader(value = "Authorization", required = false) String authorization,
      @Valid @RequestBody ChangePasswordRequest request) {
    if (authorization == null || !authorization.startsWith("Bearer ")) {
      return ResponseEntity.status(401).build();
    }
    String token = authorization.substring(7);
    if (!jwtService.isTokenValid(token)) {
      return ResponseEntity.status(401).build();
    }
    Long userId = Long.valueOf(jwtService.extractSubject(token));
    userService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());
    return ResponseEntity.noContent().build();
  }
}
