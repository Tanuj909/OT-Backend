package com.ot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ot.entity.EquipmentPricing;

public interface EquipmentPricingRepository extends JpaRepository<EquipmentPricing, Long>{
	
EquipmentPricing findByEquipmentId(Long equipmentId);

}
