package com.ot.service.impl;

import org.springframework.stereotype.Service;
import com.ot.dto.ward.WardRoomPricingRequest;
import com.ot.dto.ward.WardRoomPricingResponse;
import com.ot.entity.OTRoomPricing;
import com.ot.exception.ResourceNotFoundException;
import com.ot.exception.ValidationException;
import com.ot.mapper.OTWardRoomPricingMapper;
import com.ot.repository.OTRoomPricingRepository;
import com.ot.repository.WardRoomRepository;
import com.ot.service.WardRoomPricingService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WardRoomPricingServiceImpl implements WardRoomPricingService{
	
    private final OTRoomPricingRepository repository;
    private final OTWardRoomPricingMapper mapper;
    private final WardRoomRepository wardRoomRepository;
    
    
    // 🔥 1. Create Pricing
    @Override
    public WardRoomPricingResponse createWardRoomPricing(WardRoomPricingRequest request) {

        // ✅ Check Ward room exists
        if (!wardRoomRepository.existsById(request.getWardRoomId())) {
            throw new ResourceNotFoundException("Ward Room not found with id: " + request.getWardRoomId());
        }

        // ✅ Check pricing already exists
        if (repository.existsByRoomId(request.getWardRoomId())) {
            throw new ValidationException("Pricing already exists for this room");
        }

        OTRoomPricing pricing = mapper.toEntity(request);

        return mapper.toResponse(repository.save(pricing));
    }
    
 // 🔥 2. Get Pricing by Room
    @Override
    public WardRoomPricingResponse getByRoomId(Long wardRoomId) {

        OTRoomPricing pricing = repository.findByWardRoomId(wardRoomId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pricing not found for roomId: " + wardRoomId
                ));

        return mapper.toResponse(pricing);
    }

    // 🔥 3. Update Pricing
    @Override
    public WardRoomPricingResponse update(Long wardRoomId, WardRoomPricingRequest request) {

        OTRoomPricing pricing = repository.findByWardRoomId(wardRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing not found"));

        pricing.setBasePrice(request.getBasePrice());
        pricing.setHourlyRate(request.getHourlyRate());
        pricing.setEmergencyCharge(request.getEmergencyCharge());
        pricing.setCleaningCharge(request.getCleaningCharge());

        return mapper.toResponse(repository.save(pricing));
    }
    
    

}
