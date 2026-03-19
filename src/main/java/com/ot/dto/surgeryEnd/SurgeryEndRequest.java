package com.ot.dto.surgeryEnd;

import lombok.Data;

@Data
public class SurgeryEndRequest {
    private String drainDetails;
    private String dressingDetails;
    private String recoveryLocation;    // "PACU", "Recovery Room"
    private String immediatePostOpCondition;
}