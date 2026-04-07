package com.ot.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ot.dto.billing.OTItemBillingRequest;
import com.ot.dto.equipment.EquipmentPricingResponse;
import com.ot.dto.equipment.EquipmentRequest;
import com.ot.dto.equipment.EquipmentResponse;
import com.ot.entity.Equipment;
import com.ot.entity.EquipmentPricing;
import com.ot.entity.User;
import com.ot.enums.EquipmentStatus;
import com.ot.exception.ResourceNotFoundException;
import com.ot.exception.UnauthorizedException;
import com.ot.mapper.EquipmentMapper;
import com.ot.repository.EquipmentRepository;
import com.ot.security.CustomUserDetails;
import com.ot.service.EquipmentPricingService;
import com.ot.service.EquipmentService;

import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentRepository equipmentRepository;


    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No user is logged in");
        }
        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
        return cud.getUser();
    }

    // ---------------------------------------- Add ---------------------------------------- //

    @Transactional
    @Override
    public EquipmentResponse addEquipment(EquipmentRequest request) {

        User currentUser = currentUser();

        // AssetCode duplicate check
        if (request.getAssetCode() != null &&
            equipmentRepository.existsByAssetCode(request.getAssetCode())) {
            throw new ValidationException("Equipment with asset code " + request.getAssetCode() + " already exists");
        }
        
        Equipment equipment = Equipment.builder()
                .hospital(currentUser.getHospital())
                .name(request.getName())
                .model(request.getModel())
                .manufacturer(request.getManufacturer())
                .serialNumber(request.getSerialNumber())
                .assetCode(request.getAssetCode())
                .status(request.getStatus() != null ? request.getStatus() : EquipmentStatus.OPERATIONAL)
                .category(request.getCategory())
                .purchaseDate(request.getPurchaseDate())
                .lastMaintenanceDate(request.getLastMaintenanceDate())
                .nextMaintenanceDate(request.getNextMaintenanceDate())
                .capabilities(request.getCapabilities() != null ? request.getCapabilities() : new HashSet<>())
                .createdBy(currentUser.getUserName())
                .build();

        equipmentRepository.save(equipment);
        
        return EquipmentMapper.toResponse(equipment);
    }

    // ---------------------------------------- Get ---------------------------------------- //

    @Override
    public EquipmentResponse getEquipment(Long equipmentId) {

        User currentUser = currentUser();

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found"));

        // Same hospital check
        if (!equipment.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this equipment");
        }

        return EquipmentMapper.toResponse(equipment);
    }

    // --------------------------------------- Get All --------------------------------------- //

    @Override
    public List<EquipmentResponse> getAllEquipment() {

        User currentUser = currentUser();

        return equipmentRepository.findAllByHospitalId(currentUser.getHospital().getId())
                .stream()
                .map(EquipmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    // --------------------------------------- Update --------------------------------------- //

    @Transactional
    @Override
    public EquipmentResponse updateEquipment(Long equipmentId, EquipmentRequest request) {

        User currentUser = currentUser();

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found"));

        // Same hospital check
        if (!equipment.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to update this equipment");
        }

        // AssetCode duplicate check — apna hi assetCode ho toh skip karo
        if (request.getAssetCode() != null &&
            !request.getAssetCode().equals(equipment.getAssetCode()) &&
            equipmentRepository.existsByAssetCode(request.getAssetCode())) {
            throw new ValidationException("Equipment with asset code " + request.getAssetCode() + " already exists");
        }

        // Partial update
        if (request.getName() != null)                equipment.setName(request.getName());
        if (request.getModel() != null)               equipment.setModel(request.getModel());
        if (request.getManufacturer() != null)        equipment.setManufacturer(request.getManufacturer());
        if (request.getSerialNumber() != null)        equipment.setSerialNumber(request.getSerialNumber());
        if (request.getAssetCode() != null)           equipment.setAssetCode(request.getAssetCode());
        if (request.getStatus() != null)              equipment.setStatus(request.getStatus());
        if (request.getCategory() != null)            equipment.setCategory(request.getCategory());
        if (request.getPurchaseDate() != null)        equipment.setPurchaseDate(request.getPurchaseDate());
        if (request.getLastMaintenanceDate() != null) equipment.setLastMaintenanceDate(request.getLastMaintenanceDate());
        if (request.getNextMaintenanceDate() != null) equipment.setNextMaintenanceDate(request.getNextMaintenanceDate());
        if (request.getCapabilities() != null)        equipment.setCapabilities(request.getCapabilities());

        equipment.setUpdatedBy(currentUser.getUserName());

        equipmentRepository.save(equipment);

        return EquipmentMapper.toResponse(equipment);
    }

    // --------------------------------------- Delete --------------------------------------- //

    @Transactional
    @Override
    public void deleteEquipment(Long equipmentId) {

        User currentUser = currentUser();

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found"));

        // Same hospital check
        if (!equipment.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to delete this equipment");
        }

        // RETIRED status check — active equipment delete nahi hogi
        if (!equipment.getStatus().equals(EquipmentStatus.RETIRED)) {
            throw new ValidationException("Only RETIRED equipment can be deleted. Current status: " + equipment.getStatus());
        }

        equipmentRepository.delete(equipment);
    }

    // ---------------------------------- Update Status ------------------------------------ //

    @Transactional
    @Override
    public EquipmentResponse updateEquipmentStatus(Long equipmentId, EquipmentStatus status) {

        User currentUser = currentUser();

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found"));

        if (!equipment.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to update this equipment");
        }

        equipment.setStatus(status);
        equipment.setUpdatedBy(currentUser.getUserName());

        equipmentRepository.save(equipment);

        return EquipmentMapper.toResponse(equipment);
    }
}