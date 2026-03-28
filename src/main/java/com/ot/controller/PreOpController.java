package com.ot.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ot.dto.preOp.PreOpAssessmentRequest;
import com.ot.dto.preOp.PreOpAssessmentResponse;
import com.ot.dto.preOp.PreOpStatusResponse;
import com.ot.dto.preOp.PreOpStatusUpdateRequest;
import com.ot.dto.response.ApiResponse;
import com.ot.service.PreOpService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/operations")
@RequiredArgsConstructor
public class PreOpController {

    private final PreOpService preOpService;

    @PostMapping("/{operationId}/pre-op")
    public ResponseEntity<ApiResponse<PreOpAssessmentResponse>> createPreOpAssessment(
            @PathVariable Long operationId,
            @RequestBody PreOpAssessmentRequest request) {

        PreOpAssessmentResponse response = preOpService.createPreOpAssessment(operationId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Pre-op assessment created successfully", response));
    }

    @GetMapping("/{operationId}/pre-op")
    public ResponseEntity<ApiResponse<PreOpAssessmentResponse>> getPreOpAssessment(
            @PathVariable Long operationId) {

        PreOpAssessmentResponse response = preOpService.getPreOpAssessment(operationId);
        return ResponseEntity.ok(ApiResponse.success("Pre-op assessment fetched successfully", response));
    }
    
    @GetMapping("/{operationId}/pre-op/status")
    public ResponseEntity<ApiResponse<PreOpStatusResponse>> getPreOpStatus(
            @PathVariable Long operationId) {

        PreOpStatusResponse response = preOpService.getPreOpStatus(operationId);

        return ResponseEntity.ok(
                ApiResponse.success("Pre-op status fetched successfully", response)
        );
    }

    @PutMapping("/{operationId}/pre-op")
    public ResponseEntity<ApiResponse<PreOpAssessmentResponse>> updatePreOpAssessment(
            @PathVariable Long operationId,
            @RequestBody PreOpAssessmentRequest request) {

        PreOpAssessmentResponse response = preOpService.updatePreOpAssessment(operationId, request);
        return ResponseEntity.ok(ApiResponse.success("Pre-op assessment updated successfully", response));
    }
    
    @PatchMapping("/{operationId}/pre-op/status")
    public ResponseEntity<ApiResponse<PreOpAssessmentResponse>> updatePreOpStatus(
            @PathVariable Long operationId,
            @RequestBody PreOpStatusUpdateRequest request) {

        PreOpAssessmentResponse response = preOpService.updatePreOpStatus(operationId, request);
        return ResponseEntity.ok(ApiResponse.success("Pre-op status updated successfully", response));
    }
}