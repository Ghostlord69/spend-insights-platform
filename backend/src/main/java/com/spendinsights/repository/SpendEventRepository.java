package com.spendinsights.repository;

import com.spendinsights.domain.SpendEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpendEventRepository extends JpaRepository<SpendEvent, UUID> {

	Page<SpendEvent> findByUserIdOrderByOccurredAtDesc(UUID userId, Pageable pageable);

	Page<SpendEvent> findByUserIdAndCustomerIdOrderByOccurredAtDesc(
			UUID userId, String customerId, Pageable pageable);

	@Query("""
			SELECT e FROM SpendEvent e
			WHERE e.user.id = :userId
			  AND e.occurredAt >= :from
			  AND e.occurredAt < :to
			ORDER BY e.occurredAt ASC
			""")
	List<SpendEvent> findInRange(
			@Param("userId") UUID userId,
			@Param("from") Instant from,
			@Param("to") Instant to);

	@Query(value = """
			SELECT DATE_TRUNC('day', e.occurred_at) AS bucket,
			       COALESCE(SUM(CASE WHEN e.event_type = 'REFUND' THEN -e.amount ELSE e.amount END), 0) AS net_spend,
			       COUNT(*) AS event_count
			FROM spend_events e
			WHERE e.user_id = :userId
			  AND e.occurred_at >= :from
			  AND e.occurred_at < :to
			  AND e.event_type IN ('PURCHASE', 'REFUND', 'SUBSCRIPTION')
			GROUP BY DATE_TRUNC('day', e.occurred_at)
			ORDER BY bucket ASC
			""", nativeQuery = true)
	List<Object[]> dailySpendTrend(
			@Param("userId") UUID userId,
			@Param("from") Instant from,
			@Param("to") Instant to);

	@Query(value = """
			SELECT e.category,
			       COALESCE(SUM(CASE WHEN e.event_type = 'REFUND' THEN -e.amount ELSE e.amount END), 0) AS net_spend,
			       COUNT(*) AS event_count
			FROM spend_events e
			WHERE e.user_id = :userId
			  AND e.occurred_at >= :from
			  AND e.occurred_at < :to
			  AND e.event_type IN ('PURCHASE', 'REFUND', 'SUBSCRIPTION')
			GROUP BY e.category
			ORDER BY net_spend DESC
			""", nativeQuery = true)
	List<Object[]> categoryBreakdown(
			@Param("userId") UUID userId,
			@Param("from") Instant from,
			@Param("to") Instant to);

	@Query(value = """
			SELECT COALESCE(SUM(CASE WHEN e.event_type = 'REFUND' THEN -e.amount ELSE e.amount END), 0),
			       COUNT(*),
			       COUNT(DISTINCT e.customer_id)
			FROM spend_events e
			WHERE e.user_id = :userId
			  AND e.occurred_at >= :from
			  AND e.occurred_at < :to
			""", nativeQuery = true)
	Object[] summary(
			@Param("userId") UUID userId,
			@Param("from") Instant from,
			@Param("to") Instant to);
}
