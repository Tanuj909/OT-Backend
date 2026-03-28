package com.ot.mapper;

import org.springframework.stereotype.Component;

import com.ot.dto.otRoom.OTRoomPricingRequest;
import com.ot.dto.otRoom.OTRoomPricingResponse;
import com.ot.entity.OTRoomPricing;

@Component
public class OTRoomPricingMapper {

    public OTRoomPricing toEntity(OTRoomPricingRequest request) {
        return OTRoomPricing.builder()
                .roomId(request.getRoomId())
                .basePrice(request.getBasePrice())
                .hourlyRate(request.getHourlyRate())
                .emergencyCharge(request.getEmergencyCharge())
                .cleaningCharge(request.getCleaningCharge())
                .build();
    }

    public OTRoomPricingResponse toResponse(OTRoomPricing entity) {
        return OTRoomPricingResponse.builder()
                .id(entity.getId())
                .roomId(entity.getRoomId())
                .basePrice(entity.getBasePrice())
                .hourlyRate(entity.getHourlyRate())
                .emergencyCharge(entity.getEmergencyCharge())
                .cleaningCharge(entity.getCleaningCharge())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
