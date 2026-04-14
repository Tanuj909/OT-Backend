package com.ot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ot.entity.Hospital;
import com.ot.entity.MedicationUsage;

@Repository
public interface MedicationUsageRepository extends JpaRepository<MedicationUsage, Long> {

    List<MedicationUsage> findByHospitalAndScheduledOperationId(Hospital hospital, Long operationId);

    List<MedicationUsage> findByHospitalAndWardRoomId(Hospital hospital, Long wardRoomId);

    List<MedicationUsage> findByHospitalAndWardBedId(Hospital hospital, Long wardBedId);
}