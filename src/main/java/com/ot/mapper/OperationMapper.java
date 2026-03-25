package com.ot.mapper;

import com.ot.dto.scheduleOperation.OperationListResponse;
import com.ot.entity.ScheduledOperation;

public class OperationMapper {
	
	public static OperationListResponse toListResponse(ScheduledOperation op) {

	    return OperationListResponse.builder()
	            .operationId(op.getId())
	            .operationReference(op.getOperationReference())
	            .patientName(op.getPatientName())
	            .patientMrn(op.getPatientMrn())
	            .procedureName(op.getProcedureName())
	            .startTime(op.getScheduledStartTime())
	            .endTime(op.getScheduledEndTime())
	            .status(op.getStatus())
	            .roomId(op.getRoom() != null ? op.getRoom().getId() : null)
	            .roomName(op.getRoom() != null ? op.getRoom().getRoomName() : null)
	            .primarySurgeonName(op.getPrimarySurgeonName())
	            .build();
	}

}
