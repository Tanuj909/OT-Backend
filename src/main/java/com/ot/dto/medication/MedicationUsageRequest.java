package com.ot.dto.medication;

import lombok.Data;

@Data
public class MedicationUsageRequest {

	private Long catalogId;
    private Long operationId;       // Optional
    private Long wardRoomId;
    private Long wardBedId;
    private String batchNumber;
    private Integer quantity;
}