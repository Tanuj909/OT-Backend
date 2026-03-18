package com.ot.service;

import com.ot.dto.preOp.PreOpAssessmentRequest;
import com.ot.dto.preOp.PreOpAssessmentResponse;
import com.ot.dto.preOp.PreOpStatusUpdateRequest;

public interface PreOpService {

	PreOpAssessmentResponse getPreOpAssessment(Long operationId);

	PreOpAssessmentResponse createPreOpAssessment(Long operationId, PreOpAssessmentRequest request);

	PreOpAssessmentResponse updatePreOpAssessment(Long operationId, PreOpAssessmentRequest request);

	PreOpAssessmentResponse updatePreOpStatus(Long operationId, PreOpStatusUpdateRequest request);

}
