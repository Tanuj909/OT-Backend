package com.ot.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ot.dto.operationNotes.OperationAttributeRequest;
import com.ot.dto.operationNotes.OperationAttributeResponse;
import com.ot.dto.response.ApiResponse;
import com.ot.service.OperationAttributeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/operations/{operationId}/attributes")
@RequiredArgsConstructor
public class OperationAttributeController {

    private final OperationAttributeService attributeService;

    @PostMapping
    public ResponseEntity<ApiResponse<OperationAttributeResponse>> addAttribute(
            @PathVariable Long operationId,
            @RequestBody OperationAttributeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Attribute added successfully",
                        attributeService.addAttribute(operationId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OperationAttributeResponse>>> getAttributes(
            @PathVariable Long operationId) {
        return ResponseEntity.ok(ApiResponse.success("Attributes fetched successfully",
                attributeService.getAttributes(operationId)));
    }

    @PutMapping("/{attributeId}")
    public ResponseEntity<ApiResponse<OperationAttributeResponse>> updateAttribute(
            @PathVariable Long operationId,
            @PathVariable Long attributeId,
            @RequestBody OperationAttributeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Attribute updated successfully",
                attributeService.updateAttribute(operationId, attributeId, request)));
    }

    @DeleteMapping("/{attributeId}")
    public ResponseEntity<ApiResponse<Void>> deleteAttribute(
            @PathVariable Long operationId,
            @PathVariable Long attributeId) {
        attributeService.deleteAttribute(operationId, attributeId);
        return ResponseEntity.ok(ApiResponse.success("Attribute deleted successfully", null));
    }
}
