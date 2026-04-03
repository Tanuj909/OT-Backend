package com.ot.billing.service;

import org.springframework.stereotype.Service;

import com.ot.billing.client.BillingFeignClient;
import com.ot.dto.billing.BillingApiResponse;
import com.ot.dto.billing.BillingMasterCreateRequest;
import com.ot.dto.billing.BillingMasterData;
import com.ot.dto.billing.BillingMasterResponse;
import com.ot.entity.ScheduledOperation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

//package com.ot.billing.service

//package com.ot.billing.service

@Service
@RequiredArgsConstructor
@Slf4j
public class OTBillingIntegrationService {

 private final BillingFeignClient billingFeignClient;

 public Long createBillingMaster(ScheduledOperation operation) {
     try {
         BillingMasterCreateRequest request = BillingMasterCreateRequest.builder()
                 .hospitalExternalId(operation.getHospital().getId())
                 .patientExternalId(operation.getPatientId())
                 .otOperationId(operation.getId())
                 .moduleType("OT")
                 .build();

         BillingApiResponse<BillingMasterResponse> response =
                 billingFeignClient.createBillingMaster(request);

         if (response.isSuccess()) {
             log.info("BillingMaster created — operationId: {}, billingId: {}",
                     operation.getId(), response.getData().getId());
             return response.getData().getId();
         } else {
             log.error("BillingMaster creation failed: {}", response.getMessage());
             return null;
         }

     } catch (Exception e) {
         log.error("Billing service error — createBillingMaster: {}", e.getMessage());
         return null;
     }
 }
}