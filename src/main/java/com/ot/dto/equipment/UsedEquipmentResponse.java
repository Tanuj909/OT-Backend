package com.ot.dto.equipment;

import java.time.LocalDateTime;

import com.ot.enums.EquipmentCategory;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UsedEquipmentResponse {
    private Long id;
    private Long operationId;
    private Long equipmentId;
    private String equipmentName;
    private String assetCode;
    private EquipmentCategory category;
    private Integer quantityUsed;
    private boolean isConsumable;
    private LocalDateTime usedFrom;
    private LocalDateTime usedUntil;
    private LocalDateTime createdAt;
}
