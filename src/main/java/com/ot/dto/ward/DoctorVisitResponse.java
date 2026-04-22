package com.ot.dto.ward;

import com.ot.enums.DoctorVisitStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class DoctorVisitResponse {

    private Long   id;

    // ── Links ──────────────────────────────────────────────────────────────
    private Long   operationId;
    private Long   wardAdmissionId;

    // ── Patient ────────────────────────────────────────────────────────────
    private String patientId;
    private String patientName;
    private String patientMrn;

    // ── Visit Info ─────────────────────────────────────────────────────────
    private LocalDateTime visitTime;

    private Long   doctorId;
    private String doctorName;
    private String doctorSpecialization;

    private Long   recordedById;
    private String recordedByName;

    // ── Clinical ───────────────────────────────────────────────────────────
    private String clinicalObservations;
    private String diagnosis;
    private String treatmentPlan;

    // ── Medications ────────────────────────────────────────────────────────
    private Boolean hasMedicationChange;
    private String  medicationsAdded;
    private String  medicationsDiscontinued;
    private String  medicationNotes;

    // ── Next Visit ─────────────────────────────────────────────────────────
    private LocalDateTime nextVisitScheduled;
    private String        nextVisitInstructions;

    // ── Discharge ──────────────────────────────────────────────────────────
    private Boolean       dischargeRecommended;
    private String        dischargeNotes;
    private LocalDateTime expectedDischargeDate;

    // ── Status & Audit ─────────────────────────────────────────────────────
    private DoctorVisitStatus status;
    private LocalDateTime     createdAt;
    private LocalDateTime     updatedAt;
}