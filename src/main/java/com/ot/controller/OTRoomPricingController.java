package com.ot.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ot.dto.otRoom.OTRoomPricingRequest;
import com.ot.service.OTRoomPricingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ot-room-pricing")
@RequiredArgsConstructor
public class OTRoomPricingController {

    private final OTRoomPricingService service;

    // 🔥 Create Pricing
    @PostMapping
    public ResponseEntity<?> create(@RequestBody OTRoomPricingRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    // 🔥 Get Pricing by Room
    @GetMapping("/room/{roomId}")
    public ResponseEntity<?> getByRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(service.getByRoomId(roomId));
    }

    // 🔥 Update Pricing
    @PutMapping("/room/{roomId}")
    public ResponseEntity<?> update(@PathVariable Long roomId,
                                    @RequestBody OTRoomPricingRequest request) {
        return ResponseEntity.ok(service.update(roomId, request));
    }
}
