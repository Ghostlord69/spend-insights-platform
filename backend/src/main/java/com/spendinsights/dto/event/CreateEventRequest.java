package com.spendinsights.dto.event;

import com.spendinsights.domain.EventType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record CreateEventRequest(
		@NotBlank @Size(max = 100) String customerId,
		@NotNull EventType eventType,
		@NotBlank @Size(max = 100) String category,
		@NotNull @DecimalMin("0.0") BigDecimal amount,
		@NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currency,
		@NotBlank @Size(max = 50) String channel,
		@Size(max = 500) String description,
		@NotNull Instant occurredAt,
		Map<String, Object> metadata) {
}
