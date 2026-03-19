package com.ot.dto.surgeryEnd;

import java.time.LocalDateTime;
import com.ot.enums.OperationStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SurgeryEndResponse {
    private Long operationId;
    private OperationStatus operationStatus;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private Long surgeryDurationMinutes;  // calculated
    private String endedBy;
    private Long postOpRecordId;          // auto created
}
