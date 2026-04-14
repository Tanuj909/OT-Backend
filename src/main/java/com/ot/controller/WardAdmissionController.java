package com.ot.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ot.dto.response.ApiResponse;
import com.ot.dto.ward.AssignWardRequest;
import com.ot.dto.ward.WardAdmissionResponse;
import com.ot.service.WardAdmissionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ward-admissions")
@RequiredArgsConstructor
public class WardAdmissionController {

    private final WardAdmissionService wardAdmissionService;

    // Operation ko room + bed assign karo → bed OCCUPIED + admission record create
    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<WardAdmissionResponse>> assignWard(
            @RequestBody AssignWardRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ward assigned successfully",
                        wardAdmissionService.assignWard(request)));
    }

    // Discharge → bed AVAILABLE + admission record close
    @PatchMapping("/discharge/operation/{operationId}")
    public ResponseEntity<ApiResponse<WardAdmissionResponse>> discharge(
            @PathVariable Long operationId) {

        return ResponseEntity.ok(ApiResponse.success("Patient discharged successfully",
                wardAdmissionService.discharge(operationId)));
    }

    // Operation ka active admission
    @GetMapping("/operation/{operationId}/active")
    public ResponseEntity<ApiResponse<WardAdmissionResponse>> getActiveByOperation(
            @PathVariable Long operationId) {

        return ResponseEntity.ok(ApiResponse.success("Active admission fetched successfully",
                wardAdmissionService.getActiveByOperation(operationId)));
    }

    // Patient ki poori ward history
    @GetMapping("/patient/{patientId}/history")
    public ResponseEntity<ApiResponse<List<WardAdmissionResponse>>> getByPatient(
            @PathVariable String patientId) {

        return ResponseEntity.ok(ApiResponse.success("Patient admission history fetched successfully",
                wardAdmissionService.getByPatient(patientId)));
    }

    // Room ki poori history
    @GetMapping("/room/{wardRoomId}/history")
    public ResponseEntity<ApiResponse<List<WardAdmissionResponse>>> getByRoom(
            @PathVariable Long wardRoomId) {

        return ResponseEntity.ok(ApiResponse.success("Room admission history fetched successfully",
                wardAdmissionService.getByRoom(wardRoomId)));
    }

    // Bed ki poori history
    @GetMapping("/bed/{wardBedId}/history")
    public ResponseEntity<ApiResponse<List<WardAdmissionResponse>>> getByBed(
            @PathVariable Long wardBedId) {

        return ResponseEntity.ok(ApiResponse.success("Bed admission history fetched successfully",
                wardAdmissionService.getByBed(wardBedId)));
    }
    
    // Patient ward mein hai ya nahi — Frontend check
    @GetMapping("/patient/{patientId}/is-admitted")
    public ResponseEntity<ApiResponse<Boolean>> isPatientAdmitted(
            @PathVariable String patientId) {
 
        return ResponseEntity.ok(ApiResponse.success("Patient admission status fetched",
                wardAdmissionService.isPatientAdmitted(patientId)));
    }
    
    // Patient ward mein hai ya nahi — Frontend check --> Through Operation Id check
    @GetMapping("/operation/{operationId}/is-admitted")
    public ResponseEntity<ApiResponse<Boolean>> isOperationAdmitted(
            @PathVariable Long operationId) {
 
        return ResponseEntity.ok(ApiResponse.success("Operation admission status fetched",
                wardAdmissionService.isOperationAdmitted(operationId)));
    }
}