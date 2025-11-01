package com.autocare360.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

	@Value("${app.security.jwt.secret}")
	private String jwtSecret;

	@Value("${app.security.jwt.access-token-ttl-seconds:3600}")
	private long accessTtlSeconds;

	// ================== TOKEN GENERATION ==================
	public String generateToken(String subject, String email, String[] roles) {
		Instant now = Instant.now();
		Instant exp = now.plusSeconds(accessTtlSeconds);

		return Jwts.builder()
				.setSubject(subject)
				.setIssuedAt(Date.from(now))
				.setExpiration(Date.from(exp))
				.addClaims(Map.of(
						"email", email,
						"roles", roles
				))
				.signWith(getSigningKey(), SignatureAlgorithm.HS256)
				.compact();
	}

	// ================== TOKEN VALIDATION ==================
	public boolean isTokenValid(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
			return true;
		} catch (Exception e) {
			System.out.println("Token invalid: " + e.getMessage());
			return false;
		}
	}

	public String extractSubject(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(getSigningKey())
				.build()
				.parseClaimsJws(token)
				.getBody()
				.getSubject();
	}

	private Key getSigningKey() {
		byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	// ================== ROLE EXTRACTION ==================
	public List<String> extractRoles(String token) {
		Claims claims = Jwts.parserBuilder()
				.setSigningKey(getSigningKey())
				.build()
				.parseClaimsJws(token)
				.getBody();

		Object rolesObj = claims.get("roles");

		if (rolesObj instanceof List<?>) {
			// Convert all items to string safely
			return ((List<?>) rolesObj).stream()
					.map(Object::toString)
					.toList();
		} else if (rolesObj instanceof String) {
			return List.of((String) rolesObj);
		}

		return List.of(); // fallback empty list
	}

	// ================== ROLE CHECK ==================
	public boolean hasRole(String authorizationHeader, String role) {
		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) return false;

		String token = authorizationHeader.substring(7);

		if (!isTokenValid(token)) return false;

		List<String> roles = extractRoles(token);
		// Case-insensitive match
		return roles.stream().anyMatch(r -> r.equalsIgnoreCase(role));
	}
}
