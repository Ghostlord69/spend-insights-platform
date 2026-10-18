package com.spendinsights.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.spendinsights.config.JwtProperties;
import com.spendinsights.domain.Role;
import com.spendinsights.domain.User;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

	private JwtService jwtService;

	@BeforeEach
	void setUp() {
		jwtService = new JwtService(new JwtProperties(
				"test-secret-key-that-is-long-enough-for-hmac-sha-256-signing!!",
				3_600_000L));
	}

	@Test
	void generatesAndValidatesToken() {
		User user = User.builder()
				.id(UUID.randomUUID())
				.email("analyst@example.com")
				.passwordHash("hash")
				.fullName("Analyst")
				.role(Role.USER)
				.build();

		String token = jwtService.generateToken(user);

		assertThat(token).isNotBlank();
		assertThat(jwtService.isTokenValid(token)).isTrue();
		assertThat(jwtService.extractUserId(token)).isEqualTo(user.getId());
	}

	@Test
	void rejectsTamperedToken() {
		User user = User.builder()
				.id(UUID.randomUUID())
				.email("analyst@example.com")
				.passwordHash("hash")
				.fullName("Analyst")
				.role(Role.USER)
				.build();

		String token = jwtService.generateToken(user);
		String tampered = token.substring(0, token.length() - 4) + "xxxx";

		assertThat(jwtService.isTokenValid(tampered)).isFalse();
	}
}
