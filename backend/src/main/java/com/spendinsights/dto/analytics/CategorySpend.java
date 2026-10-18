package com.spendinsights.dto.analytics;

import java.math.BigDecimal;

public record CategorySpend(String category, BigDecimal netSpend, long eventCount) {
}
