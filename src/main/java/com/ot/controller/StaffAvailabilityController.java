package com.ot.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ot.dto.request.CreateStaffAvailabilityRequest;
import com.ot.dto.response.ApiResponse;
import com.ot.dto.response.StaffAvailabilityResponse;
import com.ot.dto.staffRequest.StaffRosterResponse;
import com.ot.entity.User;
import com.ot.service.StaffAvailabilityService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/staff-availability")
@RequiredArgsConstructor
public class StaffAvailabilityController {

    private final StaffAvailabilityService staffAvailabilityService;

    @PostMapping
    public ApiResponse<StaffAvailabilityResponse> createAvailability(
            @RequestBody CreateStaffAvailabilityRequest request,
            HttpServletRequest httpRequest){

    	StaffAvailabilityResponse response = staffAvailabilityService.createStaffAvailability(request);

        return ApiResponse.success(
                "Staff Avaliblity Created!",
                response
        );
    }
    
    @GetMapping("/{staffId}")
    public ApiResponse<List<StaffAvailabilityResponse>> getAvailabilityByStaff(
            @PathVariable Long staffId,
            HttpServletRequest httpRequest){

        List<StaffAvailabilityResponse> response =
                staffAvailabilityService.getStaffAvailabilityByStaff(staffId);

        return ApiResponse.success(
                "Staff Availability fetched successfully!",
                response
        );
    }
    
    
    @GetMapping("/availability")
    public ResponseEntity<ApiResponse<StaffRosterResponse>> getStaffAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        return ResponseEntity.ok(ApiResponse.success("Staff availability fetched successfully",
        		staffAvailabilityService.getStaffAvailability(startTime, endTime)));
    }
    
    @GetMapping("/check")
    public ApiResponse<Boolean> checkStaffAvailability(
            @RequestParam Long staffId,
            @RequestParam Long hospitalId,
            LocalDate date,
            LocalTime start,
            LocalTime end
            ) {

        boolean available = staffAvailabilityService.isStaffAvailable(staffId, date, start, end);

        return ApiResponse.success(
                "Staff availability checked successfully",
                available
        );
    }
    
    @DeleteMapping("/{availabilityId}")
    public ApiResponse<Void> deleteAvailability(
            @PathVariable Long availabilityId,
            HttpServletRequest httpRequest){

        staffAvailabilityService.deleteStaffAvailability(availabilityId);

        return ApiResponse.success(
                "Staff Availability deleted successfully!",
                null
        );
    }
}
