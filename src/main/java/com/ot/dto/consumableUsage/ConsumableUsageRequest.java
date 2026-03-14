package com.ot.dto.consumableUsage;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ConsumableUsageRequest {
    private String consumableCode;
    private String consumableName;
    private String category;
    private Integer quantityUsed;
    private Integer quantityWasted;
    private String unitOfMeasure;
    private String batchNumber;
    private LocalDateTime expiryDate;
    private Boolean isSterile;
    private LocalDateTime sterilizationDate;
}