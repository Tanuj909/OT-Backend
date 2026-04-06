package com.ot.dto.billing;

import java.time.LocalDateTime;

import com.ot.enums.OTRefundStatus;
import com.ot.enums.PaymentMode;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OTRefundResponse {
    private Long id;
    private Long otBillingDetailsId;
    private Long paymentId;
    private Double refundAmount;
    private String reason;
    private PaymentMode refundMode;
    private String referenceNumber;
    private String processedBy;
    private OTRefundStatus refundStatus;
    private LocalDateTime refundedAt;
    private LocalDateTime createdAt;
}