package com.ot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ot.entity.WardAdmission;

@Repository
public interface WardAdmissionRepository extends JpaRepository<WardAdmission, Long> {

    // Operation ka active admission (abhi discharge nahi hua)
    Optional<WardAdmission> findByOperationIdAndDischargedWhenIsNull(Long operationId);

    // Patient ki poori history
    List<WardAdmission> findByPatientIdOrderByAdmissionTimeDesc(String patientId);

    // Room ki poori history
    List<WardAdmission> findByWardRoomIdOrderByAdmissionTimeDesc(Long wardRoomId);

    // Bed ki poori history
    List<WardAdmission> findByWardBedIdOrderByAdmissionTimeDesc(Long wardBedId);
}