package com.ot.service;

import com.ot.dto.otRoom.OTRoomPricingRequest;
import com.ot.dto.otRoom.OTRoomPricingResponse;

public interface OTRoomPricingService {

	OTRoomPricingResponse create(OTRoomPricingRequest request);

	OTRoomPricingResponse getByRoomId(Long roomId);

	OTRoomPricingResponse update(Long roomId, OTRoomPricingRequest request);

}
