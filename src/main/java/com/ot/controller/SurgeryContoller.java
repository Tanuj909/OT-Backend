package com.ot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ot.dto.response.ApiResponse;
import com.ot.dto.surgeryResponse.SurgeryStartResponse;
import com.ot.dto.surgeryResponse.SurgeryStatusResponse;
import com.ot.service.SurgeryService;

@RestController
@RequestMapping("api/surgery")
public class SurgeryContoller {
	
	@Autowired
	private SurgeryService surgeryService;
	
	@PatchMapping("/{operationId}/start")
	public ResponseEntity<ApiResponse<SurgeryStartResponse>> startSurgery(
	        @PathVariable Long operationId) {

	    SurgeryStartResponse response = surgeryService.startSurgery(operationId);
	    return ResponseEntity.ok(ApiResponse.success("Surgery started successfully", response));
	}
	
	@GetMapping("/{operationId}/is-started")
	public ResponseEntity<SurgeryStatusResponse> checkSurgeryStarted(
	        @PathVariable Long operationId) {

	    return ResponseEntity.ok(surgeryService.checkSurgeryStarted(operationId));
	}
}
