package com.ot.dto.equipment;

import com.ot.enums.PricingType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EquipmentPricingResponse {

    private Long id;

    private Long equipmentId;
    private String equipmentName;

    private PricingType pricingType;

    private Double rate;
    private String unit;

    private Boolean isActive;

    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;
}