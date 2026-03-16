package com.ot.service;

import java.util.List;

import com.ot.dto.equipment.EquipmentRequest;
import com.ot.dto.equipment.EquipmentResponse;
import com.ot.enums.EquipmentStatus;

public interface EquipmentService {
    EquipmentResponse addEquipment(EquipmentRequest request);
    EquipmentResponse getEquipment(Long equipmentId);
    List<EquipmentResponse> getAllEquipment();
    EquipmentResponse updateEquipment(Long equipmentId, EquipmentRequest request);
    void deleteEquipment(Long equipmentId);
    EquipmentResponse updateEquipmentStatus(Long equipmentId, EquipmentStatus status);
}