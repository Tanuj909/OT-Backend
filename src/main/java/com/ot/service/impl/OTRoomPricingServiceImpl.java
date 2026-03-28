package com.ot.service.impl;

import org.springframework.stereotype.Service;

import com.ot.dto.otRoom.OTRoomPricingRequest;
import com.ot.dto.otRoom.OTRoomPricingResponse;
import com.ot.entity.OTRoomPricing;
import com.ot.exception.ResourceNotFoundException;
import com.ot.exception.ValidationException;
import com.ot.mapper.OTRoomPricingMapper;
import com.ot.repository.OTRoomPricingRepository;
import com.ot.repository.OTRoomRepository;
import com.ot.service.OTRoomPricingService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class OTRoomPricingServiceImpl implements OTRoomPricingService {

    private final OTRoomPricingRepository repository;
    private final OTRoomPricingMapper mapper;

    // 🔗 If same service (OT), you can inject roomRepository
    private final OTRoomRepository roomRepository;

    // 🔥 1. Create Pricing
    @Override
    public OTRoomPricingResponse create(OTRoomPricingRequest request) {

        // ✅ Check room exists
        if (!roomRepository.existsById(request.getRoomId())) {
            throw new ResourceNotFoundException("Room not found with id: " + request.getRoomId());
        }

        // ✅ Check pricing already exists
        if (repository.existsByRoomId(request.getRoomId())) {
            throw new ValidationException("Pricing already exists for this room");
        }

        OTRoomPricing pricing = mapper.toEntity(request);

        return mapper.toResponse(repository.save(pricing));
    }

    // 🔥 2. Get Pricing by Room
    @Override
    public OTRoomPricingResponse getByRoomId(Long roomId) {

        OTRoomPricing pricing = repository.findByRoomId(roomId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pricing not found for roomId: " + roomId
                ));

        return mapper.toResponse(pricing);
    }

    // 🔥 3. Update Pricing
    @Override
    public OTRoomPricingResponse update(Long roomId, OTRoomPricingRequest request) {

        OTRoomPricing pricing = repository.findByRoomId(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing not found"));

        pricing.setBasePrice(request.getBasePrice());
        pricing.setHourlyRate(request.getHourlyRate());
        pricing.setEmergencyCharge(request.getEmergencyCharge());
        pricing.setCleaningCharge(request.getCleaningCharge());

        return mapper.toResponse(repository.save(pricing));
    }
}
