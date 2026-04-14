package com.ot.dto.medication;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MedicationUsageResponse {

    private Long id;

    private Long operationId;
    private Long wardRoomId;
    private Long wardBedId;
    private Long billingItemId;

    private String itemCode;
    private String name;
    private String batchNumber;
    private String category;
    private String type;

    private Integer quantity;
    private String givenBy;

    private LocalDateTime createdAt;
}