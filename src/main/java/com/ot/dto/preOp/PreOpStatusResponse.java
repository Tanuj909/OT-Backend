package com.ot.dto.preOp;

import com.ot.enums.AssessmentStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PreOpStatusResponse {

    private Long operationId;
    private AssessmentStatus status;
    private Boolean exists;
    private String reason; // optional (for cancelled / reassessment)
}