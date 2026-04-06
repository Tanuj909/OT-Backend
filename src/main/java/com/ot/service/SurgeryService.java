package com.ot.service;

import java.util.List;

import com.ot.dto.scheduleOperation.AssignedOperationResponse;
import com.ot.dto.surgeryResponse.SurgeryStartResponse;
import com.ot.dto.surgeryResponse.SurgeryStatusResponse;

public interface SurgeryService {

	SurgeryStartResponse startSurgery(Long operationId);

	SurgeryStatusResponse checkSurgeryStarted(Long operationId);

	void shiftRoomBeforeSurgery(Long operationId, Long newRoomId);


}
