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
@Table(name = "ward_admissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WardAdmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_id", nullable = false)
    private ScheduledOperation operation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_room_id", nullable = false)
    private WardRoom wardRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_bed_id", nullable = false)
    private WardBed wardBed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    // Patient snapshot — admission ke time ka
    private String patientId;
    private String patientName;
    private String patientMrn;

    // Admission
    private LocalDateTime admissionTime;
    private String admittedBy;

    // Discharge
    private LocalDateTime dischargedWhen;
    private String dischargedBy;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}