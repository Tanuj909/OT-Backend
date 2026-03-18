package com.ot.service;

import com.ot.dto.scheduleOperation.ScheduleOperationRequest;

public interface OperationSchedulingService {

	void schedule(Long operationId, ScheduleOperationRequest request);

}
