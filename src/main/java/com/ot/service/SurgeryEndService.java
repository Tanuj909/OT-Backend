package com.ot.service;

import com.ot.dto.surgeryEnd.SurgeryEndRequest;
import com.ot.dto.surgeryEnd.SurgeryEndResponse;
import com.ot.dto.surgeryEnd.SurgeryReadinessResponse;

public interface SurgeryEndService {
    SurgeryEndResponse endSurgery(Long operationId, SurgeryEndRequest request);

	SurgeryReadinessResponse getSurgeryReadiness(Long operationId);
}
