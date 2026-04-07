package com.ot.entity;

import java.time.LocalDateTime;

import com.ot.enums.VolumeUnit;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "iv_fluid_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"hospital", "intraOpRecord"})
@ToString(exclude = {"hospital", "intraOpRecord"})
public class IVFluidRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intra_op_id", nullable = false)
    private IntraOpRecord intraOpRecord;
    
    private Long billingItemId;

    private String fluidType;       // Normal Saline, Ringer Lactate, etc.
    private Integer volume;

    @Enumerated(EnumType.STRING)
    private VolumeUnit unit;        // ML, LITERS

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String administeredBy;  // security se auto set

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
