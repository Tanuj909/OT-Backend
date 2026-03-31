package com.ot.dto.scheduleOperation;

import java.time.LocalDateTime;

import com.ot.enums.OperationStatus;
import com.ot.enums.ProcedureComplexity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssignedOperationResponse {
    private Long operationId;
    private String operationReference;
    private String patientName;
    private String patientMrn;
    private String procedureName;
    private ProcedureComplexity complexity;
    private OperationStatus status;
    private LocalDateTime scheduledStartTime;
    private LocalDateTime scheduledEndTime;
    private LocalDateTime actualStartTime;
    private String roomNumber;
    private String roomName;
    private String primarySurgeon;
    private String assignedRole;    // is user ka role is operation mein
}
