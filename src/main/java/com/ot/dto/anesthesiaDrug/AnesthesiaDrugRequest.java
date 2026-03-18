package com.ot.dto.anesthesiaDrug;

import java.time.LocalDateTime;

import com.ot.enums.DrugType;

import lombok.Data;

@Data
public class AnesthesiaDrugRequest {
    private String drugName;
    private Double dose;
    private String doseUnit;
    private String route;
    private DrugType drugType;
    private LocalDateTime administeredAt;
    private String notes;
}