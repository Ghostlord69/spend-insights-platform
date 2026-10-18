package com.spendinsights.service;

import com.spendinsights.dto.analytics.CategorySpend;
import com.spendinsights.dto.analytics.SpendSummaryResponse;
import com.spendinsights.dto.analytics.SpendTrendPoint;
import com.spendinsights.exception.ApiException;
import com.spendinsights.repository.SpendEventRepository;
import com.spendinsights.security.AuthenticatedUser;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsService {

	private final SpendEventRepository spendEventRepository;

	public AnalyticsService(SpendEventRepository spendEventRepository) {
		this.spendEventRepository = spendEventRepository;
	}

	@Transactional(readOnly = true)
	public SpendSummaryResponse summarize(AuthenticatedUser principal, Instant from, Instant to) {
		Instant rangeTo = to != null ? to : Instant.now();
		Instant rangeFrom = from != null ? from : rangeTo.minus(30, ChronoUnit.DAYS);
		if (!rangeFrom.isBefore(rangeTo)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "'from' must be earlier than 'to'");
		}

		UUID userId = principal.getId();
		Object[] summaryRow = unwrapSummary(spendEventRepository.summary(userId, rangeFrom, rangeTo));
		BigDecimal netSpend = toBigDecimal(summaryRow[0]);
		long totalEvents = toLong(summaryRow[1]);
		long uniqueCustomers = toLong(summaryRow[2]);

		List<SpendTrendPoint> trend = spendEventRepository.dailySpendTrend(userId, rangeFrom, rangeTo).stream()
				.map(this::toTrendPoint)
				.toList();

		List<CategorySpend> categories = spendEventRepository.categoryBreakdown(userId, rangeFrom, rangeTo).stream()
				.map(row -> new CategorySpend((String) row[0], toBigDecimal(row[1]), toLong(row[2])))
				.toList();

		return new SpendSummaryResponse(
				rangeFrom, rangeTo, netSpend, totalEvents, uniqueCustomers, trend, categories);
	}

	private Object[] unwrapSummary(Object raw) {
		if (raw instanceof Object[] row) {
			if (row.length == 1 && row[0] instanceof Object[] nested) {
				return nested;
			}
			return row;
		}
		return new Object[] { BigDecimal.ZERO, 0L, 0L };
	}

	private SpendTrendPoint toTrendPoint(Object[] row) {
		LocalDate date;
		Object bucket = row[0];
		if (bucket instanceof Timestamp timestamp) {
			date = timestamp.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
		}
		else if (bucket instanceof java.util.Date dateValue) {
			date = dateValue.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
		}
		else if (bucket instanceof Instant instant) {
			date = instant.atZone(ZoneOffset.UTC).toLocalDate();
		}
		else {
			date = LocalDate.parse(bucket.toString().substring(0, 10));
		}
		return new SpendTrendPoint(date, toBigDecimal(row[1]), toLong(row[2]));
	}

	private BigDecimal toBigDecimal(Object value) {
		if (value == null) {
			return BigDecimal.ZERO;
		}
		if (value instanceof BigDecimal bigDecimal) {
			return bigDecimal;
		}
		return new BigDecimal(value.toString());
	}

	private long toLong(Object value) {
		if (value == null) {
			return 0L;
		}
		if (value instanceof Number number) {
			return number.longValue();
		}
		return Long.parseLong(value.toString());
	}
}
