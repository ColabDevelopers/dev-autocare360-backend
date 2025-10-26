package com.autocare360.security;

import com.autocare360.repo.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final UserRepository userRepository;

	@Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
			throws ServletException, IOException {
		String header = request.getHeader("Authorization");
		if (header != null && header.startsWith("Bearer ")) {
			String token = header.substring(7);
			if (jwtService.isTokenValid(token)) {
				String subject = jwtService.extractSubject(token);
				userRepository.findById(Long.valueOf(subject)).ifPresent(user -> {
					List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
							.map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName()))
							.collect(Collectors.toList());
					SecurityContextHolder.getContext().setAuthentication(
							new UsernamePasswordAuthenticationToken(user.getEmail(), null, authorities)
					);
				});
			}
		}
		filterChain.doFilter(request, response);
	}
}


