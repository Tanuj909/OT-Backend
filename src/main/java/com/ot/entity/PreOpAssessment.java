package com.ot.entity;

import lombok.*;
import java.time.LocalDateTime;

import com.ot.enums.AsaGrade;
import com.ot.enums.AssessmentStatus;
import com.ot.enums.NpoStatus;

import jakarta.persistence.*;

@Entity
@Table(name = "pre_op_assessments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"hospital", "scheduledOperation"})
@ToString(exclude = {"hospital", "scheduledOperation"})
public class PreOpAssessment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_id")
    private ScheduledOperation scheduledOperation;
    
    private String patientId;
    private LocalDateTime assessmentDate;
    private String assessedBy;
    
    private Double height;
    private Double weight;
    private Double bmi;
    private String bloodGroup;
    
    @Column(length = 500)
    private String allergies;
    
    @Column(length = 500)
    private String currentMedications;
    
    @Column(length = 1000)
    private String pastMedicalHistory;
    
    @Column(length = 1000)
    private String pastSurgicalHistory;
    
    @Column(length = 1000)
    private String physicalExamination;
    
    private String ecgFindings;
    private String labResults;
    private String radiologyFindings;
    
    @Enumerated(EnumType.STRING)
    private AsaGrade asaGrade;
    
    @Enumerated(EnumType.STRING)
    private NpoStatus npoStatus;
    
    @Column(length = 1000)
    private String anesthesiaPlan;
    
    private String specialInstructions;

    private String statusChangeReason;
    
    @Enumerated(EnumType.STRING)
    private AssessmentStatus status;

    // 🔥 NEW FIELDS (Added)
    private Boolean fitForSurgery;

    @Column(length = 1000)
    private String clearanceRemarks;

    @Column(length = 500)
    private String airwayAssessment;

    private Boolean consentTaken;

    private Boolean highRisk;

    private Boolean checklistCompleted;

    private String approvedBy;

    private LocalDateTime approvedAt;

    private LocalDateTime validTill;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}