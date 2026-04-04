package com.ot.billing.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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

//package com.ot.billing.client

@FeignClient(
 name = "billing-service",
 url = "${billing.service.url}"
)
public interface BillingFeignClient {

    //------------------------------------------Create Billing Master------------------------------------------//
	@PostMapping("/api/billing/master/create")
	BillingApiResponse<BillingMasterResponse> createBillingMaster(@RequestBody BillingMasterCreateRequest request);

	
	//------------------------------------------Get Billing Master------------------------------------------//
	@GetMapping("/api/billing/master/operation/{operationId}")
	BillingApiResponse<BillingMasterData> getBillingByOperationId(@PathVariable("operationId") Long operationId);
	
	//-----------------------------------Make Advance Payment(Optional)-----------------------------------//
	@PostMapping("/api/billing/ot/payment/advance")
	BillingApiResponse<OTPaymentResponse> makeAdvancePayment(@RequestBody OTAdvancePaymentRequest request);
	
	//-----------------------------------Create OT Billing Details-----------------------------------//
	@PostMapping("/api/billing/ot/create")
	BillingApiResponse<OTBillingDetailsResponse> createOTBillingDetails(@RequestBody OTBillingDetailsRequest request);
	
	//-----------------------------------Add OT Items to Billing-----------------------------------//
	@PostMapping("/api/billing/ot/items/add")
	BillingApiResponse<OTItemBillingResponse> addItem(@RequestBody OTItemBillingRequest request);
	
	
	
// // OTBillingDetails
// @PostMapping("/api/billing/ot/create")
// BillingApiResponse<BillingDetailsData> createOTBillingDetails(
//         @RequestBody BillingDetailsCreateRequest request);
//
 
// // Staff
// @PostMapping("/api/billing/ot/staff/add")
// BillingApiResponse<?> addStaffBilling(
//         @RequestBody BillingStaffRequest request);
//
// @DeleteMapping("/api/billing/ot/staff/{staffBillingId}/remove")
// BillingApiResponse<?> removeStaffBilling(
//         @PathVariable("staffBillingId") Long staffBillingId);
//
// // Room
// @PostMapping("/api/billing/ot/room/create")
// BillingApiResponse<?> createRoomBilling(
//         @RequestBody BillingRoomRequest request);
//
// @PatchMapping("/api/billing/ot/room/end-time")
// BillingApiResponse<?> setRoomEndTime(
//         @RequestBody BillingRoomEndRequest request);
//
// // Items
// @PostMapping("/api/billing/ot/items/add")
// BillingApiResponse<?> addItemBilling(
//         @RequestBody BillingItemRequest request);
//
// @DeleteMapping("/api/billing/ot/items/{itemBillingId}/remove")
// BillingApiResponse<?> removeItemBilling(
//         @PathVariable("itemBillingId") Long itemBillingId);
}