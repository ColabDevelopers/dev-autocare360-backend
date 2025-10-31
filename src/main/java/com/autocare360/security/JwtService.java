package com.autocare360.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

	@Value("${app.security.jwt.secret}")
	private String jwtSecret;

    @Value("${app.security.jwt.access-token-ttl-seconds:3600}")
    private long accessTtlSeconds;

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

	public boolean isTokenValid(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public String extractSubject(String token) {
		return Jwts.parserBuilder().setSigningKey(getSigningKey()).build()
				.parseClaimsJws(token).getBody().getSubject();
	}

	private Key getSigningKey() {
		byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}


