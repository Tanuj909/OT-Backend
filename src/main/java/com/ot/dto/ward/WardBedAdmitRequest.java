package com.ot.dto.ward;

import lombok.Data;

@Data
public class WardBedAdmitRequest {

    private String patientId;
    private String patientName;
    private String patientMrn;
    private Long scheduledOperationId;  // Optional — OT se transfer ho toh
    private String notes;
}