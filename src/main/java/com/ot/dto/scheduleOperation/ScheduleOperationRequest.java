package com.ot.dto.scheduleOperation;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ScheduleOperationRequest {

    private Long roomId;

    private Long surgeonId;
    private String surgeonName;

    private Long anesthesiologistId;
    private String anesthesiologistName;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

}