package com.ot.dto.ward;

import com.ot.enums.DoctorVisitStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateDoctorVisitRequest {

    // ── Required ───────────────────────────────────────────────────────────
    @NotNull(message = "operationId is required")
    private Long operationId;

    // ── Doctor Info ────────────────────────────────────────────────────────
    @NotNull(message = "doctorId is required")
    private Long   doctorId;

    @NotNull(message = "doctorName is required")
    private String doctorName;

    private String doctorSpecialization;

    // ── Visit Info ─────────────────────────────────────────────────────────
    private LocalDateTime visitTime;          // null hoga toh now() use hoga

    // ── Clinical Observations ──────────────────────────────────────────────
    private String clinicalObservations;
    private String diagnosis;
    private String treatmentPlan;

    // ── Medication Changes ─────────────────────────────────────────────────
    private Boolean hasMedicationChange = false;
    private String  medicationsAdded;
    private String  medicationsDiscontinued;
    private String  medicationNotes;

    // ── Next Visit ─────────────────────────────────────────────────────────
    private LocalDateTime nextVisitScheduled;
    private String        nextVisitInstructions;

    // ── Discharge Recommendation ───────────────────────────────────────────
    private Boolean       dischargeRecommended = false;
    private String        dischargeNotes;
    private LocalDateTime expectedDischargeDate;

    // ── Status ─────────────────────────────────────────────────────────────
    // Default = COMPLETED (visit ho gayi), SCHEDULED bhi pass kar sakte ho
    private DoctorVisitStatus status;
}