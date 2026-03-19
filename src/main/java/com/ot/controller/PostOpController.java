package com.ot.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ot.dto.postOp.PostOpResponse;
import com.ot.dto.postOp.PostOpTransferRequest;
import com.ot.dto.postOp.PostOpUpdateRequest;
import com.ot.dto.response.ApiResponse;
import com.ot.service.PostOpService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/operations")
@RequiredArgsConstructor
public class PostOpController {

    private final PostOpService postOpService;

    // PostOp Get
    @GetMapping("/{operationId}/post-op")
    public ResponseEntity<ApiResponse<PostOpResponse>> getPostOpRecord(
            @PathVariable Long operationId) {

        return ResponseEntity.ok(ApiResponse.success("PostOp record fetched successfully",
                postOpService.getPostOpRecord(operationId)));
    }

    // PostOp Update
    @PutMapping("/{operationId}/post-op")
    public ResponseEntity<ApiResponse<PostOpResponse>> updatePostOpRecord(
            @PathVariable Long operationId,
            @RequestBody PostOpUpdateRequest request) {

        return ResponseEntity.ok(ApiResponse.success("PostOp record updated successfully",
                postOpService.updatePostOpRecord(operationId, request)));
    }

    // Patient Transfer
    @PatchMapping("/{operationId}/post-op/transfer")
    public ResponseEntity<ApiResponse<PostOpResponse>> transferPatient(
            @PathVariable Long operationId,
            @RequestBody PostOpTransferRequest request) {

        return ResponseEntity.ok(ApiResponse.success("Patient transferred successfully",
                postOpService.transferPatient(operationId, request)));
    }
}