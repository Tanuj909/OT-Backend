package com.ot.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ot.dto.response.ApiResponse;
import com.ot.dto.surgeryEnd.SurgeryEndRequest;
import com.ot.dto.surgeryEnd.SurgeryEndResponse;
import com.ot.dto.surgeryEnd.SurgeryReadinessResponse;
import com.ot.service.SurgeryEndService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/operations")
@RequiredArgsConstructor
public class SurgeryEndController {

    private final SurgeryEndService surgeryEndService;
    
    @GetMapping("/{operationId}/surgery-readiness")
    public ResponseEntity<ApiResponse<SurgeryReadinessResponse>> getSurgeryReadiness(
            @PathVariable Long operationId) {

        return ResponseEntity.ok(ApiResponse.success("Surgery readiness fetched successfully",
        		surgeryEndService.getSurgeryReadiness(operationId)));
    }

    // Surgery End
    @PatchMapping("/{operationId}/end")
    public ResponseEntity<ApiResponse<SurgeryEndResponse>> endSurgery(
            @PathVariable Long operationId,
            @RequestBody SurgeryEndRequest request) {

        return ResponseEntity.ok(ApiResponse.success("Surgery ended successfully",
                surgeryEndService.endSurgery(operationId, request)));
    }
    


}