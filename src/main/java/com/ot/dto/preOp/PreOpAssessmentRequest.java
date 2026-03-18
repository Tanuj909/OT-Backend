package com.ot.dto.preOp;

import java.time.LocalDateTime;

import com.ot.enums.AsaGrade;
import com.ot.enums.AssessmentStatus;
import com.ot.enums.NpoStatus;

import lombok.Data;

@Data
public class PreOpAssessmentRequest {

    private String patientId;
    private LocalDateTime assessmentDate;
    private String assessedBy;

    private Double height;
    private Double weight;
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
    private AssessmentStatus status;

    private String anesthesiaPlan;
    private String specialInstructions;
}
