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

//package com.ot.billing.client

@FeignClient(
 name = "billing-service",
 url = "${billing.service.url}"
)
public interface BillingFeignClient {

    @PostMapping("/api/billing/master/create")
    BillingApiResponse<BillingMasterResponse> createBillingMaster(
            @RequestBody BillingMasterCreateRequest request);

 @GetMapping("/api/billing/master/operation/{operationId}")
 BillingApiResponse<BillingMasterData> getBillingByOperationId(
         @PathVariable("operationId") Long operationId);

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