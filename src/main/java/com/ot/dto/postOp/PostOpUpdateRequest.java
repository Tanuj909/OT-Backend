package com.ot.dto.postOp;

import java.time.LocalDateTime;

import com.ot.enums.AldreteScore;

import lombok.Data;

@Data
public class PostOpUpdateRequest {
    private AldreteScore aldreteScore;
    private String immediatePostOpCondition;
    private String painManagement;
    private String medicationsGiven;
    private String drainDetails;
    private String dressingDetails;
    private String postOpInstructions;
    private String followUpPlan;
    private LocalDateTime recoveryStartTime;
    private LocalDateTime recoveryEndTime;
}