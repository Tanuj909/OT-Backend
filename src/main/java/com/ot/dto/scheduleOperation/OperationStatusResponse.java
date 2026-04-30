package com.ot.dto.scheduleOperation;

import java.time.LocalDateTime;

import com.ot.enums.OperationStatus;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OperationStatusResponse {

    private Long operationId;
    private OperationStatus status;
    private Boolean isScheduled;
    private Boolean isStarted;
    private Boolean isCompleted;
    private String transferStatus; 
    private String transferredTo; 
    private LocalDateTime scheduledStartTime;
    private LocalDateTime actualStartTime;
}