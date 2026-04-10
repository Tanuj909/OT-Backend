package com.ot.mapper;

import org.springframework.stereotype.Component;

import com.ot.dto.ward.WardRoomPricingRequest;
import com.ot.dto.ward.WardRoomPricingResponse;
import com.ot.entity.OTRoomPricing;

@Component
public class OTWardRoomPricingMapper {
	
	
	//Making Response
	public OTRoomPricing toEntity(WardRoomPricingRequest request) {
		return OTRoomPricing.builder()
				.wardRoomId(request.getWardRoomId())
                .basePrice(request.getBasePrice())
                .hourlyRate(request.getHourlyRate())
                .emergencyCharge(request.getEmergencyCharge())
                .cleaningCharge(request.getCleaningCharge())
                .build();
		
	}
	
	
	//Generating Response
	public WardRoomPricingResponse toResponse(OTRoomPricing entity) {
		return WardRoomPricingResponse.builder()
				.id(entity.getId())
                .wardRoomId(entity.getWardRoomId())
                .basePrice(entity.getBasePrice())
                .hourlyRate(entity.getHourlyRate())
                .emergencyCharge(entity.getEmergencyCharge())
                .cleaningCharge(entity.getCleaningCharge())
                .createdAt(entity.getCreatedAt())
                .build();
		
	}

}
