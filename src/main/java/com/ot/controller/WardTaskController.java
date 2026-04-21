package com.ot.controller;

import com.ot.dto.ward.CompleteWardTaskRequest;
import com.ot.dto.ward.CreateWardTaskRequest;
import com.ot.dto.ward.WardTaskResponse;
import com.ot.enums.TaskStatus;
import com.ot.service.WardTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ward-tasks")
@RequiredArgsConstructor
public class WardTaskController {

    private final WardTaskService wardTaskService;

    // Task create karo (doctor ya koi bhi)
    @PostMapping
    public ResponseEntity<WardTaskResponse> createTask(
            @RequestBody CreateWardTaskRequest request) {
        return ResponseEntity.ok(wardTaskService.createTask(request));
    }

    // Task complete karo (koi bhi — no restriction)
    @PutMapping("/{taskId}/complete")
    public ResponseEntity<WardTaskResponse> completeTask(
            @PathVariable Long taskId,
            @RequestBody CompleteWardTaskRequest request) {
        return ResponseEntity.ok(wardTaskService.completeTask(taskId, request));
    }

    // Task cancel karo
    @PutMapping("/{taskId}/cancel")
    public ResponseEntity<WardTaskResponse> cancelTask(
            @PathVariable Long taskId) {
        return ResponseEntity.ok(wardTaskService.cancelTask(taskId));
    }

    // Single task fetch
    @GetMapping("/{taskId}")
    public ResponseEntity<WardTaskResponse> getById(
            @PathVariable Long taskId) {
        return ResponseEntity.ok(wardTaskService.getById(taskId));
    }

    // Operation ke saare tasks
    @GetMapping("/operation/{operationId}")
    public ResponseEntity<List<WardTaskResponse>> getByOperation(
            @PathVariable Long operationId) {
        return ResponseEntity.ok(wardTaskService.getByOperation(operationId));
    }

    // Admission ke saare tasks
    @GetMapping("/admission/{wardAdmissionId}")
    public ResponseEntity<List<WardTaskResponse>> getByAdmission(
            @PathVariable Long wardAdmissionId) {
        return ResponseEntity.ok(wardTaskService.getByAdmission(wardAdmissionId));
    }

    // Operation + status filter — e.g. GET /api/ward-tasks/operation/5/status?status=PENDING
    @GetMapping("/operation/{operationId}/status")
    public ResponseEntity<List<WardTaskResponse>> getByOperationAndStatus(
            @PathVariable Long operationId,
            @RequestParam TaskStatus status) {
        return ResponseEntity.ok(wardTaskService.getByOperationAndStatus(operationId, status));
    }
}