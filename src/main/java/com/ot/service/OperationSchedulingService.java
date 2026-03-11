package com.ot.service;

import com.ot.dto.scheduleOperationRequest.ScheduleOperationRequest;

public interface OperationSchedulingService {

	void schedule(Long operationId, ScheduleOperationRequest request);

}
