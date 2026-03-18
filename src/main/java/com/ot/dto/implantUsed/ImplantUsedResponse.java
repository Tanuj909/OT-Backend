package com.ot.dto.implantUsed;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImplantUsedResponse {
    private Long id;
    private Long operationId;
    private Long catalogItemId;
    private String itemCode;
    private String itemName;
    private String manufacturer;
    private String serialNumber;
    private String batchNumber;
    private Integer quantity;
    private String bodyLocation;
    private String notes;
    private String usedBy;
    private LocalDateTime usedAt;
    private LocalDateTime createdAt;
}
