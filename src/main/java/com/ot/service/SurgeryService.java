package com.ot.service;

import com.ot.dto.surgeryResponse.SurgeryStartResponse;
import com.ot.dto.surgeryResponse.SurgeryStatusResponse;

public interface SurgeryService {

	SurgeryStartResponse startSurgery(Long operationId);

	SurgeryStatusResponse checkSurgeryStarted(Long operationId);

}
