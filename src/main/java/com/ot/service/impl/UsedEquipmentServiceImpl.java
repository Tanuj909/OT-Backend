package com.ot.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ot.dto.equipment.UsedEquipmentRequest;
import com.ot.dto.equipment.UsedEquipmentResponse;
import com.ot.entity.Equipment;
import com.ot.entity.ScheduledOperation;
import com.ot.entity.UsedEquipment;
import com.ot.entity.User;
import com.ot.enums.EquipmentStatus;
import com.ot.enums.OperationStatus;
import com.ot.exception.ResourceNotFoundException;
import com.ot.exception.UnauthorizedException;
import com.ot.exception.ValidationException;
import com.ot.repository.EquipmentRepository;
import com.ot.repository.ScheduledOperationRepository;
import com.ot.repository.UsedEquipmentRepository;
import com.ot.security.CustomUserDetails;
import com.ot.service.UsedEquipmentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsedEquipmentServiceImpl implements UsedEquipmentService {

    private final UsedEquipmentRepository usedEquipmentRepository;
    private final EquipmentRepository equipmentRepository;
    private final ScheduledOperationRepository operationRepository;

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
    public UsedEquipmentResponse addEquipmentToOperation(Long operationId, UsedEquipmentRequest request) {

        User currentUser = currentUser();

        // 1. Operation fetch
        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        // 2. Same hospital check
        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        // 3. Operation IN_PROGRESS honi chahiye
        if (!operation.getStatus().equals(OperationStatus.IN_PROGRESS)) {
            throw new ValidationException("Equipment can only be added to IN_PROGRESS operations");
        }

        // 4. Equipment fetch
        Equipment equipment = equipmentRepository.findById(request.getEquipmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found"));

        // 5. Same hospital check for equipment
        if (!equipment.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("Equipment does not belong to your hospital");
        }

        // 6. Equipment OPERATIONAL honi chahiye
        if (!equipment.getStatus().equals(EquipmentStatus.OPERATIONAL)) {
            throw new ValidationException("Equipment is not operational. Current status: " + equipment.getStatus());
        }

        // 7. Duplicate check — same equipment already added?
        boolean alreadyAdded = usedEquipmentRepository
                .existsByScheduledOperationIdAndEquipmentId(operationId, request.getEquipmentId());
        if (alreadyAdded) {
            throw new ValidationException("Equipment already added to this operation");
        }

        // 8. Build and save
        UsedEquipment usedEquipment = UsedEquipment.builder()
                .hospital(currentUser.getHospital())
                .scheduledOperation(operation)
                .equipment(equipment)
                .quantityUsed(request.getQuantityUsed())
                .isConsumable(request.isConsumable())
                .usedFrom(request.getUsedFrom() != null ? request.getUsedFrom() : LocalDateTime.now())
                .usedUntil(request.getUsedUntil())
                .build();

        usedEquipmentRepository.save(usedEquipment);

        return toResponse(usedEquipment);
    }

    // ---------------------------------------- Get ---------------------------------------- //

    @Override
    public List<UsedEquipmentResponse> getUsedEquipment(Long operationId) {

        User currentUser = currentUser();

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        return usedEquipmentRepository.findAllByScheduledOperationId(operationId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    // ---------------------------------------- Update ---------------------------------------- //
    @Transactional
    @Override
    public UsedEquipmentResponse updateUsageDetails(Long operationId, Long usedEquipmentId, UsedEquipmentRequest request) {

        User currentUser = currentUser();

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        if (!operation.getStatus().equals(OperationStatus.IN_PROGRESS)) {
            throw new ValidationException("Equipment can only be updated during IN_PROGRESS operations");
        }

        UsedEquipment usedEquipment = usedEquipmentRepository.findById(usedEquipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Used equipment record not found"));

        if (!usedEquipment.getScheduledOperation().getId().equals(operationId)) {
            throw new ValidationException("Equipment record does not belong to this operation");
        }

        // ✅ Update fields (partial update safe)
        if (request.getUsedFrom() != null) {
            usedEquipment.setUsedFrom(request.getUsedFrom());
        }

        if (request.getUsedUntil() != null) {
            usedEquipment.setUsedUntil(request.getUsedUntil());
        }

        if (request.getQuantityUsed() != null) {
            usedEquipment.setQuantityUsed(request.getQuantityUsed());
        }

        // boolean ke liye null check nahi hota → always update
        usedEquipment.setConsumable(request.isConsumable());

        usedEquipmentRepository.save(usedEquipment);

        return toResponse(usedEquipment);
    }

    // --------------------------------------- Update Used Till--------------------------------------- //

    @Transactional
    @Override
    public UsedEquipmentResponse updateUsedEquipment(Long operationId, Long usedEquipmentId, UsedEquipmentRequest request) {

        User currentUser = currentUser();

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        if (!operation.getStatus().equals(OperationStatus.IN_PROGRESS)) {
            throw new ValidationException("Equipment can only be updated during IN_PROGRESS operations");
        }

        UsedEquipment usedEquipment = usedEquipmentRepository.findById(usedEquipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Used equipment record not found"));

        // Belongs to this operation?
        if (!usedEquipment.getScheduledOperation().getId().equals(operationId)) {
            throw new ValidationException("Equipment record does not belong to this operation");
        }

        if (request.getQuantityUsed() != null) usedEquipment.setQuantityUsed(request.getQuantityUsed());
        if (request.getUsedUntil() != null)    usedEquipment.setUsedUntil(request.getUsedUntil());

        usedEquipmentRepository.save(usedEquipment);

        return toResponse(usedEquipment);
    }

    // --------------------------------------- Remove --------------------------------------- //

    @Transactional
    @Override
    public void removeEquipmentFromOperation(Long operationId, Long usedEquipmentId) {

        User currentUser = currentUser();

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        if (!operation.getStatus().equals(OperationStatus.IN_PROGRESS)) {
            throw new ValidationException("Equipment can only be removed during IN_PROGRESS operations");
        }

        UsedEquipment usedEquipment = usedEquipmentRepository.findById(usedEquipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Used equipment record not found"));

        if (!usedEquipment.getScheduledOperation().getId().equals(operationId)) {
            throw new ValidationException("Equipment record does not belong to this operation");
        }

        usedEquipmentRepository.delete(usedEquipment);
    }

    // --------------------------------------- Mapper --------------------------------------- //

    private UsedEquipmentResponse toResponse(UsedEquipment usedEquipment) {
        return UsedEquipmentResponse.builder()
                .id(usedEquipment.getId())
                .operationId(usedEquipment.getScheduledOperation().getId())
                .equipmentId(usedEquipment.getEquipment().getId())
                .equipmentName(usedEquipment.getEquipment().getName())
                .assetCode(usedEquipment.getEquipment().getAssetCode())
                .category(usedEquipment.getEquipment().getCategory())
                .quantityUsed(usedEquipment.getQuantityUsed())
                .isConsumable(usedEquipment.isConsumable())
                .usedFrom(usedEquipment.getUsedFrom())
                .usedUntil(usedEquipment.getUsedUntil())
                .createdAt(usedEquipment.getCreatedAt())
                .build();
    }
}
