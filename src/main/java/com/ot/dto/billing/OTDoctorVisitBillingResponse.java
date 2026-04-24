package com.ot.dto.billing;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OTDoctorVisitBillingResponse {

    private Long   id;
    private Long   otBillingDetailsId;
    private Long   operationExternalId;     // convenience field

    private Long   doctorExternalId;
    private String doctorName;

    private LocalDateTime visitTime;
    private Double fees;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}