package com.ot.dto.preOpRequest;

import com.ot.enums.AssessmentStatus;

import lombok.Data;

@Data
public class PreOpStatusUpdateRequest {
    private AssessmentStatus status;
    private String reason; // optional — agar REASSESSMENT_REQUIRED ya CANCELLED ho toh reason mandatory
}