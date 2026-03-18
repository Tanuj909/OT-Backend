package com.ot.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ot.dto.scheduleOperation.ScheduleOperationRequest;
import com.ot.service.OperationSchedulingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/operations")
@RequiredArgsConstructor
public class OperationAdminController {

    private final OperationSchedulingService schedulingService;

    @PostMapping("/{operationId}/schedule")
    public ResponseEntity<String> scheduleOperation(
            @PathVariable Long operationId,
            @RequestBody ScheduleOperationRequest request) {

        schedulingService.schedule(operationId, request);

        return ResponseEntity.ok("Operation scheduled successfully");
    }
}