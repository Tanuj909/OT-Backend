package com.ot.dto.billing;

import com.ot.enums.OTPaymentType;
import com.ot.enums.PaymentMode;

import lombok.Data;

@Data
public class OTPaymentRequest {
    private Long operationExternalId;
    private Long patientExternalId;
    private OTPaymentType paymentType;
    private PaymentMode paymentMode;
    private Double amount;
    private String referenceNumber;
    private String receivedBy;
    private String notes;
}
