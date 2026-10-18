package com.spendinsights.service;

import com.spendinsights.domain.Role;
import com.spendinsights.domain.User;
import com.spendinsights.dto.auth.AuthResponse;
import com.spendinsights.dto.auth.LoginRequest;
import com.spendinsights.dto.auth.RegisterRequest;
import com.spendinsights.exception.ApiException;
import com.spendinsights.repository.UserRepository;
import com.spendinsights.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;

	public AuthService(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			JwtService jwtService,
			AuthenticationManager authenticationManager) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.authenticationManager = authenticationManager;
	}

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		if (userRepository.existsByEmailIgnoreCase(request.email())) {
			throw new ApiException(HttpStatus.CONFLICT, "Email already registered");
		}
		User user = User.builder()
				.email(request.email().trim().toLowerCase())
				.passwordHash(passwordEncoder.encode(request.password()))
				.fullName(request.fullName().trim())
				.role(Role.USER)
				.build();
		userRepository.save(user);
		return toAuthResponse(user);
	}

	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password()));
		User user = userRepository.findByEmailIgnoreCase(request.email())
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
		return toAuthResponse(user);
	}

	private AuthResponse toAuthResponse(User user) {
		return new AuthResponse(
				jwtService.generateToken(user),
				"Bearer",
				user.getId(),
				user.getEmail(),
				user.getFullName());
	}
}
