package com.ot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ot.entity.ConsumableUsage;
import com.ot.entity.ScheduledOperation;

public interface ConsumableUsageRepository extends JpaRepository<ConsumableUsage, Long> {
    List<ConsumableUsage> findByScheduledOperationIdOrderByCreatedAtAsc(Long operationId);
    boolean existsByScheduledOperationIdAndConsumableCode(Long operationId, String consumableCode);
//	Optional<ConsumableUsage> findByScheduledOperation(ScheduledOperation operation);
    List<ConsumableUsage> findByScheduledOperation(ScheduledOperation operation);
}
