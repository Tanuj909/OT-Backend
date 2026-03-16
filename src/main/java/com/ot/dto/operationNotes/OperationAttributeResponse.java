package com.ot.dto.operationNotes;

import java.time.LocalDateTime;

import com.ot.enums.AttributeType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OperationAttributeResponse {
    private Long id;
    private Long operationId;
    private String attributeName;
    private String attributeValue;
    private AttributeType attributeType;
    private boolean isRequired;
    private boolean isSystem;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}