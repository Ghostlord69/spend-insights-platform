package com.spendinsights.controller;

import com.spendinsights.dto.event.CreateEventRequest;
import com.spendinsights.dto.event.EventPageResponse;
import com.spendinsights.dto.event.EventResponse;
import com.spendinsights.security.AuthenticatedUser;
import com.spendinsights.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Events")
@SecurityRequirement(name = "bearerAuth")
public class EventController {

	private final EventService eventService;

	public EventController(EventService eventService) {
		this.eventService = eventService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Ingest a customer spend or interaction event")
	public EventResponse create(
			@AuthenticationPrincipal AuthenticatedUser principal,
			@Valid @RequestBody CreateEventRequest request) {
		return eventService.create(principal, request);
	}

	@GetMapping
	@Operation(summary = "Query ingested events")
	public EventPageResponse list(
			@AuthenticationPrincipal AuthenticatedUser principal,
			@RequestParam(required = false) String customerId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return eventService.list(principal, customerId, page, size);
	}

	@GetMapping("/{eventId}")
	@Operation(summary = "Get a single event by id")
	public EventResponse getById(
			@AuthenticationPrincipal AuthenticatedUser principal,
			@PathVariable UUID eventId) {
		return eventService.getById(principal, eventId);
	}
}
