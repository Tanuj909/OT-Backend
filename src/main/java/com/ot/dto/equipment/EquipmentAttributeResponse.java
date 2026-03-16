package com.ot.dto.equipment;

import java.time.LocalDateTime;

import com.ot.enums.AttributeType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EquipmentAttributeResponse {
    private Long id;
    private Long equipmentId;
    private String attributeName;
    private String attributeValue;
    private AttributeType attributeType;
    private boolean isRequired;
    private boolean isSystem;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}