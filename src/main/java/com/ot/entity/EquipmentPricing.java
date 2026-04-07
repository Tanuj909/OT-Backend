package com.ot.entity;

import java.time.LocalDateTime;

import com.ot.enums.PricingType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "equipment_pricing")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentPricing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Equipment equipment;

    @Enumerated(EnumType.STRING)
    private PricingType pricingType;

    private Double rate; // amount

    private String unit; 
    // Example: "PER_HOUR", "PER_USE", "FIXED"

    private Boolean isActive;

    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;

    private String createdBy;
}