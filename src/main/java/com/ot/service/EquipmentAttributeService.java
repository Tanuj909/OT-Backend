package com.ot.service;

import java.util.List;

import com.ot.dto.equipment.EquipmentAttributeRequest;
import com.ot.dto.equipment.EquipmentAttributeResponse;

public interface EquipmentAttributeService {
    EquipmentAttributeResponse addAttribute(Long equipmentId, EquipmentAttributeRequest request);
    List<EquipmentAttributeResponse> getAttributes(Long equipmentId);
    EquipmentAttributeResponse updateAttribute(Long equipmentId, Long attributeId, EquipmentAttributeRequest request);
    void deleteAttribute(Long equipmentId, Long attributeId);
}
