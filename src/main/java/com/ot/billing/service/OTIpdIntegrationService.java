package com.ot.billing.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.ot.billing.client.IPDFeignClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OTIpdIntegrationService {

    private final IPDFeignClient ipdFeignClient;

    public void notifyIpdForReturn(Long patientId, Long admissionId) {

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("patientId", patientId);
            payload.put("admissionId", admissionId);

            ipdFeignClient.notifyReturnFromOT(payload);

            log.info("IPD notified for return. admissionId={}", admissionId);

        } catch (Exception e) {
            log.error("Failed to notify IPD", e);
            throw new RuntimeException("IPD notification failed");
        }
    }
}