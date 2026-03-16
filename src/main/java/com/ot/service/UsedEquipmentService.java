package com.ot.service;

import java.util.List;

import com.ot.dto.equipment.UsedEquipmentRequest;
import com.ot.dto.equipment.UsedEquipmentResponse;

public interface UsedEquipmentService {
    UsedEquipmentResponse addEquipmentToOperation(Long operationId, UsedEquipmentRequest request);
    List<UsedEquipmentResponse> getUsedEquipment(Long operationId);
    UsedEquipmentResponse updateUsedEquipment(Long operationId, Long usedEquipmentId, UsedEquipmentRequest request);
    void removeEquipmentFromOperation(Long operationId, Long usedEquipmentId);
}