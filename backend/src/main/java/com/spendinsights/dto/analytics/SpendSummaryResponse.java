package com.spendinsights.dto.analytics;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record SpendSummaryResponse(
		Instant from,
		Instant to,
		BigDecimal netSpend,
		long totalEvents,
		long uniqueCustomers,
		List<SpendTrendPoint> dailyTrend,
		List<CategorySpend> byCategory) {
}
