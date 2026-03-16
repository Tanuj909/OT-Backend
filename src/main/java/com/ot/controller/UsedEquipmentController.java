package com.ot.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ot.dto.equipment.UsedEquipmentRequest;
import com.ot.dto.equipment.UsedEquipmentResponse;
import com.ot.dto.response.ApiResponse;
import com.ot.service.UsedEquipmentService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/operations/{operationId}/equipment")
@RequiredArgsConstructor
public class UsedEquipmentController {

    private final UsedEquipmentService usedEquipmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<UsedEquipmentResponse>> addEquipment(
            @PathVariable Long operationId,
            @RequestBody UsedEquipmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Equipment added to operation successfully",
                        usedEquipmentService.addEquipmentToOperation(operationId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UsedEquipmentResponse>>> getUsedEquipment(
            @PathVariable Long operationId) {
        return ResponseEntity.ok(ApiResponse.success("Used equipment fetched successfully",
                usedEquipmentService.getUsedEquipment(operationId)));
    }

    @PutMapping("/{usedEquipmentId}")
    public ResponseEntity<ApiResponse<UsedEquipmentResponse>> updateUsedEquipment(
            @PathVariable Long operationId,
            @PathVariable Long usedEquipmentId,
            @RequestBody UsedEquipmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Used equipment updated successfully",
                usedEquipmentService.updateUsedEquipment(operationId, usedEquipmentId, request)));
    }

    @DeleteMapping("/{usedEquipmentId}")
    public ResponseEntity<ApiResponse<Void>> removeEquipment(
            @PathVariable Long operationId,
            @PathVariable Long usedEquipmentId) {
        usedEquipmentService.removeEquipmentFromOperation(operationId, usedEquipmentId);
        return ResponseEntity.ok(ApiResponse.success("Equipment removed from operation successfully", null));
    }
}
