package com.ot.billing.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ot.billing.service.OTBillingIntegrationService;
import com.ot.dto.billing.OTBillingDetailsResponse;
import com.ot.dto.billing.OTItemBillingResponse;
import com.ot.dto.billing.OTRoomBillingResponse;
import com.ot.dto.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ot/billing")
@RequiredArgsConstructor
public class OTBillingController {

    private final OTBillingIntegrationService billingIntegrationService;

    
    //Get Billing Master
    @GetMapping("/operation/{operationId}")
    public ResponseEntity<?> getBillingByOperationId(@PathVariable Long operationId) {

        var billing = billingIntegrationService.getBillingByOperationId(operationId);

        if (billing == null) {
            return ResponseEntity.ok("Billing not found for operationId: " + operationId);
        }

        return ResponseEntity.ok(billing);
    }
    
    
    //Get Billing Details
    @GetMapping("/details/operation/{operationId}")
    public ResponseEntity<ApiResponse<OTBillingDetailsResponse>> getById(
            @PathVariable Long operationId) {

        OTBillingDetailsResponse response =
                billingIntegrationService.getOTBillingById(operationId);

        return ResponseEntity.ok(
                ApiResponse.success("OT Billing fetched successfully", response)
        );
    }
    
    
    //Get Room Billing Details
    @GetMapping("/details/room/operation/{operationId}")
    public ResponseEntity<ApiResponse<List<OTRoomBillingResponse>>> getRoomBillingByOperationId(
            @PathVariable Long operationId) {

        List<OTRoomBillingResponse> response =
                billingIntegrationService.getRoomBillingByOperationId(operationId);

        return ResponseEntity.ok(
                ApiResponse.success("Room billing fetched successfully", response)
        );
    }
    
    
    //Get Item Billing Details
    @GetMapping("/details/items/operation/{operationId}")
    public ResponseEntity<ApiResponse<List<OTItemBillingResponse>>>
    getItemsByOperationId(@PathVariable Long operationId) {

        List<OTItemBillingResponse> response =
                billingIntegrationService.getItemsByOperationId(operationId);

        return ResponseEntity.ok(
                ApiResponse.success("Items fetched successfully", response)
        );
    }
    
    // ✅ CLOSE BILLING
    @PatchMapping("/operation/{operationId}/close")
    public ResponseEntity<ApiResponse<String>> closeBilling(
            @PathVariable Long operationId) {

    	billingIntegrationService.closeBilling(operationId);

        return ResponseEntity.ok(
                ApiResponse.success("Billing closed successfully", "SUCCESS")
        );
    }
}
