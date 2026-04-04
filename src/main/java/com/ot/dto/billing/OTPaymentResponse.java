package com.ot.dto.billing;


import java.time.LocalDateTime;

import com.ot.enums.OTPaymentStatus;
import com.ot.enums.OTPaymentType;
import com.ot.enums.PaymentMode;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OTPaymentResponse {
    private Long id;
    private Long otBillingDetailsId;
    private Long patientExternalId;
    private OTPaymentType paymentType;
    private PaymentMode paymentMode;
    private Double amount;
    private String referenceNumber;
    private String receivedBy;
    private OTPaymentStatus status;
    private String notes;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}