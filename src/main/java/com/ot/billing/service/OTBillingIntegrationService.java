package com.ot.billing.service;

import java.util.List;

import org.springframework.stereotype.Service;
import com.ot.billing.client.BillingFeignClient;
import com.ot.dto.billing.BillingApiResponse;
import com.ot.dto.billing.BillingMasterCreateRequest;
import com.ot.dto.billing.BillingMasterData;
import com.ot.dto.billing.BillingMasterResponse;
import com.ot.dto.billing.OTAdvancePaymentRequest;
import com.ot.dto.billing.OTBillingDetailsRequest;
import com.ot.dto.billing.OTBillingDetailsResponse;
import com.ot.dto.billing.OTBillingSummaryResponse;
import com.ot.dto.billing.OTItemBillingRequest;
import com.ot.dto.billing.OTItemBillingResponse;
import com.ot.dto.billing.OTItemBillingUpdateRequest;
import com.ot.dto.billing.OTPaymentHistoryResponse;
import com.ot.dto.billing.OTPaymentRequest;
import com.ot.dto.billing.OTPaymentResponse;
import com.ot.dto.billing.OTRecoveryRoomBillingEndRequest;
import com.ot.dto.billing.OTRecoveryRoomBillingRequest;
import com.ot.dto.billing.OTRecoveryRoomBillingResponse;
import com.ot.dto.billing.OTRecoveryRoomBillingUpdateRequest;
import com.ot.dto.billing.OTRoomBillingEndRequest;
import com.ot.dto.billing.OTRoomBillingRequest;
import com.ot.dto.billing.OTRoomBillingResponse;
import com.ot.dto.billing.OTStaffBillingRequest;
import com.ot.dto.billing.OTStaffBillingResponse;
import com.ot.entity.ScheduledOperation;
import com.ot.exception.BillingException;

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
					.hospitalExternalId(operation.getHospital().getId()).patientExternalId(operation.getPatientId())
					.otOperationId(operation.getId()).moduleType("OT").build();

			BillingApiResponse<BillingMasterResponse> response = billingFeignClient.createBillingMaster(request);

			if (response.isSuccess()) {
				log.info("BillingMaster created — operationId: {}, billingId: {}", operation.getId(),
						response.getData().getId());
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
			BillingApiResponse<BillingMasterData> response = billingFeignClient.getBillingByOperationId(operationId);

			if (response != null && response.isSuccess()) {
				log.info("Billing fetched — operationId: {}, billingId: {}", operationId, response.getData().getId());

				return response.getData();
			} else {
				log.warn("Billing not found or failed — operationId: {}, message: {}", operationId,
						response != null ? response.getMessage() : "NULL RESPONSE");

				return null;
			}

		} catch (Exception e) {
			log.error("Billing service error — getBillingByOperationId: {}, error: {}", operationId, e.getMessage());

			return null;
		}
	}
 
    //---------------------------------------------Advance Payment--------------------------------------//
	public OTPaymentResponse makeAdvancePayment(OTAdvancePaymentRequest request) {
		try {
			BillingApiResponse<OTPaymentResponse> response = billingFeignClient.makeAdvancePayment(request);

			if (response != null && response.isSuccess()) {
				log.info("Advance payment successful — billingId: {}, amount: {}", request.getBillingMasterId(),
						request.getAmount());

				return response.getData();
			} else {
				log.warn("Advance payment failed — billingId: {}, message: {}", request.getBillingMasterId(),
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

			BillingApiResponse<OTBillingDetailsResponse> response = billingFeignClient.createOTBillingDetails(request);

			if (response != null && response.isSuccess()) {
				log.info("OT Billing Details created — billingMasterId: {}, detailsId: {}", billingMasterId,
						response.getData().getId());

				return response.getData();
			} else {
				log.warn("OT Billing Details creation failed — billingMasterId: {}, message: {}", billingMasterId,
						response != null ? response.getMessage() : "NULL RESPONSE");

				return null;
			}

		} catch (Exception e) {
			log.error("Billing service error — createOTBillingDetails — billingMasterId: {}, error: {}",
					billingMasterId, e.getMessage());

			return null;
		}
	}
	
	
	// ---------------------------------------------Add Staff Billing--------------------------------------//
	public OTStaffBillingResponse addStaffBilling(OTStaffBillingRequest request) {
	    try {

	        BillingApiResponse<OTStaffBillingResponse> response =
	                billingFeignClient.addStaffBilling(request);

	        if (response != null && response.isSuccess()) {
	            log.info("Staff billing added — operationId: {}, staff: {}",
	                    request.getOperationExternalId(),
	                    request.getStaffName());

	            return response.getData();
	        } else {
	            log.warn("Staff billing failed — operationId: {}, message: {}",
	                    request.getOperationExternalId(),
	                    response != null ? response.getMessage() : "NULL RESPONSE");

	            return null;
	        }

	    } catch (Exception e) {
	        log.error("Billing service error — addStaffBilling — operationId: {}, error: {}",
	                request.getOperationExternalId(), e.getMessage());
	        return null;
	    }
	}
 
	// ---------------------------------------------Add Items to Billing--------------------------------------//
	public OTItemBillingResponse addItemToBilling(OTItemBillingRequest request) {
		try {

			BillingApiResponse<OTItemBillingResponse> response = billingFeignClient.addItem(request);

			if (response != null && response.isSuccess()) {
				log.info("Item added to billing — operationId: {}, item: {}", request.getOperationExternalId(),
						request.getItemName());

				return response.getData();
			} else {
				log.warn("Item billing failed — operationId: {}, message: {}", request.getOperationExternalId(),
						response != null ? response.getMessage() : "NULL RESPONSE");

				return null;
			}

		} catch (Exception e) {
			log.error("Billing service error — addItem — operationId: {}, error: {}", request.getOperationExternalId(),
					e.getMessage());

			return null;
		}
	}
	
	// ---------------------------------------------Remove Items to Billing--------------------------------------//
	public void removeItemFromBilling(Long itemBillingId) {
		try {

			billingFeignClient.removeItem(itemBillingId);

			log.info("Item removed from billing — itemBillingId: {}", itemBillingId);

		} catch (Exception e) {
			log.error("Billing service error — removeItem — itemBillingId: {}, error: {}", itemBillingId,
					e.getMessage());
		}
	}
	
	// ---------------------------------------------Update Items to Billing--------------------------------------//
	public void updateItemInBilling(Long itemBillingId, OTItemBillingUpdateRequest request) {
	    try {

	        BillingApiResponse<OTItemBillingResponse> response =
	                billingFeignClient.updateItem(itemBillingId, request);

	        if (response != null && response.isSuccess()) {
	            log.info("Item updated in billing — itemBillingId: {}", itemBillingId);
	        } else {
	            log.warn("Item update failed — itemBillingId: {}, message: {}",
	                    itemBillingId,
	                    response != null ? response.getMessage() : "NULL RESPONSE");
	        }

	    } catch (Exception e) {
	        log.error("Billing service error — updateItem — itemBillingId: {}, error: {}",
	                itemBillingId, e.getMessage());
	    }
	}
 
    // ==================== CREATE ROOM BILLING ==================== //
	public OTRoomBillingResponse createRoomBilling(OTRoomBillingRequest request) {
		try {

			BillingApiResponse<OTRoomBillingResponse> response = billingFeignClient.createRoomBilling(request);

			if (response != null && response.isSuccess()) {
				return response.getData();
			}

			return null;

		} catch (Exception e) {
			log.error("Room billing failed — operationId: {}, error: {}", request.getOperationExternalId(),
					e.getMessage());
			return null;
		}
	}
	
    // ==================== CREATE ROOM BILLING ==================== //
	public void setRoomEndTime(OTRoomBillingEndRequest request) {
	    try {
	        BillingApiResponse<OTRoomBillingResponse> response =
	                billingFeignClient.setEndTime(request);

	        if (response != null && response.isSuccess()) {
	            log.info("Room end time set — operationId: {}", request.getOperationExternalId());
	        } else {
	            log.warn("Failed to set room end time — operationId: {}, message: {}",
	                    request.getOperationExternalId(),
	                    response != null ? response.getMessage() : "NULL RESPONSE");
	        }

	    } catch (Exception e) {
	        log.error("🔥 FULL ERROR — setRoomEndTime", e);
//	        throw new RuntimeException("Billing service failed", e); // TEMP
	        throw new BillingException("Billing Service Failed");
	        
	    }
	}
	
    // ==================== Close BILLING ==================== //
	public void closeBilling(Long operationId) {
	    try {

	        BillingApiResponse<OTBillingDetailsResponse> response =
	                billingFeignClient.closeBilling(operationId);

	        if (response != null && response.isSuccess()) {
	            log.info("Billing closed — operationId: {}", operationId);
	        } else {
	            log.warn("Billing close failed — operationId: {}, message: {}",
	                    operationId,
	                    response != null ? response.getMessage() : "NULL RESPONSE");
	        }

	    } 
	    catch (Exception e) {
	        log.error("Billing service error — closeBilling — operationId: {}, error: {}", e);
	        throw new RuntimeException("Billing service failed", e); // TEMP
	    }
	}
	
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//-------------------Get Billing Details By Operation Id-----------------------//
	public OTBillingDetailsResponse getOTBillingById(Long operationId) {
    try {

        BillingApiResponse<OTBillingDetailsResponse> response =
                billingFeignClient.getOTBillingById(operationId);

        if (response != null && response.isSuccess()) {
            log.info("OT Billing fetched — id: {}", operationId);
            return response.getData();
        } else {
            log.warn("Failed to fetch OT Billing — id: {}, message: {}",
            		operationId,
                    response != null ? response.getMessage() : "NULL RESPONSE");
            return null;
        }

    } catch (Exception e) {
        log.error("Billing service error — getOTBillingById — id: {}, error: {}",
        		operationId, e.getMessage());
        return null;
    }
}
	
	
	//-------------------Get Room Billing Details By Operation Id-----------------------//
	public List<OTRoomBillingResponse> getRoomBillingByOperationId(Long operationId) {
	    try {

	        BillingApiResponse<List<OTRoomBillingResponse>> response =
	                billingFeignClient.getRoomBillingByOperationId(operationId);

	        if (response != null && response.isSuccess()) {
	            log.info("Room billing fetched — operationId: {}", operationId);
	            return response.getData();
	        } else {
	            log.warn("Failed to fetch room billing — operationId: {}, message: {}",
	                    operationId,
	                    response != null ? response.getMessage() : "NULL RESPONSE");
	            return null;
	        }

	    } catch (Exception e) {
	        log.error("Billing service error — getRoomBillingByOperationId — operationId: {}, error: {}",
	                operationId, e.getMessage());
	        return null;
	    }
	}
	
	//-------------------Get Item Billing Details By Operation Id-----------------------//
	public List<OTItemBillingResponse> getItemsByOperationId(Long operationId) {
	    try {

	        BillingApiResponse<List<OTItemBillingResponse>> response =
	                billingFeignClient.getItemsByOperationId(operationId);

	        if (response != null && response.isSuccess()) {
	            log.info("Item billing fetched — operationId: {}", operationId);
	            return response.getData();
	        } else {
	            log.warn("Failed to fetch item billing — operationId: {}, message: {}",
	                    operationId,
	                    response != null ? response.getMessage() : "NULL RESPONSE");
	            return null;
	        }

	    } catch (Exception e) {
	        log.error("Billing service error — getItemsByOperationId — operationId: {}, error: {}",
	                operationId, e.getMessage());
	        return null;
	    }
	}
	
	//-------------------Get Operation Billing Summary-----------------------//
	public OTBillingSummaryResponse getBillingSummary(Long operationId) {
	    try {

	        BillingApiResponse<OTBillingSummaryResponse> response =
	                billingFeignClient.getBillingSummary(operationId);

	        if (response != null && response.isSuccess()) {
	            log.info("Billing summary fetched — operationId: {}", operationId);
	            return response.getData();
	        } else {
	            log.warn("Failed to fetch billing summary — operationId: {}, message: {}",
	                    operationId,
	                    response != null ? response.getMessage() : "NULL RESPONSE");
	            return null;
	        }

	    } catch (Exception e) {
	        log.error("Billing service error — getBillingSummary — operationId: {}, error: {}",
	                operationId, e.getMessage());
	        return null;
	    }
	}
	
	//-------------------Make Payment-----------------------//
	public OTPaymentResponse makePayment(OTPaymentRequest request) {
	    try {

	        BillingApiResponse<OTPaymentResponse> response =
	                billingFeignClient.makePayment(request);

	        if (response != null && response.isSuccess()) {
	            log.info("Payment successful — operationId: {}, amount: {}",
	                    request.getOperationExternalId(),
	                    request.getAmount());
	            return response.getData();
	        } else {
	            log.warn("Payment failed — operationId: {}, message: {}",
	                    request.getOperationExternalId(),
	                    response != null ? response.getMessage() : "NULL RESPONSE");
	            return null;
	        }

	    } catch (Exception e) {
	        log.error("Billing service error — makePayment — operationId: {}, error: {}",
	                request.getOperationExternalId(), e.getMessage());
	        return null;
	    }
	}
	
	//------------------- Payment History -----------------------//
	public OTPaymentHistoryResponse getPaymentHistory(Long operationId) {
	    try {

	        BillingApiResponse<OTPaymentHistoryResponse> response =
	                billingFeignClient.getPaymentHistory(operationId);

	        if (response != null && response.isSuccess()) {
	            log.info("Payment history fetched — operationId: {}", operationId);
	            return response.getData();
	        } else {
	            log.warn("Failed to fetch payment history — operationId: {}, message: {}",
	                    operationId,
	                    response != null ? response.getMessage() : "NULL RESPONSE");
	            return null;
	        }

	    } catch (Exception e) {
	        log.error("Billing service error — getPaymentHistory — operationId: {}, error: {}",
	                operationId, e.getMessage());
	        return null;
	    }
	}

	//------------------- Recover Room Billing  -----------------------//
	public OTRecoveryRoomBillingResponse createRecoveryRoom(OTRecoveryRoomBillingRequest request) {
		try {

			OTRecoveryRoomBillingResponse response = billingFeignClient.createRecoveryRoom(request);

			log.info("Recovery room billing created — operationId: {}", request.getOperationExternalId());

			return response;

		} catch (Exception e) {
			log.error("Billing service error — createRecoveryRoom — operationId: {}, error: {}",
					request.getOperationExternalId(), e.getMessage());
			return null;
		}
	}

	//------------------- Recover Room Billing  -----------------------//
	public OTRecoveryRoomBillingResponse setRecoveryRoomEndTime(OTRecoveryRoomBillingEndRequest request) {	
		try {
			
			OTRecoveryRoomBillingResponse response = billingFeignClient.setRecoveryRoomEndTime(request);
			
			log.info("Recovery room End Time created — operationId: {}", request.getOperationExternalId());
			return response;
			
		} catch (Exception e) {
			log.error("Billing service error — Revicery Room End Time Not Set — operationId: {}, error: {}",
					request.getOperationExternalId(), e.getMessage());
			return null;
		}
	}
	
	
	
	//------------------- Update Reciver Room Billing -----------------------//
	public OTRecoveryRoomBillingResponse updateRecoveryRoom(Long recoveryId,
			OTRecoveryRoomBillingUpdateRequest request) {
		try {

			OTRecoveryRoomBillingResponse response = billingFeignClient.updateRecoveryRoom(recoveryId, request);

			log.info("Recovery room billing updated — recoveryId: {}", recoveryId);

			return response;

		} catch (Exception e) {
			log.error("Billing service error — updateRecoveryRoom — recoveryId: {}, error: {}", recoveryId,
					e.getMessage());
			return null;
		}
	}
}