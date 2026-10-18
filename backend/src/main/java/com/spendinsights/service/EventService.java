package com.spendinsights.service;

import com.spendinsights.domain.SpendEvent;
import com.spendinsights.domain.User;
import com.spendinsights.dto.event.CreateEventRequest;
import com.spendinsights.dto.event.EventPageResponse;
import com.spendinsights.dto.event.EventResponse;
import com.spendinsights.exception.ApiException;
import com.spendinsights.repository.SpendEventRepository;
import com.spendinsights.repository.UserRepository;
import com.spendinsights.security.AuthenticatedUser;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Service
public class EventService {

	private final SpendEventRepository spendEventRepository;
	private final UserRepository userRepository;
	private final JsonMapper jsonMapper;

	public EventService(
			SpendEventRepository spendEventRepository,
			UserRepository userRepository,
			JsonMapper jsonMapper) {
		this.spendEventRepository = spendEventRepository;
		this.userRepository = userRepository;
		this.jsonMapper = jsonMapper;
	}

	@Transactional
	public EventResponse create(AuthenticatedUser principal, CreateEventRequest request) {
		User user = userRepository.findById(principal.getId())
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));

		SpendEvent event = SpendEvent.builder()
				.user(user)
				.customerId(request.customerId().trim())
				.eventType(request.eventType())
				.category(request.category().trim())
				.amount(request.amount())
				.currency(request.currency().toUpperCase())
				.channel(request.channel().trim())
				.description(request.description())
				.occurredAt(request.occurredAt())
				.metadataJson(serializeMetadata(request))
				.build();

		return EventResponse.from(spendEventRepository.save(event));
	}

	@Transactional(readOnly = true)
	public EventPageResponse list(AuthenticatedUser principal, String customerId, int page, int size) {
		PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
		Page<SpendEvent> result;
		if (StringUtils.hasText(customerId)) {
			result = spendEventRepository.findByUserIdAndCustomerIdOrderByOccurredAtDesc(
					principal.getId(), customerId.trim(), pageable);
		}
		else {
			result = spendEventRepository.findByUserIdOrderByOccurredAtDesc(principal.getId(), pageable);
		}
		return new EventPageResponse(
				result.map(EventResponse::from).getContent(),
				result.getNumber(),
				result.getSize(),
				result.getTotalElements(),
				result.getTotalPages());
	}

	@Transactional(readOnly = true)
	public EventResponse getById(AuthenticatedUser principal, UUID eventId) {
		SpendEvent event = spendEventRepository.findById(eventId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Event not found"));
		if (!event.getUser().getId().equals(principal.getId())) {
			throw new ApiException(HttpStatus.FORBIDDEN, "Access denied");
		}
		return EventResponse.from(event);
	}

	private String serializeMetadata(CreateEventRequest request) {
		if (request.metadata() == null || request.metadata().isEmpty()) {
			return null;
		}
		try {
			return jsonMapper.writeValueAsString(request.metadata());
		}
		catch (JacksonException ex) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid metadata payload");
		}
	}
}
