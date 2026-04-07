package com.ot.service;

import java.util.List;

import com.ot.dto.equipment.EquipmentPricingRequest;
import com.ot.dto.equipment.EquipmentPricingResponse;

public interface EquipmentPricingService {
	
    EquipmentPricingResponse createPricing(EquipmentPricingRequest request);

    EquipmentPricingResponse updatePricing(Long id, EquipmentPricingRequest request);

    EquipmentPricingResponse getById(Long id);

    EquipmentPricingResponse getByEquipment(Long equipmentId);

    void deletePricing(Long id);

}
