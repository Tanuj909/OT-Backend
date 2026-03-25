package com.ot.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ot.dto.equipment.EquipmentAttributeRequest;
import com.ot.dto.equipment.EquipmentAttributeResponse;
import com.ot.dto.response.ApiResponse;
import com.ot.service.EquipmentAttributeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/equipment/{equipmentId}/attributes")
@RequiredArgsConstructor
public class EquipmentAttributeController {

    private final EquipmentAttributeService attributeService;

    @PostMapping
    public ResponseEntity<ApiResponse<EquipmentAttributeResponse>> addAttribute(
            @PathVariable Long equipmentId,
            @RequestBody EquipmentAttributeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Attribute added successfully",
                        attributeService.addAttribute(equipmentId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EquipmentAttributeResponse>>> getAttributes(
            @PathVariable Long equipmentId) {
        return ResponseEntity.ok(ApiResponse.success("Attributes fetched successfully",
                attributeService.getAttributes(equipmentId)));
    }
    
    @GetMapping("/{attributeId}")
    public ResponseEntity<ApiResponse<EquipmentAttributeResponse>> getAttributeById(
            @PathVariable Long attributeId) {
        
        EquipmentAttributeResponse response = attributeService.getAttributeById(attributeId);
        
        return ResponseEntity.ok(
            ApiResponse.success("Attribute fetched successfully", response)
        );
    }

    @PutMapping("/{attributeId}")
    public ResponseEntity<ApiResponse<EquipmentAttributeResponse>> updateAttribute(
            @PathVariable Long equipmentId,
            @PathVariable Long attributeId,
            @RequestBody EquipmentAttributeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Attribute updated successfully",
                attributeService.updateAttribute(equipmentId, attributeId, request)));
    }

    @DeleteMapping("/{attributeId}")
    public ResponseEntity<ApiResponse<Void>> deleteAttribute(
            @PathVariable Long equipmentId,
            @PathVariable Long attributeId) {
        attributeService.deleteAttribute(equipmentId, attributeId);
        return ResponseEntity.ok(ApiResponse.success("Attribute deleted successfully", null));
    }
}
