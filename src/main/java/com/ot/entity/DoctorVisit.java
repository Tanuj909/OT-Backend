package com.ot.entity;

import com.ot.enums.DoctorVisitStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "doctor_visits")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"scheduledOperation", "wardAdmission", "hospital"})
@ToString(exclude = {"scheduledOperation", "wardAdmission", "hospital"})
public class DoctorVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Links ──────────────────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_id", nullable = false)
    private ScheduledOperation scheduledOperation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_admission_id", nullable = false)
    private WardAdmission wardAdmission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    // ── Patient Snapshot (at visit time) ──────────────────────────────────
    private String patientId;
    private String patientName;
    private String patientMrn;

    // ── Visit Info ────────────────────────────────────────────────────────
    private LocalDateTime visitTime;          // Actual visit time

    // Doctor who visited
    private Long   doctorId;
    private String doctorName;
    private String doctorSpecialization;

    // Who recorded this entry (could be nurse on behalf)
    private Long   recordedById;
    private String recordedByName;

    // ── Clinical Observations ─────────────────────────────────────────────
    @Column(length = 2000)
    private String clinicalObservations;      // General observations

    @Column(length = 2000)
    private String diagnosis;                 // Updated diagnosis if any

    @Column(length = 2000)
    private String treatmentPlan;             // Plan going forward

    // ── Medication Changes ────────────────────────────────────────────────
    @Builder.Default
    private Boolean hasMedicationChange = false;

    @Column(length = 2000)
    private String medicationsAdded;          // New meds prescribed (free text / JSON)

    @Column(length = 2000)
    private String medicationsDiscontinued;   // Stopped meds

    @Column(length = 2000)
    private String medicationNotes;           // Any dosage change notes

    // ── Next Visit Schedule ───────────────────────────────────────────────
    private LocalDateTime nextVisitScheduled; // When doctor wants to visit next

    @Column(length = 500)
    private String nextVisitInstructions;     // Instructions for next visit / nursing staff

    // ── Discharge Recommendation ──────────────────────────────────────────
    @Builder.Default
    private Boolean dischargeRecommended = false;

    @Column(length = 1000)
    private String dischargeNotes;            // Condition for discharge / instructions

    private LocalDateTime expectedDischargeDate;

    // ── Status ────────────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DoctorVisitStatus status = DoctorVisitStatus.COMPLETED;
    // COMPLETED = visit ho gayi, SCHEDULED = future visit

    // ── Audit ─────────────────────────────────────────────────────────────
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (visitTime == null) visitTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}