package com.ot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ot.entity.VitalsLog;

public interface VitalsLogRepository extends JpaRepository<VitalsLog, Long> {
    List<VitalsLog> findByScheduledOperationIdOrderByRecordedTimeAsc(Long operationId);
}