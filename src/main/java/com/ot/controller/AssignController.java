package com.ot.controller;

import lombok.RequiredArgsConstructor;

import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ot.dto.staffRequest.StaffAssignmentRequest;
import com.ot.dto.staffRequest.StaffUnAssignRequest;
import com.ot.dto.surgeonAssignment.SurgeonAssignmentRequest;
import com.ot.dto.surgeonAssignment.UnAssignSurgeonRequest;
import com.ot.embed.StaffAssignment;
import com.ot.embed.SurgeonAssignment;
import com.ot.service.AssignService;

@RestController
@RequestMapping("/api/assign")
@RequiredArgsConstructor
public class AssignController {

    private final AssignService assignService;

    // Assign staff to scheduled operation
    @PostMapping("/{operationId}/staff")
    public ResponseEntity<String> assignStaff(
            @PathVariable Long operationId,
            @RequestBody StaffAssignmentRequest request) {

    	assignService.assignStaff(operationId, request);

        return ResponseEntity.ok("Staff assigned successfully");
    }
    
    // Get Staff of scheduled operation
    @GetMapping("/{operationId}/staff")
    public ResponseEntity<Set<StaffAssignment>> getAssignedStaff(
            @PathVariable Long operationId) {

        return ResponseEntity.ok(assignService.getAssignedStaff(operationId));
    }
    
    // UnAssign Staff
    @DeleteMapping("/{operationId}/staff")
    public ResponseEntity<String> unAssignStaff(
            @PathVariable Long operationId,
            @RequestBody StaffUnAssignRequest request) {

        assignService.unAssignStaff(operationId, request.getStaffIds());
        return ResponseEntity.ok("Staff unassigned successfully");
    }
    
    
//-------------------------------------------------Surgeon API's--------------------------------------------------//
    
    // Assign Surgeon To Scheduled Operation
    @PostMapping("/{operationId}/surgeons")
    public ResponseEntity<String> assignSurgeon(
            @PathVariable Long operationId,
            @RequestBody SurgeonAssignmentRequest request) {

        assignService.assignSurgeon(operationId, request);
        return ResponseEntity.ok("Surgeons assigned successfully");
    }
    
    // Get Surgeon of scheduled operation
    @GetMapping("/{operationId}/surgeons")
    public ResponseEntity<Set<SurgeonAssignment>> getAssignedSurgeons(
            @PathVariable Long operationId) {

        return ResponseEntity.ok(assignService.getAssignedSurgeons(operationId));
    }
    
    @DeleteMapping("/{operationId}/surgeons")
    public ResponseEntity<String> unAssignSurgeon(
            @PathVariable Long operationId,
            @RequestBody UnAssignSurgeonRequest request) {

        assignService.unAssignSurgeon(operationId, request);
        return ResponseEntity.ok("Surgeons unassigned successfully");
    }
}
