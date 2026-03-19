package com.ot.entity;

import java.time.LocalDateTime;

import com.ot.enums.WardType;

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
@Table(name = "wards")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    private String wardNumber;      // "Ward 4B", "ICU-1"
    private String wardName;        // "General Ward", "Intensive Care Unit"

    @Enumerated(EnumType.STRING)
    private WardType wardType;      // GENERAL, ICU, HDU, PRIVATE, SEMI_PRIVATE

    private Integer totalBeds;
    private Integer occupiedBeds;   // auto managed
    private Integer availableBeds;  // calculated

    private Boolean isActive;

    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        isActive = true;
        occupiedBeds = 0;
        availableBeds = totalBeds;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        availableBeds = totalBeds - occupiedBeds;
    }
}