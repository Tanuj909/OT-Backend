package com.ot.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ot.dto.response.ApiResponse;
import com.ot.dto.scheduleOperation.AssignedOperationResponse;
import com.ot.dto.scheduleOperation.OperationListResponse;
import com.ot.dto.scheduleOperation.OperationStatusResponse;
import com.ot.dto.scheduleOperation.ScheduleOperationRequest;
import com.ot.enums.OperationStatus;
import com.ot.service.OperationSchedulingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/operations")
@RequiredArgsConstructor
public class OperationAdminController {

    private final OperationSchedulingService schedulingService;
    
//---------------------------------------Get All Requested Operation---------------------------------------//
    @GetMapping("/requested")
    public ResponseEntity<List<OperationListResponse>> getRequestedOperations() {

        return ResponseEntity.ok(
                schedulingService.getRequestedOperations()
        );
    }

//---------------------------------------Get All Operations---------------------------------------//
    @GetMapping
    public ResponseEntity<List<OperationListResponse>> getAllOperations() {

        return ResponseEntity.ok(schedulingService.getAllOperations());
    }
    
//---------------------------------------Get All Operations(My Operations)---------------------------------------//
    @GetMapping("/my-operations")
    public ResponseEntity<ApiResponse<List<AssignedOperationResponse>>> getMyAssignedOperations(
            @RequestParam(required = false) List<String> statuses) {

        return ResponseEntity.ok(ApiResponse.success("Assigned operations fetched successfully",
        		schedulingService.getMyAssignedOperations(statuses)));
    }

    
//---------------------------------------Get Operations By Status---------------------------------------//    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OperationListResponse>> getOperationsByStatus(
            @PathVariable OperationStatus status) {

        return ResponseEntity.ok(
                schedulingService.getOperationsByStatus(status)
        );
    }
    

//---------------------------------------Get Operations Status---------------------------------------// 
    @GetMapping("/{operationId}/status")
    public ResponseEntity<OperationStatusResponse> getOperationStatus(
            @PathVariable Long operationId) {

        return ResponseEntity.ok(
                schedulingService.getOperationStatus(operationId)
        );
    }
    
    
//---------------------------------------Schedule Operation---------------------------------------//    
    @PostMapping("/{operationId}/schedule")
    public ResponseEntity<String> scheduleOperation(
            @PathVariable Long operationId,
            @RequestBody ScheduleOperationRequest request) {

        schedulingService.schedule(operationId, request);

        return ResponseEntity.ok("Operation scheduled successfully");
    }
}