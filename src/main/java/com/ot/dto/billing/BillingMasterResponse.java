package com.ot.dto.billing;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class BillingMasterResponse {
    private Long id;
    private Long hospitalExternalId;
    private Long patientExternalId;
    private Long admissionId;
    private Long otOperationId;
    private String moduleType;
    private Double totalAmount;
    private String paymentStatus;
    private String paymentMode;
    private String advancePaymentMode;
    private LocalDateTime billingDate;
    private LocalDateTime updatedAt;
}
