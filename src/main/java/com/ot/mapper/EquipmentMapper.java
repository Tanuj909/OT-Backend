package com.ot.mapper;

import com.ot.dto.equipment.EquipmentResponse;
import com.ot.entity.Equipment;

public class EquipmentMapper {
    public static EquipmentResponse toResponse(Equipment equipment) {
        return EquipmentResponse.builder()
                .id(equipment.getId())
                .name(equipment.getName())
                .model(equipment.getModel())
                .manufacturer(equipment.getManufacturer())
                .serialNumber(equipment.getSerialNumber())
                .assetCode(equipment.getAssetCode())
                .status(equipment.getStatus())
                .category(equipment.getCategory())
                .purchaseDate(equipment.getPurchaseDate())
                .lastMaintenanceDate(equipment.getLastMaintenanceDate())
                .nextMaintenanceDate(equipment.getNextMaintenanceDate())
                .capabilities(equipment.getCapabilities())
                .createdAt(equipment.getCreatedAt())
                .updatedAt(equipment.getUpdatedAt())
                .createdBy(equipment.getCreatedBy())
                .build();
    }
}