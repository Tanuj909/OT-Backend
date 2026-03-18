package com.ot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ot.dto.intraOp.AnesthesiaTimeRequest;
import com.ot.dto.intraOp.IntraOpRequest;
import com.ot.dto.intraOp.IntraOpResponse;
import com.ot.dto.intraOp.IntraOpStatusRequest;
import com.ot.dto.intraOp.IntraOpSummaryResponse;
import com.ot.dto.response.ApiResponse;
import com.ot.service.IntraOpService;

@RestController
@RequestMapping("/api/intra-op")
public class IntraOpController {
	
	@Autowired private IntraOpService intraOpService;
	
	@PostMapping("/{operationId}/create")
	public ResponseEntity<ApiResponse<IntraOpResponse>> createIntraOpRecord(
	        @PathVariable Long operationId,
	        @RequestBody IntraOpRequest request) {

	    IntraOpResponse response = intraOpService.createIntraOpRecord(operationId, request);
	    return ResponseEntity.status(HttpStatus.CREATED)
	            .body(ApiResponse.success("IntraOp record created successfully", response));
	}

	@GetMapping("/{operationId}/get")
	public ResponseEntity<ApiResponse<IntraOpResponse>> getIntraOpRecord(
	        @PathVariable Long operationId) {

	    return ResponseEntity.ok(ApiResponse.success("IntraOp record fetched successfully",
	            intraOpService.getIntraOpRecord(operationId)));
	}

	@PutMapping("/{operationId}/update")
	public ResponseEntity<ApiResponse<IntraOpResponse>> updateIntraOpRecord(
	        @PathVariable Long operationId,
	        @RequestBody IntraOpRequest request) {

	    return ResponseEntity.ok(ApiResponse.success("IntraOp record updated successfully",
	            intraOpService.updateIntraOpRecord(operationId, request)));
	}
	
	@PatchMapping("/{operationId}/update/status")
	public ResponseEntity<ApiResponse<IntraOpResponse>> updateIntraOpStatus(
	        @PathVariable Long operationId,
	        @RequestBody IntraOpStatusRequest request) {

	    return ResponseEntity.ok(ApiResponse.success("IntraOp status updated successfully",
	            intraOpService.updateIntraOpStatus(operationId, request)));
	}

	@PatchMapping("/{operationId}/update/anesthesia-time")
	public ResponseEntity<ApiResponse<IntraOpResponse>> updateAnesthesiaTime(
	        @PathVariable Long operationId,
	        @RequestBody AnesthesiaTimeRequest request) {

	    return ResponseEntity.ok(ApiResponse.success("Anesthesia time updated successfully",
	            intraOpService.updateAnesthesiaTime(operationId, request)));
	}

	@GetMapping("/{operationId}/summary")
	public ResponseEntity<ApiResponse<IntraOpSummaryResponse>> getIntraOpSummary(
	        @PathVariable Long operationId) {

	    return ResponseEntity.ok(ApiResponse.success("IntraOp summary fetched successfully",
	            intraOpService.getIntraOpSummary(operationId)));
	}

}
