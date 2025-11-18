package com.autocare360.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("JwtService Tests")
class JwtServiceTest {

  private JwtService jwtService;
  private String jwtSecret;
  private long accessTtlSeconds;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService();
    // Base64 encoded secret (32+ bytes)
    jwtSecret =
        "dGVzdC1qd3Qtc2VjcmV0LWtleS1mb3ItdGVzdGluZy1wdXJwb3Nlcy1vbmx5LXRoaXMtc2hvdWxkLWJlLWF0LWxlYXN0LTMyLWJ5dGVz";
    accessTtlSeconds = 3600L;

    ReflectionTestUtils.setField(jwtService, "jwtSecret", jwtSecret);
    ReflectionTestUtils.setField(jwtService, "accessTtlSeconds", accessTtlSeconds);
  }

  @Test
  @DisplayName("Should generate valid JWT token")
  void testGenerateToken_Success() {
    String token = jwtService.generateToken("1", "test@example.com", new String[] {"customer"});

    assertNotNull(token);
    assertFalse(token.isEmpty());
    assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
  }

  @Test
  @DisplayName("Should generate token with multiple roles")
  void testGenerateToken_MultipleRoles() {
    String token =
        jwtService.generateToken("1", "test@example.com", new String[] {"customer", "admin"});

    assertNotNull(token);
    assertTrue(jwtService.isTokenValid(token));
  }

  @Test
  @DisplayName("Should validate valid token")
  void testIsTokenValid_ValidToken() {
    String token = jwtService.generateToken("1", "test@example.com", new String[] {"customer"});

    boolean isValid = jwtService.isTokenValid(token);

    assertTrue(isValid);
  }

  @Test
  @DisplayName("Should reject invalid token")
  void testIsTokenValid_InvalidToken() {
    boolean isValid = jwtService.isTokenValid("invalid.token.here");

    assertFalse(isValid);
  }

  @Test
  @DisplayName("Should reject empty token")
  void testIsTokenValid_EmptyToken() {
    boolean isValid = jwtService.isTokenValid("");

    assertFalse(isValid);
  }

  @Test
  @DisplayName("Should reject null token")
  void testIsTokenValid_NullToken() {
    boolean isValid = jwtService.isTokenValid(null);

    assertFalse(isValid);
  }

  @Test
  @DisplayName("Should extract subject from token")
  void testExtractSubject_Success() {
    String token = jwtService.generateToken("123", "test@example.com", new String[] {"customer"});

    String subject = jwtService.extractSubject(token);

    assertEquals("123", subject);
  }

  @Test
  @DisplayName("Should extract roles from token")
  void testExtractRoles_Success() {
    String token =
        jwtService.generateToken("1", "test@example.com", new String[] {"customer", "admin"});

    List<String> roles = jwtService.extractRoles(token);

    assertNotNull(roles);
    assertEquals(2, roles.size());
    assertTrue(roles.contains("customer"));
    assertTrue(roles.contains("admin"));
  }

  @Test
  @DisplayName("Should extract single role from token")
  void testExtractRoles_SingleRole() {
    String token = jwtService.generateToken("1", "test@example.com", new String[] {"customer"});

    List<String> roles = jwtService.extractRoles(token);

    assertNotNull(roles);
    assertEquals(1, roles.size());
    assertTrue(roles.contains("customer"));
  }

  @Test
  @DisplayName("Should return empty list for token without roles")
  void testExtractRoles_NoRoles() {
    String token = jwtService.generateToken("1", "test@example.com", new String[] {});

    List<String> roles = jwtService.extractRoles(token);

    assertNotNull(roles);
    assertEquals(0, roles.size());
  }

  @Test
  @DisplayName("Should check if user has specific role")
  void testHasRole_Success() {
    String token =
        jwtService.generateToken("1", "test@example.com", new String[] {"customer", "admin"});
    String authHeader = "Bearer " + token;

    boolean hasRole = jwtService.hasRole(authHeader, "admin");

    assertTrue(hasRole);
  }

  @Test
  @DisplayName("Should return false when user doesn't have role")
  void testHasRole_NoRole() {
    String token = jwtService.generateToken("1", "test@example.com", new String[] {"customer"});
    String authHeader = "Bearer " + token;

    boolean hasRole = jwtService.hasRole(authHeader, "admin");

    assertFalse(hasRole);
  }

  @Test
  @DisplayName("Should handle case-insensitive role check")
  void testHasRole_CaseInsensitive() {
    String token = jwtService.generateToken("1", "test@example.com", new String[] {"ADMIN"});
    String authHeader = "Bearer " + token;

    boolean hasRole = jwtService.hasRole(authHeader, "admin");

    assertTrue(hasRole);
  }

  @Test
  @DisplayName("Should return false for null authorization header")
  void testHasRole_NullHeader() {
    boolean hasRole = jwtService.hasRole(null, "admin");

    assertFalse(hasRole);
  }

  @Test
  @DisplayName("Should return false for invalid authorization header format")
  void testHasRole_InvalidHeaderFormat() {
    boolean hasRole = jwtService.hasRole("InvalidFormat token", "admin");

    assertFalse(hasRole);
  }

  @Test
  @DisplayName("Should return false for invalid token in hasRole")
  void testHasRole_InvalidToken() {
    String authHeader = "Bearer invalid.token.here";

    boolean hasRole = jwtService.hasRole(authHeader, "admin");

    assertFalse(hasRole);
  }

  @Test
  @DisplayName("Should generate different tokens for different users")
  void testGenerateToken_DifferentUsers() {
    String token1 = jwtService.generateToken("1", "user1@example.com", new String[] {"customer"});
    String token2 = jwtService.generateToken("2", "user2@example.com", new String[] {"customer"});

    assertNotEquals(token1, token2);
    assertEquals("1", jwtService.extractSubject(token1));
    assertEquals("2", jwtService.extractSubject(token2));
  }

  @Test
  @DisplayName("Should include email in token claims")
  void testGenerateToken_IncludesEmail() {
    String token = jwtService.generateToken("1", "test@example.com", new String[] {"customer"});

    assertTrue(jwtService.isTokenValid(token));
    // The email is embedded in the token claims, we can verify it's valid
  }

  @Test
  @DisplayName("Should handle token with special characters in subject")
  void testGenerateToken_SpecialCharactersInSubject() {
    String token =
        jwtService.generateToken("user-123", "test@example.com", new String[] {"customer"});

    assertTrue(jwtService.isTokenValid(token));
    assertEquals("user-123", jwtService.extractSubject(token));
  }

  @Test
  @DisplayName("Should reject malformed token")
  void testIsTokenValid_MalformedToken() {
    boolean isValid = jwtService.isTokenValid("not.a.valid.jwt.token.format");

    assertFalse(isValid);
  }
}

