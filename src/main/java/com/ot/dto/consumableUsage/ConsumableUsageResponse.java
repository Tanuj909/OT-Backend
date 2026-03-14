package com.ot.dto.consumableUsage;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConsumableUsageResponse {
    private Long id;
    private Long operationId;
    private String consumableCode;
    private String consumableName;
    private String category;
    private Integer quantityUsed;
    private Integer quantityWasted;
    private String unitOfMeasure;
    private String batchNumber;
    private LocalDateTime expiryDate;
    private String issuedBy;
    private String returnedBy;
    private Boolean isSterile;
    private LocalDateTime sterilizationDate;
    private LocalDateTime createdAt;
}