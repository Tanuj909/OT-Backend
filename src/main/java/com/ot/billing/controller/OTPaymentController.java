package com.ot.billing.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ot.billing.service.OTBillingIntegrationService;
import com.ot.dto.billing.OTPaymentHistoryResponse;
import com.ot.dto.billing.OTPaymentRequest;
import com.ot.dto.billing.OTPaymentResponse;
import com.ot.dto.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ot/billing/payment")
@RequiredArgsConstructor
public class OTPaymentController {

    private final OTBillingIntegrationService billingIntegrationService;

    @PostMapping("/make")
    public ResponseEntity<ApiResponse<OTPaymentResponse>> makePayment(
            @RequestBody OTPaymentRequest request) {

        OTPaymentResponse response =
                billingIntegrationService.makePayment(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment made successfully", response));
    }
    
    
    @GetMapping("/details/payment/operation/{operationId}/history")
    public ResponseEntity<ApiResponse<OTPaymentHistoryResponse>> getPaymentHistory(
            @PathVariable Long operationId) {

        OTPaymentHistoryResponse response =
                billingIntegrationService.getPaymentHistory(operationId);

        return ResponseEntity.ok(
                ApiResponse.success("Payment history fetched successfully", response)
        );
    }
}