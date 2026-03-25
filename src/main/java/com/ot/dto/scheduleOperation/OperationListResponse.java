package com.ot.dto.scheduleOperation;

import java.time.LocalDateTime;

import com.ot.enums.OperationStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OperationListResponse {

    private Long operationId;
    private String operationReference;

    private String patientName;
    private String patientMrn;

    private String procedureName;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private OperationStatus status;

    private Long roomId;
    private String roomName;

    private String primarySurgeonName;
}