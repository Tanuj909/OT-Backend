package com.ot.billing.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ot.billing.service.OTBillingIntegrationService;
import com.ot.dto.billing.OTBillingSummaryResponse;
import com.ot.dto.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ot/billing")
@RequiredArgsConstructor
public class OTBillSummaryController {
	
    private final OTBillingIntegrationService billingIntegrationService;
    
    
    @GetMapping("/details/operation/{operationId}/summary")
    public ResponseEntity<ApiResponse<OTBillingSummaryResponse>> getBillingSummary(
            @PathVariable Long operationId) {

        OTBillingSummaryResponse response =
                billingIntegrationService.getBillingSummary(operationId);

        return ResponseEntity.ok(
                ApiResponse.success("Billing summary fetched successfully", response)
        );
    }

}
