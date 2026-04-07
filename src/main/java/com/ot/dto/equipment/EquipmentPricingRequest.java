package com.ot.dto.equipment;


import com.ot.enums.PricingType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EquipmentPricingRequest {

    private Long equipmentId;

    private PricingType pricingType;

    private Double rate;

    private String unit; // PER_HOUR, PER_USE, etc.

    private Boolean isActive;

    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;
}