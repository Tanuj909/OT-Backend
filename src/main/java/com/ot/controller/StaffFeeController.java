package com.ot.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ot.dto.response.ApiResponse;
import com.ot.dto.staffRequest.StaffFeeRequest;
import com.ot.dto.staffRequest.StaffFeeResponse;
import com.ot.dto.staffRequest.StaffFeeUpdateRequest;
import com.ot.service.StaffFeeService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/staff-fees")
@RequiredArgsConstructor
public class StaffFeeController {

    private final StaffFeeService staffFeeService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<StaffFeeResponse>> createStaffFee(
            @RequestBody StaffFeeRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Staff fee created successfully",
                        staffFeeService.createStaffFee(request)));
    }

    @GetMapping("/staff/{staffId}")
    public ResponseEntity<ApiResponse<StaffFeeResponse>> getByStaffId(
            @PathVariable Long staffId) {

        return ResponseEntity.ok(ApiResponse.success("Staff fee fetched successfully",
                staffFeeService.getByStaffId(staffId)));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<StaffFeeResponse>>> getAllStaffFees(
            @RequestParam(required = false) Boolean isActive) {

        return ResponseEntity.ok(ApiResponse.success("Staff fees fetched successfully",
                staffFeeService.getAllStaffFees(isActive)));
    }

    @PutMapping("/staff/{staffId}/update")
    public ResponseEntity<ApiResponse<StaffFeeResponse>> updateStaffFee(
            @PathVariable Long staffId,
            @RequestBody StaffFeeUpdateRequest request) {

        return ResponseEntity.ok(ApiResponse.success("Staff fee updated successfully",
                staffFeeService.updateStaffFee(staffId, request)));
    }
}