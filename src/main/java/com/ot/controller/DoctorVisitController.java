package com.ot.controller;

import com.ot.dto.ward.CreateDoctorVisitRequest;
import com.ot.dto.ward.DoctorVisitResponse;
import com.ot.dto.ward.UpdateDoctorVisitRequest;
import com.ot.enums.DoctorVisitStatus;
import com.ot.service.DoctorVisitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctor-visits")
@RequiredArgsConstructor
public class DoctorVisitController {

    private final DoctorVisitService doctorVisitService;

    // ── Create ─────────────────────────────────────────────────────────────

    /**
     * POST /api/doctor-visits
     * Doctor ya Nurse visit record karo
     */
    @PostMapping
    public ResponseEntity<DoctorVisitResponse> createVisit(
            @Valid @RequestBody CreateDoctorVisitRequest request) {
        return ResponseEntity.ok(doctorVisitService.createVisit(request));
    }

    /**
     * PUT /api/doctor-visits/{visitId}/complete
     * Scheduled visit ko complete karo
     */
    @PutMapping("/{visitId}/complete")
    public ResponseEntity<DoctorVisitResponse> completeVisit(
            @PathVariable Long visitId) {
        return ResponseEntity.ok(doctorVisitService.completeVisit(visitId));
    }
    
    // ── Update ─────────────────────────────────────────────────────────────

    /**
     * PUT /api/doctor-visits/{visitId}
     * Visit update karo (notes, medication, discharge recommendation etc.)
     */
    @PutMapping("/{visitId}")
    public ResponseEntity<DoctorVisitResponse> updateVisit(
            @PathVariable Long visitId,
            @RequestBody UpdateDoctorVisitRequest request) {
        return ResponseEntity.ok(doctorVisitService.updateVisit(visitId, request));
    }

    // ── Cancel ─────────────────────────────────────────────────────────────

    /**
     * PUT /api/doctor-visits/{visitId}/cancel
     * Scheduled visit cancel karo
     */
    @PutMapping("/{visitId}/cancel")
    public ResponseEntity<DoctorVisitResponse> cancelVisit(
            @PathVariable Long visitId) {
        return ResponseEntity.ok(doctorVisitService.cancelVisit(visitId));
    }

    // ── Get Single ─────────────────────────────────────────────────────────

    /**
     * GET /api/doctor-visits/{visitId}
     */
    @GetMapping("/{visitId}")
    public ResponseEntity<DoctorVisitResponse> getById(
            @PathVariable Long visitId) {
        return ResponseEntity.ok(doctorVisitService.getById(visitId));
    }

    // ── Get By Operation ───────────────────────────────────────────────────

    /**
     * GET /api/doctor-visits/operation/{operationId}
     * Operation ke saare visits (latest first)
     */
    @GetMapping("/operation/{operationId}")
    public ResponseEntity<List<DoctorVisitResponse>> getByOperation(
            @PathVariable Long operationId) {
        return ResponseEntity.ok(doctorVisitService.getByOperation(operationId));
    }

    /**
     * GET /api/doctor-visits/operation/{operationId}/latest
     * Operation ki sabse latest visit
     */
    @GetMapping("/operation/{operationId}/latest")
    public ResponseEntity<DoctorVisitResponse> getLatestVisit(
            @PathVariable Long operationId) {
        return ResponseEntity.ok(doctorVisitService.getLatestVisit(operationId));
    }

    /**
     * GET /api/doctor-visits/operation/{operationId}/status?status=SCHEDULED
     * Operation + status filter
     */
    @GetMapping("/operation/{operationId}/status")
    public ResponseEntity<List<DoctorVisitResponse>> getByOperationAndStatus(
            @PathVariable Long operationId,
            @RequestParam DoctorVisitStatus status) {
        return ResponseEntity.ok(doctorVisitService.getByOperationAndStatus(operationId, status));
    }

    /**
     * GET /api/doctor-visits/operation/{operationId}/discharge-recommended
     * Kisi visit mein doctor ne discharge recommend kiya hai kya?
     */
    @GetMapping("/operation/{operationId}/discharge-recommended")
    public ResponseEntity<Boolean> isDischargeRecommended(
            @PathVariable Long operationId) {
        return ResponseEntity.ok(doctorVisitService.isDischargeRecommended(operationId));
    }

    // ── Get By Admission ───────────────────────────────────────────────────

    /**
     * GET /api/doctor-visits/admission/{wardAdmissionId}
     * Ward admission ke saare visits (latest first)
     */
    @GetMapping("/admission/{wardAdmissionId}")
    public ResponseEntity<List<DoctorVisitResponse>> getByAdmission(
            @PathVariable Long wardAdmissionId) {
        return ResponseEntity.ok(doctorVisitService.getByAdmission(wardAdmissionId));
    }
}