package com.ot.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ot.dto.equipment.EquipmentRequest;
import com.ot.dto.equipment.EquipmentResponse;
import com.ot.dto.response.ApiResponse;
import com.ot.enums.EquipmentStatus;
import com.ot.service.EquipmentService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<EquipmentResponse>> addEquipment(
            @RequestBody EquipmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Equipment added successfully", equipmentService.addEquipment(request)));
    }

    @GetMapping("/{equipmentId}")
    public ResponseEntity<ApiResponse<EquipmentResponse>> getEquipment(
            @PathVariable Long equipmentId) {
        return ResponseEntity.ok(ApiResponse.success("Equipment fetched successfully", equipmentService.getEquipment(equipmentId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EquipmentResponse>>> getAllEquipment() {
        return ResponseEntity.ok(ApiResponse.success("Equipment list fetched successfully", equipmentService.getAllEquipment()));
    }

    @PutMapping("/{equipmentId}")
    public ResponseEntity<ApiResponse<EquipmentResponse>> updateEquipment(
            @PathVariable Long equipmentId,
            @RequestBody EquipmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Equipment updated successfully", equipmentService.updateEquipment(equipmentId, request)));
    }

    @DeleteMapping("/{equipmentId}")
    public ResponseEntity<ApiResponse<Void>> deleteEquipment(
            @PathVariable Long equipmentId) {
        equipmentService.deleteEquipment(equipmentId);
        return ResponseEntity.ok(ApiResponse.success("Equipment deleted successfully", null));
    }

    @PatchMapping("/{equipmentId}/status")
    public ResponseEntity<ApiResponse<EquipmentResponse>> updateEquipmentStatus(
            @PathVariable Long equipmentId,
            @RequestParam EquipmentStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Equipment status updated successfully", equipmentService.updateEquipmentStatus(equipmentId, status)));
    }
}