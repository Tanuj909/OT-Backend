package com.ot.repository;

import com.ot.entity.DoctorVisit;
import com.ot.enums.DoctorVisitStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorVisitRepository extends JpaRepository<DoctorVisit, Long> {

    // By operation — latest first
    List<DoctorVisit> findByScheduledOperationIdOrderByVisitTimeDesc(Long operationId);

    // By admission — latest first
    List<DoctorVisit> findByWardAdmissionIdOrderByVisitTimeDesc(Long wardAdmissionId);

    // By operation + status (e.g. SCHEDULED upcoming visits)
    List<DoctorVisit> findByScheduledOperationIdAndStatusOrderByVisitTimeAsc(
            Long operationId, DoctorVisitStatus status);

    // Latest visit for operation
    Optional<DoctorVisit> findTopByScheduledOperationIdOrderByVisitTimeDesc(Long operationId);

    // Check if discharge recommended in any visit for this operation
    boolean existsByScheduledOperationIdAndDischargeRecommendedTrue(Long operationId);
}