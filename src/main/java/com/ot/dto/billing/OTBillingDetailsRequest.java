package com.ot.dto.billing;


import lombok.Data;

@Data
public class OTBillingDetailsRequest {
    private Long billingMasterId;
    private String operationReference;
}
