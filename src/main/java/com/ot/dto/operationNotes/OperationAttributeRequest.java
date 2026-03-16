package com.ot.dto.operationNotes;

import com.ot.enums.AttributeType;

import lombok.Data;

@Data
public class OperationAttributeRequest {
    private String attributeName;
    private String attributeValue;
    private AttributeType attributeType;
    private boolean isRequired;
    private boolean isSystem;
}