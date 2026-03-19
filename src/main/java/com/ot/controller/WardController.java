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
import com.ot.dto.ward.WardRequest;
import com.ot.dto.ward.WardResponse;
import com.ot.dto.ward.WardUpdateRequest;
import com.ot.enums.WardType;
import com.ot.service.WardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/wards")
@RequiredArgsConstructor
public class WardController {

    private final WardService wardService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<WardResponse>> createWard(
            @RequestBody WardRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ward created successfully",
                        wardService.createWard(request)));
    }

    @GetMapping("/{wardId}")
    public ResponseEntity<ApiResponse<WardResponse>> getWardById(
            @PathVariable Long wardId) {

        return ResponseEntity.ok(ApiResponse.success("Ward fetched successfully",
                wardService.getWardById(wardId)));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<WardResponse>>> getAllWards(
            @RequestParam(required = false) WardType wardType,
            @RequestParam(required = false) Boolean isActive) {

        return ResponseEntity.ok(ApiResponse.success("Wards fetched successfully",
                wardService.getAllWards(wardType, isActive)));
    }

    @PutMapping("/{wardId}/update")
    public ResponseEntity<ApiResponse<WardResponse>> updateWard(
            @PathVariable Long wardId,
            @RequestBody WardUpdateRequest request) {

        return ResponseEntity.ok(ApiResponse.success("Ward updated successfully",
                wardService.updateWard(wardId, request)));
    }

    @PatchMapping("/{wardId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateWard(
            @PathVariable Long wardId) {

        wardService.deactivateWard(wardId);
        return ResponseEntity.ok(ApiResponse.success("Ward deactivated successfully", null));
    }

    @PatchMapping("/{wardId}/activate")
    public ResponseEntity<ApiResponse<Void>> activateWard(
            @PathVariable Long wardId) {

        wardService.activateWard(wardId);
        return ResponseEntity.ok(ApiResponse.success("Ward activated successfully", null));
    }
}