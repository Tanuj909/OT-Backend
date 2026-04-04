package com.ot.billing.service;

import org.springframework.stereotype.Service;

import com.ot.billing.client.BillingFeignClient;
import com.ot.dto.billing.BillingApiResponse;
import com.ot.dto.billing.BillingMasterCreateRequest;
import com.ot.dto.billing.BillingMasterData;
import com.ot.dto.billing.BillingMasterResponse;
import com.ot.dto.billing.OTAdvancePaymentRequest;
import com.ot.dto.billing.OTBillingDetailsRequest;
import com.ot.dto.billing.OTBillingDetailsResponse;
import com.ot.dto.billing.OTItemBillingRequest;
import com.ot.dto.billing.OTItemBillingResponse;
import com.ot.dto.billing.OTPaymentResponse;
import com.ot.entity.ScheduledOperation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OTBillingIntegrationService {

 private final BillingFeignClient billingFeignClient;

 //---------------------------------------------Create Billing Master--------------------------------------//
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
 
 //---------------------------------------------Create Billing Master--------------------------------------//
 public BillingMasterData getBillingByOperationId(Long operationId) {
	    try {
	        BillingApiResponse<BillingMasterData> response =
	                billingFeignClient.getBillingByOperationId(operationId);

	        if (response != null && response.isSuccess()) {
	            log.info("Billing fetched — operationId: {}, billingId: {}",
	                    operationId, response.getData().getId());

	            return response.getData();
	        } else {
	            log.warn("Billing not found or failed — operationId: {}, message: {}",
	                    operationId,
	                    response != null ? response.getMessage() : "NULL RESPONSE");

	            return null;
	        }

	    } catch (Exception e) {
	        log.error("Billing service error — getBillingByOperationId: {}, error: {}",
	                operationId, e.getMessage());

	        return null;
	    }
	}
 
 //---------------------------------------------Advance Payment--------------------------------------//
 public OTPaymentResponse makeAdvancePayment(OTAdvancePaymentRequest request) {
	    try {
	        BillingApiResponse<OTPaymentResponse> response =
	                billingFeignClient.makeAdvancePayment(request);

	        if (response != null && response.isSuccess()) {
	            log.info("Advance payment successful — billingId: {}, amount: {}",
	                    request.getBillingMasterId(), request.getAmount());

	            return response.getData();
	        } else {
	            log.warn("Advance payment failed — billingId: {}, message: {}",
	                    request.getBillingMasterId(),
	                    response != null ? response.getMessage() : "NULL RESPONSE");

	            return null;
	        }

	    } catch (Exception e) {
	        log.error("Billing service error — advance payment — billingId: {}, error: {}",
	                request.getBillingMasterId(), e.getMessage());

	        return null;
	    }
	}
 
 
 //---------------------------------------------Create Billing Details--------------------------------------//
 public OTBillingDetailsResponse createOTBillingDetails(Long billingMasterId, String operationReference) {
	    try {
	        OTBillingDetailsRequest request = new OTBillingDetailsRequest();
	        request.setBillingMasterId(billingMasterId);
	        request.setOperationReference(operationReference);

	        BillingApiResponse<OTBillingDetailsResponse> response =
	                billingFeignClient.createOTBillingDetails(request);

	        if (response != null && response.isSuccess()) {
	            log.info("OT Billing Details created — billingMasterId: {}, detailsId: {}",
	                    billingMasterId, response.getData().getId());

	            return response.getData();
	        } else {
	            log.warn("OT Billing Details creation failed — billingMasterId: {}, message: {}",
	                    billingMasterId,
	                    response != null ? response.getMessage() : "NULL RESPONSE");

	            return null;
	        }

	    } catch (Exception e) {
	        log.error("Billing service error — createOTBillingDetails — billingMasterId: {}, error: {}",
	                billingMasterId, e.getMessage());

	        return null;
	    }
	}
 
 //---------------------------------------------Add Items to Billing--------------------------------------//
 public OTItemBillingResponse addItemToBilling(OTItemBillingRequest request) {
	    try {

	        BillingApiResponse<OTItemBillingResponse> response =
	                billingFeignClient.addItem(request);

	        if (response != null && response.isSuccess()) {
	            log.info("Item added to billing — operationId: {}, item: {}",
	                    request.getOperationExternalId(),
	                    request.getItemName());

	            return response.getData();
	        } else {
	            log.warn("Item billing failed — operationId: {}, message: {}",
	                    request.getOperationExternalId(),
	                    response != null ? response.getMessage() : "NULL RESPONSE");

	            return null;
	        }

	    } catch (Exception e) {
	        log.error("Billing service error — addItem — operationId: {}, error: {}",
	                request.getOperationExternalId(), e.getMessage());

	        return null;
	    }
	}

}