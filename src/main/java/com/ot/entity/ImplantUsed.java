package com.ot.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "implants_used")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImplantUsed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_id", nullable = false)
    private ScheduledOperation scheduledOperation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalog_item_id", nullable = false)
    private OTItemCatalog catalogItem;  // OTItemCatalog se link

    private String serialNumber;    // Specific implant ka unique ID
    private String batchNumber;     // Traceability ke liye
    private Integer quantity;
    private String bodyLocation;    // "Left Femur", "L4-L5"
    private String notes;

    private String usedBy;
    private LocalDateTime usedAt;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (usedAt == null) usedAt = LocalDateTime.now();
    }
}