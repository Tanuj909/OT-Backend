package com.ot.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ot.dto.ward.WardRoomPricingRequest;
import com.ot.service.WardRoomPricingService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ward-room-pricing")
@RequiredArgsConstructor
public class WardRoomPricingController {
	
	private final WardRoomPricingService service;
	
	 // 🔥 Create Pricing
    @PostMapping
    public ResponseEntity<?> create(@RequestBody WardRoomPricingRequest request) {
        return ResponseEntity.ok(service.createWardRoomPricing(request));
    }

    // 🔥 Get Pricing by Room
    @GetMapping("/room/{wardRoomId}")
    public ResponseEntity<?> getByRoom(@PathVariable Long wardRoomId) {
        return ResponseEntity.ok(service.getByRoomId(wardRoomId));
    }

    // 🔥 Update Pricing
    @PutMapping("/room/{wardRoomId}")
    public ResponseEntity<?> update(@PathVariable Long wardRoomId,
                                    @RequestBody WardRoomPricingRequest request) {
        return ResponseEntity.ok(service.update(wardRoomId, request));
    }

}
