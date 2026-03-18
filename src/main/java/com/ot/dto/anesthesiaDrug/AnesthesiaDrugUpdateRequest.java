package com.ot.dto.anesthesiaDrug;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AnesthesiaDrugUpdateRequest {
    private Double dose;
    private String doseUnit;
    private String route;
    private LocalDateTime administeredAt;
    private String notes;
}