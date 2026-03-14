package com.ot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ot.entity.AuditTrail;

public interface AuditTrailRepository extends JpaRepository<AuditTrail, Long> {
    List<AuditTrail> findByHospitalIdOrderByTimestampDesc(Long hospitalId);
    List<AuditTrail> findByEntityNameAndEntityIdOrderByTimestampDesc(String entityName, String entityId);
    List<AuditTrail> findByUserIdOrderByTimestampDesc(String userId);
}