package com.ot.service;

import java.util.List;

import com.ot.dto.scheduleOperation.OperationListResponse;
import com.ot.dto.scheduleOperation.OperationStatusResponse;
import com.ot.dto.scheduleOperation.ScheduleOperationRequest;
import com.ot.enums.OperationStatus;

public interface OperationSchedulingService {

	void schedule(Long operationId, ScheduleOperationRequest request);

//	List<OperationListResponse> getAllOperations(OperationStatus status, Long roomId);

	List<OperationListResponse> getAllOperations();

	List<OperationListResponse> getOperationsByStatus(OperationStatus status);

	List<OperationListResponse> getRequestedOperations();

	OperationStatusResponse getOperationStatus(Long operationId);

}
