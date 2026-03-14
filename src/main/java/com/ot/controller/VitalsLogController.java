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
import com.ot.dto.response.ApiResponse;
import com.ot.dto.vitalsLog.VitalsLogBulkRequest;
import com.ot.dto.vitalsLog.VitalsLogRequest;
import com.ot.dto.vitalsLog.VitalsLogResponse;
import com.ot.service.VitalsLogService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/operations")
@RequiredArgsConstructor
public class VitalsLogController {

    private final VitalsLogService vitalsLogService;

    @PostMapping("/{operationId}/vitals")
    public ResponseEntity<ApiResponse<VitalsLogResponse>> addVitals(
            @PathVariable Long operationId,
            @RequestBody VitalsLogRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vitals recorded successfully",
                        vitalsLogService.addVitals(operationId, request)));
    }
    
    @PostMapping("/{operationId}/vitals/bulk")
    public ResponseEntity<ApiResponse<List<VitalsLogResponse>>> addBulkVitals(
            @PathVariable Long operationId,
            @RequestBody VitalsLogBulkRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bulk vitals recorded successfully",
                        vitalsLogService.addBulkVitals(operationId, request)));
    }

    @GetMapping("/{operationId}/vitals")
    public ResponseEntity<ApiResponse<List<VitalsLogResponse>>> getVitals(
            @PathVariable Long operationId) {

        return ResponseEntity.ok(ApiResponse.success("Vitals fetched successfully",
                vitalsLogService.getVitals(operationId)));
    }

    @GetMapping("/{operationId}/vitals/latest")
    public ResponseEntity<ApiResponse<VitalsLogResponse>> getLatestVitals(
            @PathVariable Long operationId) {

        return ResponseEntity.ok(ApiResponse.success("Latest vitals fetched successfully",
                vitalsLogService.getLatestVitals(operationId)));
    }

    @DeleteMapping("/{operationId}/vitals/{vitalsId}")
    public ResponseEntity<ApiResponse<Void>> deleteVitals(
            @PathVariable Long operationId,
            @PathVariable Long vitalsId) {

        vitalsLogService.deleteVitals(operationId, vitalsId);
        return ResponseEntity.ok(ApiResponse.success("Vitals deleted successfully", null));
    }
}