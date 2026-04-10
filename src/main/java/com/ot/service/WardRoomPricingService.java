package com.ot.service;

import com.ot.dto.ward.WardRoomPricingRequest;
import com.ot.dto.ward.WardRoomPricingResponse;

public interface WardRoomPricingService {

	WardRoomPricingResponse createWardRoomPricing(WardRoomPricingRequest request);

	WardRoomPricingResponse getByRoomId(Long roomId);

	WardRoomPricingResponse update(Long wardRoomId, WardRoomPricingRequest request);

}
