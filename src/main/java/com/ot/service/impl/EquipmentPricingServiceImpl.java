package com.ot.service.impl;

import com.ot.dto.equipment.EquipmentPricingRequest;
import com.ot.dto.equipment.EquipmentPricingResponse;
import com.ot.entity.Equipment;
import com.ot.entity.EquipmentPricing;
import com.ot.entity.User;
import com.ot.repository.EquipmentPricingRepository;
import com.ot.repository.EquipmentRepository;
import com.ot.security.CustomUserDetails;
import com.ot.service.EquipmentPricingService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EquipmentPricingServiceImpl implements EquipmentPricingService {

    private final EquipmentPricingRepository pricingRepository;
    private final EquipmentRepository equipmentRepository;
    
    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No user is logged in");
        }
        return ((CustomUserDetails) auth.getPrincipal()).getUser();
    }

    @Override
    public EquipmentPricingResponse createPricing(EquipmentPricingRequest request) {
    	
    	User user = currentUser();

        Equipment equipment = equipmentRepository.findById(request.getEquipmentId())
                .orElseThrow(() -> new RuntimeException("Equipment not found"));

        EquipmentPricing pricing = EquipmentPricing.builder()
                .equipment(equipment)
                .pricingType(request.getPricingType())
                .rate(request.getRate())
                .unit(request.getUnit())
                .isActive(request.getIsActive())
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .createdBy(user.getEmail())
                .build();

        pricingRepository.save(pricing);

        return mapToResponse(pricing);
    }

    @Override
    public EquipmentPricingResponse updatePricing(Long id, EquipmentPricingRequest request) {

        EquipmentPricing pricing = pricingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pricing not found"));

        pricing.setPricingType(request.getPricingType());
        pricing.setRate(request.getRate());
        pricing.setUnit(request.getUnit());
        pricing.setIsActive(request.getIsActive());
        pricing.setEffectiveFrom(request.getEffectiveFrom());
        pricing.setEffectiveTo(request.getEffectiveTo());

        pricingRepository.save(pricing);

        return mapToResponse(pricing);
    }

    @Override
    public EquipmentPricingResponse getById(Long id) {

        EquipmentPricing pricing = pricingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pricing not found"));

        return mapToResponse(pricing);
    }

    @Override
    public EquipmentPricingResponse getByEquipment(Long equipmentId) {

        EquipmentPricing pricing = pricingRepository.findByEquipmentId(equipmentId);
        
        return mapToResponse(pricing);
    }

    @Override
    public void deletePricing(Long id) {
        pricingRepository.deleteById(id);
    }

    // 🔁 Mapper
    private EquipmentPricingResponse mapToResponse(EquipmentPricing pricing) {
        return EquipmentPricingResponse.builder()
                .id(pricing.getId())
                .equipmentId(pricing.getEquipment().getId())
                .equipmentName(pricing.getEquipment().getName())
                .pricingType(pricing.getPricingType())
                .rate(pricing.getRate())
                .unit(pricing.getUnit())
                .isActive(pricing.getIsActive())
                .effectiveFrom(pricing.getEffectiveFrom())
                .effectiveTo(pricing.getEffectiveTo())
                .build();
    }
}