package com.ot.dto.anesthesiaDrug;

import java.time.LocalDateTime;

import com.ot.enums.DrugType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnesthesiaDrugResponse {
    private Long id;
    private Long intraOpId;
    private String drugName;
    private Double dose;
    private String doseUnit;
    private String route;
    private DrugType drugType;
    private LocalDateTime administeredAt;
    private LocalDateTime endTime;      // 👈 NEW
    private String administeredBy;
    private String notes;
    private LocalDateTime createdAt;
}
