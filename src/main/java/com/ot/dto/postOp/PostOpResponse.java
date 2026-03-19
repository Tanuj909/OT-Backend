package com.ot.dto.postOp;

import java.time.LocalDateTime;

import com.ot.enums.AldreteScore;
import com.ot.enums.RecoveryStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostOpResponse {
    private Long id;
    private Long operationId;
    private LocalDateTime surgeryEndTime;
    private LocalDateTime recoveryStartTime;
    private LocalDateTime recoveryEndTime;
    private String recoveryLocation;
    private AldreteScore aldreteScore;
    private String immediatePostOpCondition;
    private String painManagement;
    private String medicationsGiven;
    private String drainDetails;
    private String dressingDetails;
    private String postOpInstructions;
    private String followUpPlan;
    private String transferredTo;
    private String transferredBy;
    private String receivedBy;
    private RecoveryStatus status;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
