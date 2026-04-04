package com.ot.billing.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ot.billing.service.OTBillingIntegrationService;
import com.ot.dto.billing.OTAdvancePaymentRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ot/payment")
@RequiredArgsConstructor
public class OTAdvancePaymentController {

    private final OTBillingIntegrationService otBillingIntegrationService;

    @PostMapping("/advance")
    public ResponseEntity<?> makeAdvancePayment(
            @RequestBody OTAdvancePaymentRequest request) {

        var response = otBillingIntegrationService.makeAdvancePayment(request);

        if (response == null) {
            return ResponseEntity.badRequest()
                    .body("Advance payment failed!");
        }

        return ResponseEntity.ok(response);
    }
}