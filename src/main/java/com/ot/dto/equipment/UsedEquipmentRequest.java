package com.ot.dto.equipment;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UsedEquipmentRequest {
    private Long equipmentId;
    private Integer quantityUsed;
    private boolean isConsumable;
    private LocalDateTime usedFrom;
    private LocalDateTime usedUntil;
}
