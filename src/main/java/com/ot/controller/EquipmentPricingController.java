package com.ot.controller;

import com.ot.dto.equipment.EquipmentPricingRequest;
import com.ot.dto.equipment.EquipmentPricingResponse;
import com.ot.service.EquipmentPricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment/pricing")
@RequiredArgsConstructor
public class EquipmentPricingController {

    private final EquipmentPricingService pricingService;

    @PostMapping
    public EquipmentPricingResponse create(@RequestBody EquipmentPricingRequest request) {
        return pricingService.createPricing(request);
    }

    @PutMapping("/{id}")
    public EquipmentPricingResponse update(
            @PathVariable Long id,
            @RequestBody EquipmentPricingRequest request) {
        return pricingService.updatePricing(id, request);
    }

    @GetMapping("/{id}")
    public EquipmentPricingResponse getById(@PathVariable Long id) {
        return pricingService.getById(id);
    }

    @GetMapping("/equipment/{equipmentId}")
    public EquipmentPricingResponse getByEquipment(@PathVariable Long equipmentId) {
        return pricingService.getByEquipment(equipmentId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        pricingService.deletePricing(id);
    }
}