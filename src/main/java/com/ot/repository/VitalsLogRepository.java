package com.ot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ot.entity.ScheduledOperation;
import com.ot.entity.VitalsLog;
import com.ot.entity.Ward;
import com.ot.enums.VitalsPhase;

public interface VitalsLogRepository extends JpaRepository<VitalsLog, Long> {
    List<VitalsLog> findByScheduledOperationIdOrderByRecordedTimeAsc(Long operationId);
    
    // Ward vitals
//    List<VitalsLog> findByWardAndPhase(Ward ward, VitalsPhase phase);

    // Latest stable entry
    Optional<VitalsLog> findTopByScheduledOperationAndPhaseAndIsStableOrderByRecordedTimeDesc(
            ScheduledOperation operation, VitalsPhase phase, Boolean isStable);

    List<VitalsLog> findByScheduledOperation(ScheduledOperation operation);
    
    // IntraOp vitals
    List<VitalsLog> findByScheduledOperationAndPhase(
            ScheduledOperation operation, VitalsPhase phase);
    
    // VitalsLogRepository
    Optional<VitalsLog> findTopByScheduledOperationAndPhaseOrderByRecordedTimeDesc(
            ScheduledOperation operation, VitalsPhase phase);
}