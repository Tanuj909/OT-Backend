package com.ot.dto.vitalsLog;

import lombok.Data;

@Data
public class VitalsLogRequest {
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
}