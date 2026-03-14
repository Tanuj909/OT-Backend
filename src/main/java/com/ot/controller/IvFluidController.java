package com.ot.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ot.dto.iVFluid.IVFluidRequest;
import com.ot.dto.iVFluid.IVFluidResponse;
import com.ot.dto.iVFluid.IVFluidSummaryResponse;
import com.ot.dto.iVFluid.IVFluidUpdateRequest;
import com.ot.dto.response.ApiResponse;
import com.ot.service.IvFluidService;

@RestController
@RequestMapping("/api/iv-fluids")
public class IvFluidController {
	
	@Autowired private IvFluidService ivFluidService;
	
	@PostMapping("/{operationId}/add")
	public ResponseEntity<ApiResponse<IVFluidResponse>> addIVFluid(
	        @PathVariable Long operationId,
	        @RequestBody IVFluidRequest request) {

	    IVFluidResponse response = ivFluidService.addIVFluid(operationId, request);
	    return ResponseEntity.status(HttpStatus.CREATED)
	            .body(ApiResponse.success("IV fluid added successfully", response));
	}

	@GetMapping("/{operationId}/get")
	public ResponseEntity<ApiResponse<List<IVFluidResponse>>> getIVFluids(
	        @PathVariable Long operationId) {

	    return ResponseEntity.ok(ApiResponse.success("IV fluids fetched successfully",
	            ivFluidService.getIVFluids(operationId)));
	}

	@DeleteMapping("/{operationId}/remove/fluid/{fluidId}")
	public ResponseEntity<ApiResponse<Void>> removeIVFluid(
	        @PathVariable Long operationId,
	        @PathVariable Long fluidId) {

	    ivFluidService.removeIVFluid(operationId, fluidId);
	    return ResponseEntity.ok(ApiResponse.success("IV fluid removed successfully", null));
	}
	
	@PatchMapping("/{operationId}/update/fluid/{fluidId}")
	public ResponseEntity<ApiResponse<IVFluidResponse>> updateIVFluid(
	        @PathVariable Long operationId,
	        @PathVariable Long fluidId,
	        @RequestBody IVFluidUpdateRequest request) {

	    return ResponseEntity.ok(ApiResponse.success("IV fluid updated successfully",
	            ivFluidService.updateIVFluid(operationId, fluidId, request)));
	}

	@PatchMapping("/{operationId}/fluid/{fluidId}/complete")
	public ResponseEntity<ApiResponse<IVFluidResponse>> completeIVFluid(
	        @PathVariable Long operationId,
	        @PathVariable Long fluidId) {

	    return ResponseEntity.ok(ApiResponse.success("IV fluid administration completed",
	            ivFluidService.completeIVFluid(operationId, fluidId)));
	}

	@GetMapping("/{operationId}/summary")
	public ResponseEntity<ApiResponse<IVFluidSummaryResponse>> getIVFluidSummary(
	        @PathVariable Long operationId) {

	    return ResponseEntity.ok(ApiResponse.success("IV fluid summary fetched successfully",
	            ivFluidService.getIVFluidSummary(operationId)));
	}
}
