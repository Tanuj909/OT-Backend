package com.ot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ot.entity.EquipmentAttribute;

public interface EquipmentAttributeRepository extends JpaRepository<EquipmentAttribute, Long> {
    List<EquipmentAttribute> findAllByEquipmentId(Long equipmentId);
    boolean existsByEquipmentIdAndAttributeName(Long equipmentId, String attributeName);
}
