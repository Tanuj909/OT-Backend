package com.ot.controller;

import java.util.List;

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

import com.ot.dto.consumableUsage.ConsumableSummaryResponse;
import com.ot.dto.consumableUsage.ConsumableUsageRequest;
import com.ot.dto.consumableUsage.ConsumableUsageResponse;
import com.ot.dto.response.ApiResponse;
import com.ot.service.ConsumableUsageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/operations")
@RequiredArgsConstructor
public class ConsumableUsageController {

    private final ConsumableUsageService consumableService;

    @PostMapping("/{operationId}/consumables")
    public ResponseEntity<ApiResponse<ConsumableUsageResponse>> addConsumable(
            @PathVariable Long operationId,
            @RequestBody ConsumableUsageRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Consumable added successfully",
                        consumableService.addConsumable(operationId, request)));
    }

    @GetMapping("/{operationId}/consumables")
    public ResponseEntity<ApiResponse<List<ConsumableUsageResponse>>> getConsumables(
            @PathVariable Long operationId) {

        return ResponseEntity.ok(ApiResponse.success("Consumables fetched successfully",
                consumableService.getConsumables(operationId)));
    }

    @PatchMapping("/{operationId}/consumables/{consumableId}")
    public ResponseEntity<ApiResponse<ConsumableUsageResponse>> updateConsumable(
            @PathVariable Long operationId,
            @PathVariable Long consumableId,
            @RequestBody ConsumableUsageRequest request) {

        return ResponseEntity.ok(ApiResponse.success("Consumable updated successfully",
                consumableService.updateConsumable(operationId, consumableId, request)));
    }

    @PatchMapping("/{operationId}/consumables/{consumableId}/return")
    public ResponseEntity<ApiResponse<ConsumableUsageResponse>> returnConsumable(
            @PathVariable Long operationId,
            @PathVariable Long consumableId) {

        return ResponseEntity.ok(ApiResponse.success("Consumable returned successfully",
                consumableService.returnConsumable(operationId, consumableId)));
    }

    @DeleteMapping("/{operationId}/consumables/{consumableId}")
    public ResponseEntity<ApiResponse<Void>> deleteConsumable(
            @PathVariable Long operationId,
            @PathVariable Long consumableId) {

        consumableService.deleteConsumable(operationId, consumableId);
        return ResponseEntity.ok(ApiResponse.success("Consumable deleted successfully", null));
    }

    @GetMapping("/{operationId}/consumables/summary")
    public ResponseEntity<ApiResponse<ConsumableSummaryResponse>> getConsumableSummary(
            @PathVariable Long operationId) {

        return ResponseEntity.ok(ApiResponse.success("Consumable summary fetched successfully",
                consumableService.getConsumableSummary(operationId)));
    }
}