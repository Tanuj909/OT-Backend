package com.ot.dto.wardVitals;

import java.time.LocalDateTime;

import com.ot.enums.VitalsPhase;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WardVitalsResponse {
    private Long id;
    private Long operationId;
    private Long wardId;
    private String wardName;
    private VitalsPhase phase;
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
    private Boolean isStable;
    private String additionalNotes;
    private LocalDateTime createdAt;
}
