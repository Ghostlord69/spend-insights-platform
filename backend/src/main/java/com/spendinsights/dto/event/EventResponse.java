package com.spendinsights.dto.event;

import com.spendinsights.domain.EventType;
import com.spendinsights.domain.SpendEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record EventResponse(
		UUID id,
		String customerId,
		EventType eventType,
		String category,
		BigDecimal amount,
		String currency,
		String channel,
		String description,
		Instant occurredAt,
		Instant createdAt) {

	public static EventResponse from(SpendEvent event) {
		return new EventResponse(
				event.getId(),
				event.getCustomerId(),
				event.getEventType(),
				event.getCategory(),
				event.getAmount(),
				event.getCurrency(),
				event.getChannel(),
				event.getDescription(),
				event.getOccurredAt(),
				event.getCreatedAt());
	}
}
