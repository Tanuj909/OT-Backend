package com.ot.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ot.billing.service.OTBillingIntegrationService;
import com.ot.constants.OTRoleConstants;
import com.ot.dto.billing.OTRoomBillingRequest;
import com.ot.dto.surgeryResponse.SurgeryStartResponse;
import com.ot.dto.surgeryResponse.SurgeryStatusResponse;
import com.ot.embed.SurgeonAssignment;
import com.ot.entity.OTRoom;
import com.ot.entity.OTRoomPricing;
import com.ot.entity.ScheduledOperation;
import com.ot.entity.User;
import com.ot.enums.AssessmentStatus;
import com.ot.enums.OperationStatus;
import com.ot.enums.StaffRole;
import com.ot.exception.ResourceNotFoundException;
import com.ot.exception.UnauthorizedException;
import com.ot.exception.ValidationException;
import com.ot.repository.OTRoomPricingRepository;
import com.ot.repository.OTRoomRepository;
import com.ot.repository.ScheduledOperationRepository;
import com.ot.security.CustomUserDetails;
import com.ot.service.SurgeryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SurgeryServiceImpl implements SurgeryService{
	
	private final ScheduledOperationRepository operationRepository;
	private final OTBillingIntegrationService billingIntegrationService;
	private final OTRoomPricingRepository roomPricingRepository;
	private final OTRoomRepository otRoomRepository;
	
    // ---------------------------------------- Helper ---------------------------------------- //

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No user is logged in");
        }
        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
        return cud.getUser();
    }
    
    @Transactional
    @Override
    public SurgeryStartResponse startSurgery(Long operationId) {

        User currentUser = currentUser();
        
        // Role check
        if (!OTRoleConstants.ALLOWED_SURGERY_START_ROLES.contains(currentUser.getRole())) {
            throw new UnauthorizedException("User with role " + currentUser.getRole() + " cannot start surgery");
        }

        // 1. Operation fetch
        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        // 2. Same hospital check
        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        // 3. Status check
        if (!operation.getStatus().equals(OperationStatus.SCHEDULED)) {
            throw new ValidationException("Only SCHEDULED operations can be started, current status: " + operation.getStatus());
        }

        // ==================== MANDATORY CHECKS — block karo ==================== //

        // Primary Surgeon — MANDATORY
        boolean hasPrimarySurgeon = operation.getSupportingSurgeons().stream()
                .anyMatch(SurgeonAssignment::isPrimary);
        if (!hasPrimarySurgeon) {
            throw new ValidationException("Cannot start surgery — No primary surgeon assigned");
        }

        // Anesthesiologist — MANDATORY
        boolean hasAnesthesiologist = operation.getSupportingStaff().stream()
                .anyMatch(s -> s.getRole().equals(StaffRole.ANESTHESIOLOGIST));
        if (!hasAnesthesiologist) {
            throw new ValidationException("Cannot start surgery — No anesthesiologist assigned");
        }

        // OT Room — MANDATORY
        if (operation.getRoom() == null) {
            throw new ValidationException("Cannot start surgery — No OT room assigned");
        }

        // ==================== WARNING CHECKS — sirf warn karo ==================== //

        List<String> warnings = new ArrayList<>();

        // PreOp — WARNING only
        if (operation.getPreOp() == null) {
            warnings.add("Pre-op assessment not found");
        } else if (!operation.getPreOp().getStatus().equals(AssessmentStatus.COMPLETED)) {
            warnings.add("Pre-op assessment is not completed, current status: " + operation.getPreOp().getStatus());
        }

        
        // ==================== BILLING CHECK ==================== //

        if (operation.getBillingMasterId() == null) {
            throw new ValidationException("Cannot start surgery — Billing not initialized");
        }
        
        // ==================== Start Surgery ==================== //

        operation.setStatus(OperationStatus.IN_PROGRESS);
        operation.setActualStartTime(LocalDateTime.now());
        operation.setUpdatedBy(currentUser.getUserName());

        operationRepository.save(operation);
       
     // ==================== CREATE BILLING DETAILS ==================== //

        billingIntegrationService.createOTBillingDetails(
                operation.getBillingMasterId(),
                operation.getOperationReference()
        );
        
     // ==================== CREATE ROOM BILLING ==================== //
        
        OTRoomPricing pricing = roomPricingRepository
                .findByRoomId(operation.getRoom().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Room pricing not found"));
        
        OTRoomBillingRequest roomBillingRequest = new OTRoomBillingRequest();

        roomBillingRequest.setOperationExternalId(operation.getId());

        roomBillingRequest.setRoomNumber(String.valueOf(operation.getRoom().getId()));
        roomBillingRequest.setRoomName(operation.getRoom().getRoomName());
         // Optional fields
        roomBillingRequest.setRatePerHour(pricing.getHourlyRate());

        roomBillingRequest.setStartTime(operation.getActualStartTime());

        billingIntegrationService.createRoomBilling(roomBillingRequest);

        return SurgeryStartResponse.builder()
                .operationId(operation.getId())
                .status(operation.getStatus())
                .actualStartTime(operation.getActualStartTime())
                .startedBy(currentUser.getUserName())
                .warnings(warnings.isEmpty() ? null : warnings)
                .build();
    }
    
    @Override
    public SurgeryStatusResponse checkSurgeryStarted(Long operationId) {

        User currentUser = currentUser();

        // Fetch operation
        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        // Hospital check
        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        boolean isStarted = operation.getStatus().equals(OperationStatus.IN_PROGRESS)
                || operation.getStatus().equals(OperationStatus.COMPLETED);

        return SurgeryStatusResponse.builder()
                .operationId(operation.getId())
                .isStarted(isStarted)
                .status(operation.getStatus())
                .actualStartTime(operation.getActualStartTime())
                .build();
    }
    
    @Transactional
    @Override
    public void shiftRoomBeforeSurgery(Long operationId, Long newRoomId) {

        User currentUser = currentUser();

        // 1. Fetch Operation
        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        // 2. Hospital validation
        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to modify this operation");
        }

        // 3. Status check (VERY IMPORTANT)
        if (!operation.getStatus().equals(OperationStatus.SCHEDULED)) {
            throw new ValidationException("Room shift allowed only before surgery start");
        }

        // 4. Current room check
        if (operation.getRoom() != null && operation.getRoom().getId().equals(newRoomId)) {
            throw new ValidationException("Operation is already assigned to this room");
        }

        // 5. Fetch new room
        OTRoom newRoom = otRoomRepository.findById(newRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("New OT Room not found"));

        // 6. Room availability check (optional but recommended)
//        boolean isRoomBusy = operationRepository.existsByRoomIdAndStatusIn(
//                newRoomId,
//                List.of(OperationStatus.SCHEDULED, OperationStatus.IN_PROGRESS)
//        );

//        if (isRoomBusy) {
//            throw new ValidationException("Selected room is already occupied");
//        }

        // 7. Assign new room
        operation.setRoom(newRoom);
        operation.setUpdatedBy(currentUser.getUserName());

        operationRepository.save(operation);
    }
    
    
}
