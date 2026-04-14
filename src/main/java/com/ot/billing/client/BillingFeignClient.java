package com.ot.billing.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
import com.ot.dto.billing.OTRoomBillingEndRequest;
import com.ot.dto.billing.OTRoomBillingRequest;
import com.ot.dto.billing.OTRoomBillingResponse;
import com.ot.dto.billing.OTStaffBillingRequest;
import com.ot.dto.billing.OTStaffBillingResponse;

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
	
	//-----------------------------------Add Staff Billing-----------------------------------//
	@PostMapping("/api/billing/ot/staff/add")
	BillingApiResponse<OTStaffBillingResponse> addStaffBilling(
	        @RequestBody OTStaffBillingRequest request);
	
	//-----------------------------------Add OT Items to Billing-----------------------------------//
	@PostMapping("/api/billing/ot/items/add")
	BillingApiResponse<OTItemBillingResponse> addItem(@RequestBody OTItemBillingRequest request);
	
	
	//-----------------------------------Remove OT Items to Billing-----------------------------------//
	@DeleteMapping("/api/billing/ot/items/{itemBillingId}/remove")
	BillingApiResponse<Void> removeItem(
	        @PathVariable("itemBillingId") Long itemBillingId);
	
	//-----------------------------------Update OT Items to Billing-----------------------------------//
	@PutMapping("/api/billing/ot/items/{itemBillingId}/update")
	BillingApiResponse<OTItemBillingResponse> updateItem(
	        @PathVariable("itemBillingId") Long itemBillingId,
	        @RequestBody OTItemBillingUpdateRequest request);
	
	
    // ==================== ROOM BILLING ==================== //
    @PostMapping("/api/billing/ot/room/create")
    BillingApiResponse<OTRoomBillingResponse> createRoomBilling(
            @RequestBody OTRoomBillingRequest request);
    
    
    // ==================== Set Room End Time ==================== //
    @PostMapping("/api/billing/ot/room/end-time")
    BillingApiResponse<OTRoomBillingResponse> setEndTime(
            @RequestBody OTRoomBillingEndRequest request);
    
    
    // ==================== Close Billing ==================== //
    @PostMapping("/api/billing/ot/operation/{operationId}/close")
    BillingApiResponse<OTBillingDetailsResponse> closeBilling(
            @PathVariable("operationId") Long operationId);
    
    
//----------------------------------------------------Get API's-----------------------------------------------------------------------//
    
    // ==================== Get Billing Details By Operation ID ==================== //
    @GetMapping("/api/billing/ot/operation/{operationId}")
    BillingApiResponse<OTBillingDetailsResponse> getOTBillingById(
            @PathVariable("operationId") Long operationId);
    
    
    // ==================== Get Room Billing Details By Operation ID ==================== //
    @GetMapping("/api/billing/ot/room/operation/{operationId}")
    BillingApiResponse<List<OTRoomBillingResponse>> getRoomBillingByOperationId(
            @PathVariable("operationId") Long operationId);
    
    
    // ==================== Get Item Billing Details By Operation ID ==================== //
    @GetMapping("/api/billing/ot/items/operation/{operationId}")
    BillingApiResponse<List<OTItemBillingResponse>> getItemsByOperationId(
            @PathVariable("operationId") Long operationId);
    
    
    // ==================== Get Operation Billing Summary ==================== //
    @GetMapping("/api/billing/ot/operation/{operationId}/summary")
    BillingApiResponse<OTBillingSummaryResponse> getBillingSummary(
            @PathVariable("operationId") Long operationId);
    
    
//-----------------------------------------------Payment API's--------------------------------------------------//
    
    // ==================== Make Payment ==================== //
    @PostMapping("/api/billing/ot/payment/make")
    BillingApiResponse<OTPaymentResponse> makePayment(
            @RequestBody OTPaymentRequest request);
    
    
    // ==================== Payment History ==================== //
    @GetMapping("/api/billing/ot/payment/operation/{operationId}/history")
    BillingApiResponse<OTPaymentHistoryResponse> getPaymentHistory(
            @PathVariable("operationId") Long operationId);
}