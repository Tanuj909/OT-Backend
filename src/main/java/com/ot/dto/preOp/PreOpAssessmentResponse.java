package com.ot.dto.preOp;

import java.time.LocalDateTime;
import com.ot.enums.AsaGrade;
import com.ot.enums.AssessmentStatus;
import com.ot.enums.NpoStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PreOpAssessmentResponse {

    private Long id;
    private Long operationId;
    private String patientId;
    private LocalDateTime assessmentDate;
    private String assessedBy;

    private Double height;
    private Double weight;
    private Double bmi;
    private String bloodGroup;

    private String allergies;
    private String currentMedications;
    private String pastMedicalHistory;
    private String pastSurgicalHistory;
    private String physicalExamination;

    private String ecgFindings;
    private String labResults;
    private String radiologyFindings;

    private AsaGrade asaGrade;
    private NpoStatus npoStatus;

    private String anesthesiaPlan;
    private String specialInstructions;

    private AssessmentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
}