package com.ot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ot.dto.opertaionReport.OperationReportResponse;
import com.ot.dto.response.ApiResponse;
import com.ot.service.OperationReportService;

@RestController
@RequestMapping("/api/operations")
public class OperationReportController {
	
	@Autowired private OperationReportService reportService;
	
	@GetMapping("/{operationId}/report")
	public ResponseEntity<ApiResponse<OperationReportResponse>> getOperationReport(
	        @PathVariable Long operationId) {

	    return ResponseEntity.ok(ApiResponse.success("Operation report fetched successfully",
	            reportService.getOperationReport(operationId)));
	}
}
