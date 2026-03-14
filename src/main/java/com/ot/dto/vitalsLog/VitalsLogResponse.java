package com.ot.dto.vitalsLog;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VitalsLogResponse {
    private Long id;
    private Long operationId;
    private LocalDateTime recordedTime;
    private String recordedBy;
    private Integer heartRate;
    private Integer systolicBp;
    private Integer diastolicBp;
    private Integer meanBp;
    private Integer respiratoryRate;
    private Double temperature;
    private Integer oxygenSaturation;
    private Integer etco2;
    private Integer painScale;
    private String consciousness;
    private String sedationScore;
    private String additionalNotes;
    private LocalDateTime createdAt;
}