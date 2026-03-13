package com.ot.service;

import com.ot.dto.preOpRequest.PreOpAssessmentRequest;
import com.ot.dto.preOpRequest.PreOpStatusUpdateRequest;
import com.ot.dto.preOpResponse.PreOpAssessmentResponse;

public interface PreOpService {

	PreOpAssessmentResponse getPreOpAssessment(Long operationId);

	PreOpAssessmentResponse createPreOpAssessment(Long operationId, PreOpAssessmentRequest request);

	PreOpAssessmentResponse updatePreOpAssessment(Long operationId, PreOpAssessmentRequest request);

	PreOpAssessmentResponse updatePreOpStatus(Long operationId, PreOpStatusUpdateRequest request);

}
