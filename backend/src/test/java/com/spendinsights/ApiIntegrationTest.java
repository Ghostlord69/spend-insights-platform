package com.spendinsights;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JsonMapper jsonMapper;

	@Test
	void registerIngestAndQuerySpendSummary() throws Exception {
		String email = "demo+" + Instant.now().toEpochMilli() + "@spendinsights.dev";

		MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "%s",
								  "password": "SecurePass1",
								  "fullName": "Demo Analyst"
								}
								""".formatted(email)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andReturn();

		String token = jsonMapper.readTree(registerResult.getResponse().getContentAsString())
				.get("accessToken")
				.asString();

		Instant occurredAt = Instant.now().minus(2, ChronoUnit.DAYS);
		mockMvc.perform(post("/api/v1/events")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "customerId": "cust-1001",
								  "eventType": "PURCHASE",
								  "category": "Travel",
								  "amount": 249.50,
								  "currency": "USD",
								  "channel": "web",
								  "description": "Hotel booking",
								  "occurredAt": "%s",
								  "metadata": {"city": "Bangkok"}
								}
								""".formatted(occurredAt)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.customerId").value("cust-1001"))
				.andExpect(jsonPath("$.category").value("Travel"));

		mockMvc.perform(post("/api/v1/events")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "customerId": "cust-1001",
								  "eventType": "INTERACTION",
								  "category": "Support",
								  "amount": 0,
								  "currency": "USD",
								  "channel": "web",
								  "description": "Viewed invoice",
								  "occurredAt": "%s"
								}
								""".formatted(Instant.now().minus(1, ChronoUnit.DAYS))))
				.andExpect(status().isCreated());

		MvcResult eventsResult = mockMvc.perform(get("/api/v1/events")
						.header("Authorization", "Bearer " + token)
						.param("customerId", "cust-1001"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElements").value(2))
				.andReturn();

		assertThat(eventsResult.getResponse().getContentAsString()).contains("Travel");

		MvcResult summaryResult = mockMvc.perform(get("/api/v1/analytics/spend-summary")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalEvents").value(2))
				.andExpect(jsonPath("$.uniqueCustomers").value(1))
				.andReturn();

		JsonNode summary = jsonMapper.readTree(summaryResult.getResponse().getContentAsString());
		assertThat(new BigDecimal(summary.get("netSpend").asString())).isEqualByComparingTo("249.5");
		assertThat(summary.get("byCategory")).isNotEmpty();
		assertThat(summary.get("dailyTrend")).isNotEmpty();
	}

	@Test
	void rejectsUnauthenticatedEventCreate() throws Exception {
		mockMvc.perform(post("/api/v1/events")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "customerId": "cust-1",
								  "eventType": "PURCHASE",
								  "category": "Food",
								  "amount": 10,
								  "currency": "USD",
								  "channel": "web",
								  "occurredAt": "%s"
								}
								""".formatted(Instant.now())))
				.andExpect(status().isUnauthorized());
	}
}
