package com.spendinsights.dto.analytics;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SpendTrendPoint(LocalDate date, BigDecimal netSpend, long eventCount) {
}
