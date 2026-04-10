package com.ot.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ot.dto.response.ApiResponse;
import com.ot.dto.ward.WardBedRequest;
import com.ot.dto.ward.WardBedResponse;
import com.ot.dto.ward.WardBedUpdateRequest;
import com.ot.enums.BedStatus;
import com.ot.service.WardBedService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ward-beds")
@RequiredArgsConstructor
public class WardBedController {

    private final WardBedService wardBedService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<WardBedResponse>> createBed(
            @RequestBody WardBedRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ward bed created successfully",
                        wardBedService.createBed(request)));
    }

    @GetMapping("/{bedId}")
    public ResponseEntity<ApiResponse<WardBedResponse>> getBedById(
            @PathVariable Long bedId) {

        return ResponseEntity.ok(ApiResponse.success("Ward bed fetched successfully",
                wardBedService.getBedById(bedId)));
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<ApiResponse<List<WardBedResponse>>> getBedsByRoom(
            @PathVariable Long roomId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) BedStatus status) {

        return ResponseEntity.ok(ApiResponse.success("Ward beds fetched successfully",
                wardBedService.getBedsByRoom(roomId, isActive, status)));
    }

    @PutMapping("/{bedId}/update")
    public ResponseEntity<ApiResponse<WardBedResponse>> updateBed(
            @PathVariable Long bedId,
            @RequestBody WardBedUpdateRequest request) {

        return ResponseEntity.ok(ApiResponse.success("Ward bed updated successfully",
                wardBedService.updateBed(bedId, request)));
    }

    @PatchMapping("/{bedId}/maintenance")
    public ResponseEntity<ApiResponse<WardBedResponse>> markMaintenance(
            @PathVariable Long bedId) {

        return ResponseEntity.ok(ApiResponse.success("Bed marked as maintenance",
                wardBedService.markMaintenance(bedId)));
    }

    @PatchMapping("/{bedId}/available")
    public ResponseEntity<ApiResponse<WardBedResponse>> markAvailable(
            @PathVariable Long bedId) {

        return ResponseEntity.ok(ApiResponse.success("Bed marked as available",
                wardBedService.markAvailable(bedId)));
    }

    @PatchMapping("/{bedId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateBed(
            @PathVariable Long bedId) {

        wardBedService.deactivateBed(bedId);
        return ResponseEntity.ok(ApiResponse.success("Ward bed deactivated successfully", null));
    }

    @PatchMapping("/{bedId}/activate")
    public ResponseEntity<ApiResponse<Void>> activateBed(
            @PathVariable Long bedId) {

        wardBedService.activateBed(bedId);
        return ResponseEntity.ok(ApiResponse.success("Ward bed activated successfully", null));
    }
}