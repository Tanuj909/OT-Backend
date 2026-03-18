package com.ot.entity;

import java.time.LocalDateTime;

import com.ot.enums.CatalogItemType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ot_item_catalog")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OTItemCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    private String itemCode;          // "IMP-001", "CON-001", "FLU-001"
    private String itemName;          // "Titanium Plate", "Surgical Gloves"

    @Enumerated(EnumType.STRING)
    private CatalogItemType itemType; // IMPLANT, CONSUMABLE, IV_FLUID, DRUG, EQUIPMENT

    private String category;          // "Orthopedic", "PPE", "Anesthesia"
    private String subCategory;       // "Bone Plates", "Gloves", "Induction"
    private String manufacturer;      // "Stryker", "BD", "Baxter"
    private String modelNumber;
    private String description;
    private String unit;              // "per piece", "per ml", "per pair"

    private Boolean isActive;

    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        isActive = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}