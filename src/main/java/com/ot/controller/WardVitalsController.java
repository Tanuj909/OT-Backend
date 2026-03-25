package com.ot.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ot.dto.response.ApiResponse;
import com.ot.dto.wardVitals.WardVitalsRequest;
import com.ot.dto.wardVitals.WardVitalsResponse;
import com.ot.service.WardVitalsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/operations")
@RequiredArgsConstructor
public class WardVitalsController {

    private final WardVitalsService wardVitalsService;

    @PostMapping("/{operationId}/ward-vitals")
    public ResponseEntity<ApiResponse<WardVitalsResponse>> recordVitals(
            @PathVariable Long operationId,
            @RequestBody WardVitalsRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ward vitals recorded successfully",
                        wardVitalsService.recordVitals(operationId, request)));
    }

    @GetMapping("/{operationId}/ward-vitals")
    public ResponseEntity<ApiResponse<List<WardVitalsResponse>>> getWardVitals(
            @PathVariable Long operationId) {

        return ResponseEntity.ok(ApiResponse.success("Ward vitals fetched successfully",
                wardVitalsService.getWardVitals(operationId)));
    }

    @GetMapping("/{operationId}/ward-vitals/latest")
    public ResponseEntity<ApiResponse<WardVitalsResponse>> getLatestVitals(
            @PathVariable Long operationId) {

        return ResponseEntity.ok(ApiResponse.success("Latest vitals fetched successfully",
                wardVitalsService.getLatestVitals(operationId)));
    }

    @GetMapping("/{operationId}/ward-vitals/stable")
    public ResponseEntity<ApiResponse<Boolean>> isPatientStable(
            @PathVariable Long operationId) {

        return ResponseEntity.ok(ApiResponse.success("Patient stability checked",
                wardVitalsService.isPatientStable(operationId)));
    }
}