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
import com.ot.dto.ward.WardRoomRequest;
import com.ot.dto.ward.WardRoomResponse;
import com.ot.dto.ward.WardRoomUpdateRequest;
import com.ot.service.WardRoomService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ward-rooms")
@RequiredArgsConstructor
public class WardRoomController {

    private final WardRoomService wardRoomService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<WardRoomResponse>> createWardRoom(
            @RequestBody WardRoomRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ward room created successfully",
                        wardRoomService.createWardRoom(request)));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<WardRoomResponse>> getWardRoomById(
            @PathVariable Long roomId) {

        return ResponseEntity.ok(ApiResponse.success("Ward room fetched successfully",
                wardRoomService.getWardRoomById(roomId)));
    }

    @GetMapping("/ward/{wardId}")
    public ResponseEntity<ApiResponse<List<WardRoomResponse>>> getWardRoomsByWardId(
            @PathVariable Long wardId,
            @RequestParam(required = false) Boolean isActive) {

        return ResponseEntity.ok(ApiResponse.success("Ward rooms fetched successfully",
                wardRoomService.getWardRoomsByWardId(wardId, isActive)));
    }

    @GetMapping("/ward/{wardId}/available")
    public ResponseEntity<ApiResponse<List<WardRoomResponse>>> getAvailableRooms(
            @PathVariable Long wardId) {

        return ResponseEntity.ok(ApiResponse.success("Available rooms fetched successfully",
                wardRoomService.getAvailableRooms(wardId)));
    }

    @PutMapping("/{roomId}/update")
    public ResponseEntity<ApiResponse<WardRoomResponse>> updateWardRoom(
            @PathVariable Long roomId,
            @RequestBody WardRoomUpdateRequest request) {

        return ResponseEntity.ok(ApiResponse.success("Ward room updated successfully",
                wardRoomService.updateWardRoom(roomId, request)));
    }

    @PatchMapping("/{roomId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateWardRoom(
            @PathVariable Long roomId) {

        wardRoomService.deactivateWardRoom(roomId);
        return ResponseEntity.ok(ApiResponse.success("Ward room deactivated successfully", null));
    }

    @PatchMapping("/{roomId}/activate")
    public ResponseEntity<ApiResponse<Void>> activateWardRoom(
            @PathVariable Long roomId) {

        wardRoomService.activateWardRoom(roomId);
        return ResponseEntity.ok(ApiResponse.success("Ward room activated successfully", null));
    }
}