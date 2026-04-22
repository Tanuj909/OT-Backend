package com.ot.dto.ward;

import com.ot.enums.DoctorVisitStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UpdateDoctorVisitRequest {

    // ── Visit Info (optional update) ───────────────────────────────────────
    private LocalDateTime visitTime;
    private String        doctorSpecialization;

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

    // ── Status ─────────────────────────────────────────────────────────────
    private DoctorVisitStatus status;
}