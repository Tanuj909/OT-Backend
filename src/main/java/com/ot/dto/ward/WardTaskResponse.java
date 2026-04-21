package com.ot.dto.ward;

import com.ot.enums.TaskStatus;
import com.ot.enums.TaskType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class WardTaskResponse {

    private Long id;

    // Context
    private Long operationId;
    private Long wardAdmissionId;
    private String patientId;
    private String patientName;
    private String patientMrn;

    // Task info
    private TaskType taskType;
    private String taskDescription;
    private String taskNotes;
    private LocalDateTime scheduledTime;

    // Recurring
    private Boolean isRecurring;
    private Integer intervalHours;
    private LocalDateTime recurringEndTime;

    // Assigned by
    private Long assignedById;
    private String assignedByName;
    private LocalDateTime assignedAt;

    // Status
    private TaskStatus status;

    // Completion
    private Long completedById;
    private String completedByName;
    private LocalDateTime completedAt;
    private String completionNotes;
    private String readingValue;
    private String readingUnit;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}