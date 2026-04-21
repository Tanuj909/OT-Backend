package com.ot.dto.ward;

import com.ot.enums.TaskType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateWardTaskRequest {

    private Long operationId;           // mandatory
    private Long wardAdmissionId;       // mandatory

    private TaskType taskType;          // mandatory
    private String taskDescription;     // mandatory
    private String taskNotes;           // optional

    private LocalDateTime scheduledTime; // optional — kab karna hai

    private Boolean isRecurring = false;
    private Integer intervalHours;       // null if non-recurring
    private LocalDateTime recurringEndTime; // null = discharge tak
}