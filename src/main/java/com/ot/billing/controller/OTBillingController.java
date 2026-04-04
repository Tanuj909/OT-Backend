package com.ot.billing.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ot.billing.service.OTBillingIntegrationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ot/billing")
@RequiredArgsConstructor
public class OTBillingController {

    private final OTBillingIntegrationService otBillingIntegrationService;

    @GetMapping("/operation/{operationId}")
    public ResponseEntity<?> getBillingByOperationId(@PathVariable Long operationId) {

        var billing = otBillingIntegrationService.getBillingByOperationId(operationId);

        if (billing == null) {
            return ResponseEntity.ok("Billing not found for operationId: " + operationId);
        }

        return ResponseEntity.ok(billing);
    }
}
