package com.ot.dto.billing;

import com.ot.enums.PaymentMode;

import lombok.Data;

@Data
public class OTAdvancePaymentRequest {
    private Long billingMasterId;      // required
    private Long operationExternalId;  // required (for OTBillingDetails)

    private Long patientExternalId;

    private Double amount;

    private PaymentMode paymentMode;   // CASH / UPI / CARD

    private String referenceNumber;
    private String receivedBy;
    private String notes;
}
