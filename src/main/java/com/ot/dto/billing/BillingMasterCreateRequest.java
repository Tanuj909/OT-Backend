package com.ot.dto.billing;


import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class BillingMasterCreateRequest {
    private Long hospitalExternalId;
    private Long patientExternalId;
    private Long otOperationId;
    private String moduleType;
}
