package com.ot.dto.equipment;

import com.ot.enums.AttributeType;

import lombok.Data;

@Data
public class EquipmentAttributeRequest {
    private String attributeName;
    private String attributeValue;
    private AttributeType attributeType;
    private boolean isRequired;
    private boolean isSystem;
}