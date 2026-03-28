package com.ot.dto.surgeryResponse;

import java.time.LocalDateTime;

import com.ot.enums.OperationStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SurgeryStatusResponse {

    private Long operationId;
    private Boolean isStarted;
    private OperationStatus status;
    private LocalDateTime actualStartTime;
}