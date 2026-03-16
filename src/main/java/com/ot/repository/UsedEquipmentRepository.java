package com.ot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ot.entity.UsedEquipment;

public interface UsedEquipmentRepository extends JpaRepository<UsedEquipment, Long> {
    List<UsedEquipment> findAllByScheduledOperationId(Long operationId);
    boolean existsByScheduledOperationIdAndEquipmentId(Long operationId, Long equipmentId);
}