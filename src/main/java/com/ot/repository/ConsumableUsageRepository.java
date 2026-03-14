package com.ot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ot.entity.ConsumableUsage;

public interface ConsumableUsageRepository extends JpaRepository<ConsumableUsage, Long> {
    List<ConsumableUsage> findByScheduledOperationIdOrderByCreatedAtAsc(Long operationId);
    boolean existsByScheduledOperationIdAndConsumableCode(Long operationId, String consumableCode);
}
