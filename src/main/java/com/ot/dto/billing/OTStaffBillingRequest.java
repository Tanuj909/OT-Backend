package com.ot.dto.billing;

import lombok.Data;

@Data
public class OTStaffBillingRequest {
    private Long operationExternalId;
    private Long staffExternalId;
    private String staffName;
    private String staffRole;
    private Double fees;
    private Double discountPercent;
    private Double gstPercent;

}
