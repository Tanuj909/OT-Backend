package com.ot.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ot.dto.medication.MedicationUsageRequest;
import com.ot.dto.medication.MedicationUsageResponse;
import com.ot.dto.response.ApiResponse;
import com.ot.service.MedicationUsageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/medication-usage")
@RequiredArgsConstructor
public class MedicationUsageController {

    private final MedicationUsageService medicationUsageService;

    @PostMapping("/record")
    public ResponseEntity<ApiResponse<MedicationUsageResponse>> recordUsage(
            @RequestBody MedicationUsageRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Medication usage recorded successfully",
                        medicationUsageService.recordUsage(request)));
    }
    
    @PostMapping("/{id}/update-quantity")
    public ResponseEntity<ApiResponse<MedicationUsageResponse>> updateQuantity(
            @PathVariable Long id,
            @RequestBody MedicationUsageRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Medication quantity updated successfully",
                        medicationUsageService.updateQuantity(id, request.getQuantity())
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicationUsageResponse>> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.success("Medication usage fetched successfully",
                medicationUsageService.getById(id)));
    }

    @GetMapping("/operation/{operationId}")
    public ResponseEntity<ApiResponse<List<MedicationUsageResponse>>> getByOperation(
            @PathVariable Long operationId) {

        return ResponseEntity.ok(ApiResponse.success("Medication usage fetched successfully",
                medicationUsageService.getByOperation(operationId)));
    }

    @GetMapping("/ward-room/{wardRoomId}")
    public ResponseEntity<ApiResponse<List<MedicationUsageResponse>>> getByWardRoom(
            @PathVariable Long wardRoomId) {

        return ResponseEntity.ok(ApiResponse.success("Medication usage fetched successfully",
                medicationUsageService.getByWardRoom(wardRoomId)));
    }

    @GetMapping("/ward-bed/{wardBedId}")
    public ResponseEntity<ApiResponse<List<MedicationUsageResponse>>> getByWardBed(
            @PathVariable Long wardBedId) {

        return ResponseEntity.ok(ApiResponse.success("Medication usage fetched successfully",
                medicationUsageService.getByWardBed(wardBedId)));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {

        medicationUsageService.deleteById(id);

        return ResponseEntity.ok(
                ApiResponse.success("Medication usage deleted successfully", null)
        );
    }
}