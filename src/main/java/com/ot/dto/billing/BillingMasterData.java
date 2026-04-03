package com.ot.dto.billing;

import lombok.Data;

@Data
public class BillingMasterData {
    private Long id;
    private Long otOperationId;
    private String moduleType;
    private Double totalAmount;
    private String paymentStatus;
}