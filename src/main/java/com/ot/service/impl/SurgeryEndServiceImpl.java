package com.ot.service.impl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ot.billing.service.OTBillingIntegrationService;
import com.ot.constants.OTRoleConstants;
import com.ot.dto.billing.OTRoomBillingEndRequest;
import com.ot.dto.billing.OTStaffBillingRequest;
import com.ot.dto.staffRequest.StaffFeeResponse;
import com.ot.dto.surgeryEnd.SurgeryEndRequest;
import com.ot.dto.surgeryEnd.SurgeryEndResponse;
import com.ot.dto.surgeryEnd.SurgeryReadinessResponse;
import com.ot.embed.SurgeonAssignment;
import com.ot.entity.IntraOpRecord;
import com.ot.entity.OTRoom;
import com.ot.entity.PostOpRecord;
import com.ot.entity.ScheduledOperation;
import com.ot.entity.UsedEquipment;
import com.ot.entity.User;
import com.ot.enums.AssessmentStatus;
import com.ot.enums.OperationStatus;
import com.ot.enums.RecoveryStatus;
import com.ot.enums.RoomStatus;
import com.ot.enums.StaffRole;
import com.ot.enums.SurgeryStatus;
import com.ot.enums.VitalsPhase;
import com.ot.exception.ResourceNotFoundException;
import com.ot.exception.UnauthorizedException;
import com.ot.exception.ValidationException;
import com.ot.repository.IntraOpRepository;
import com.ot.repository.OTRoomRepository;
import com.ot.repository.PostOpRecordRepository;
import com.ot.repository.ScheduledOperationRepository;
import com.ot.repository.UsedEquipmentRepository;
import com.ot.repository.VitalsLogRepository;
import com.ot.security.CustomUserDetails;
import com.ot.service.StaffFeeService;
import com.ot.service.SurgeryEndService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SurgeryEndServiceImpl implements SurgeryEndService {

    private final ScheduledOperationRepository operationRepository;
    private final PostOpRecordRepository postOpRepository;
    private final IntraOpRepository intraOpRepository;
    private final OTRoomRepository roomRepository;
    private final VitalsLogRepository vitalsLogRepository;
    private final UsedEquipmentRepository usedEquipmentRepository;
    private final OTBillingIntegrationService billingIntegrationService;
    private final StaffFeeService staffFeeService;

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No user is logged in");
        }
        return ((CustomUserDetails) auth.getPrincipal()).getUser();
    }

//    @Transactional
//    @Override
//    public SurgeryEndResponse endSurgery(Long operationId, SurgeryEndRequest request) {
//
//        User currentUser = currentUser();
//
//        // 1. Role check
//        if (!OTRoleConstants.ALLOWED_SURGERY_START_ROLES.contains(currentUser.getRole())) {
//            throw new ValidationException("User with role " + currentUser.getRole() + " cannot end surgery");
//        }
//
//        // 2. Operation fetch
//        ScheduledOperation operation = operationRepository.findById(operationId)
//                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));
//
//        // 3. Hospital check
//        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
//            throw new UnauthorizedException("You are not authorized to access this operation");
//        }
//
//        // 4. Status check — sirf IN_PROGRESS end ho sakti hai
//        if (!operation.getStatus().equals(OperationStatus.IN_PROGRESS)) {
//            throw new ValidationException("Only IN_PROGRESS operations can be ended, current status: "
//                    + operation.getStatus());
//        }
//
//        // 5. Already ended check
//        if (operation.getPostOp() != null) {
//            throw new ValidationException("Surgery already ended for this operation");
//        }
//
//        LocalDateTime endTime = LocalDateTime.now();
//
//        // 6. Operation update
//        operation.setStatus(OperationStatus.COMPLETED);
//        operation.setActualEndTime(endTime);
//        operation.setUpdatedBy(currentUser.getUserName());
//
//        // 7. IntraOp COMPLETED
//        IntraOpRecord intraOp = operation.getIntraOp();
//        if (intraOp != null) {
//            intraOp.setStatus(SurgeryStatus.COMPLETED);
//            intraOpRepository.save(intraOp);
//        }
//
//        // 8. OTRoom — UNDER_CLEANING (cleaning ke baad AVAILABLE hoga)
//        OTRoom room = operation.getRoom();
//        if (room != null) {
//            room.setStatus(RoomStatus.CLEANING);
//            roomRepository.save(room);
//        }
//
//        // 9. Surgery duration calculate
//        long durationMinutes = 0;
//        if (operation.getActualStartTime() != null) {
//            durationMinutes = ChronoUnit.MINUTES.between(operation.getActualStartTime(), endTime);
//        }
//
//        // 10. PostOpRecord auto create
//        PostOpRecord postOp = PostOpRecord.builder()
//                .hospital(operation.getHospital())
//                .scheduledOperation(operation)
//                .surgeryEndTime(endTime)
//                .recoveryStartTime(LocalDateTime.now())
//                .recoveryLocation(request.getRecoveryLocation())
//                .immediatePostOpCondition(request.getImmediatePostOpCondition())
//                .drainDetails(request.getDrainDetails())
//                .dressingDetails(request.getDressingDetails())
//                .status(RecoveryStatus.IN_RECOVERY)
//                .createdBy(currentUser.getUserName())
//                .build();
//
//        postOpRepository.save(postOp);
//        operationRepository.save(operation);
//
//        return SurgeryEndResponse.builder()
//                .operationId(operation.getId())
//                .operationStatus(operation.getStatus())
//                .actualStartTime(operation.getActualStartTime())
//                .actualEndTime(endTime)
//                .surgeryDurationMinutes(durationMinutes)
//                .endedBy(currentUser.getUserName())
//                .postOpRecordId(postOp.getId())
//                .build();
//    }
    
    @Override
    public SurgeryReadinessResponse getSurgeryReadiness(Long operationId) {

        User currentUser = currentUser();

        // 1. Operation fetch
        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        // 2. Hospital check
        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        // ==================== Start Checks ==================== //

        boolean preOpCompleted = operation.getPreOp() != null
                && operation.getPreOp().getStatus().equals(AssessmentStatus.COMPLETED);

        boolean primarySurgeonAssigned = operation.getSupportingSurgeons().stream()
                .anyMatch(SurgeonAssignment::isPrimary);

        boolean anesthesiologistAssigned = operation.getSupportingStaff().stream()
                .anyMatch(s -> s.getRole().equals(StaffRole.ANESTHESIOLOGIST));

        boolean roomAssigned = operation.getRoom() != null;

        boolean canStart = primarySurgeonAssigned
                && anesthesiologistAssigned
                && roomAssigned
                && operation.getStatus().equals(OperationStatus.SCHEDULED);

        // ==================== End Checks ==================== //

        IntraOpRecord intraOp = operation.getIntraOp();

        boolean intraOpExists = intraOp != null;

        boolean intraOpCompleted = intraOpExists
                && intraOp.getStatus().equals(SurgeryStatus.COMPLETED);

        boolean woundClosureFilled = intraOpExists
                && intraOp.getWoundClosure() != null
                && !intraOp.getWoundClosure().isBlank();

        boolean procedurePerformedFilled = intraOpExists
                && intraOp.getProcedurePerformed() != null
                && !intraOp.getProcedurePerformed().isBlank();

        boolean bloodLossRecorded = intraOpExists
                && intraOp.getBloodLoss() != null;

        // All drugs ka endTime set hai?
        boolean allDrugsEndTimeSet = intraOpExists
                && !intraOp.getAnesthesiaDrugs().isEmpty()
                && intraOp.getAnesthesiaDrugs().stream()
                        .allMatch(drug -> drug.getEndTime() != null);

        
        // Equipment check
        boolean allEquipmentEndTimeSet = true; // default true — agar equipment hi nahi use hua
        
        List<UsedEquipment> usedEquipments = usedEquipmentRepository
                .findByScheduledOperation(operation);
        
        if (!usedEquipments.isEmpty()) {
            allEquipmentEndTimeSet = usedEquipments.stream()
                    .allMatch(e -> e.getUsedUntil() != null);
        }
        
        // IV Fluid check
        boolean allIVFluidsEndTimeSet = true; // default true — agar IV fluid hi nahi diya

        if (intraOpExists && !intraOp.getIvFluids().isEmpty()) {
            allIVFluidsEndTimeSet = intraOp.getIvFluids().stream()
                    .allMatch(f -> f.getEndTime() != null);
        }
        
        boolean vitalsRecorded = vitalsLogRepository
                .findTopByScheduledOperationAndPhaseOrderByRecordedTimeDesc(
                        operation, VitalsPhase.INTRA_OP)
                .isPresent();

        boolean canEnd = intraOpExists
                && intraOpCompleted
                && woundClosureFilled
                && procedurePerformedFilled
                && bloodLossRecorded
                && allDrugsEndTimeSet
                && vitalsRecorded
                && allIVFluidsEndTimeSet         // 👈 NEW
                && allEquipmentEndTimeSet       // 👈 NEW
                && operation.getStatus().equals(OperationStatus.IN_PROGRESS);
        
        
        return SurgeryReadinessResponse.builder()
                .operationId(operation.getId())
                .currentStatus(operation.getStatus())
                .canStart(canStart)
                .canEnd(canEnd)
                .checks(SurgeryReadinessResponse.SurgeryReadinessChecks.builder()
                        .preOpCompleted(preOpCompleted)
                        .primarySurgeonAssigned(primarySurgeonAssigned)
                        .anesthesiologistAssigned(anesthesiologistAssigned)
                        .roomAssigned(roomAssigned)
                        .intraOpExists(intraOpExists)
                        .intraOpCompleted(intraOpCompleted)
                        .woundClosureFilled(woundClosureFilled)
                        .procedurePerformedFilled(procedurePerformedFilled)
                        .bloodLossRecorded(bloodLossRecorded)
                        .allDrugsEndTimeSet(allDrugsEndTimeSet)
                        .allIVFluidsEndTimeSet(allIVFluidsEndTimeSet)
                        .allEquipmentEndTimeSet(allEquipmentEndTimeSet)
                        .vitalsRecorded(vitalsRecorded)
                        .build())
                .build();
    }
    
    @Transactional
    @Override
    public SurgeryEndResponse endSurgery(Long operationId, SurgeryEndRequest request) {

        User currentUser = currentUser();

        // 1. Role check
        if (!OTRoleConstants.ALLOWED_SURGERY_START_ROLES.contains(currentUser.getRole())) {
            throw new ValidationException("User with role " + currentUser.getRole() + " cannot end surgery");
        }

        // 2. Operation fetch
        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        // 3. Hospital check
        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        // 4. Status check
        if (!operation.getStatus().equals(OperationStatus.IN_PROGRESS)) {
            throw new ValidationException("Only IN_PROGRESS operations can be ended, current status: "
                    + operation.getStatus());
        }

        // 5. Already ended check
        if (operation.getPostOp() != null) {
            throw new ValidationException("Surgery already ended for this operation");
        }

        // ==================== MANDATORY PRE-CHECKS ==================== //

        List<String> failedChecks = new ArrayList<>();

        // Check 1 — IntraOp Record exist kare
        IntraOpRecord intraOp = operation.getIntraOp();
        if (intraOp == null) {
            failedChecks.add("IntraOp record not found");
        } else {

            // Check 2 — IntraOp Status COMPLETED ho
            if (!intraOp.getStatus().equals(SurgeryStatus.COMPLETED)) {
                failedChecks.add("IntraOp status is not COMPLETED, current status: " + intraOp.getStatus());
            }

            // Check 3 — Wound Closure filled ho
            if (intraOp.getWoundClosure() == null || intraOp.getWoundClosure().isBlank()) {
                failedChecks.add("Wound closure details are required");
            }

            // Check 4 — Procedure Performed filled ho
            if (intraOp.getProcedurePerformed() == null || intraOp.getProcedurePerformed().isBlank()) {
                failedChecks.add("Procedure performed details are required");
            }

            // Check 5 — Blood Loss recorded ho
            if (intraOp.getBloodLoss() == null) {
                failedChecks.add("Blood loss must be recorded");
            }

            // Check 6 — Anesthesia End Time set ho
//            if (intraOp.getAnesthesiaEndTime() == null) {
//                failedChecks.add("Anesthesia end time must be set");
//            }
        }

        // Check 7 — Vitals last entry recorded ho
        boolean hasVitals = vitalsLogRepository
                .findTopByScheduledOperationAndPhaseOrderByRecordedTimeDesc(
                        operation, VitalsPhase.INTRA_OP)
                .isPresent();
        if (!hasVitals) {
            failedChecks.add("At least one vitals entry must be recorded");
        }

        // ==================== Agar koi check fail hua ==================== //
        if (!failedChecks.isEmpty()) {
            throw new ValidationException("Surgery cannot be ended. Failed checks: " + failedChecks);
        }

        // ==================== All checks passed — End Surgery ==================== //

        LocalDateTime endTime = LocalDateTime.now();

        // Operation update
        operation.setStatus(OperationStatus.COMPLETED);
        operation.setActualEndTime(endTime);
        operation.setUpdatedBy(currentUser.getUserName());

        // IntraOp update
        intraOp.setStatus(SurgeryStatus.COMPLETED);
        intraOpRepository.save(intraOp);

        // OTRoom → UNDER_CLEANING
        OTRoom room = operation.getRoom();
        if (room != null) {
            room.setStatus(RoomStatus.CLEANING);
            roomRepository.save(room);
        }

        // Surgery duration calculate
        long durationMinutes = 0;
        if (operation.getActualStartTime() != null) {
            durationMinutes = ChronoUnit.MINUTES.between(operation.getActualStartTime(), endTime);
        }

        // PostOpRecord auto create
        PostOpRecord postOp = PostOpRecord.builder()
                .hospital(operation.getHospital())
                .scheduledOperation(operation)
                .surgeryEndTime(endTime)
                .recoveryStartTime(LocalDateTime.now())
                .recoveryLocation(request.getRecoveryLocation())
                .immediatePostOpCondition(request.getImmediatePostOpCondition())
                .drainDetails(request.getDrainDetails())
                .dressingDetails(request.getDressingDetails())
                .status(RecoveryStatus.IN_RECOVERY)
                .createdBy(currentUser.getUserName())
                .build();

        postOpRepository.save(postOp);
        operationRepository.save(operation);
        
        //----------------Setting the End time for The room in the Billing--------------//
        // 1. Room end time
        OTRoomBillingEndRequest endRequest = new OTRoomBillingEndRequest();

        endRequest.setOperationExternalId(operation.getId());
        endRequest.setEndTime(endTime);

        billingIntegrationService.setRoomEndTime(endRequest);
        
        
     // ==================== STAFF BILLING ADD ==================== //

     // 🔹 Surgeons
     operation.getSupportingSurgeons().forEach(surgeon -> {

         StaffFeeResponse fee = staffFeeService.getByStaffId(surgeon.getSurgeonId());

         OTStaffBillingRequest billingRequest = new OTStaffBillingRequest();
         billingRequest.setOperationExternalId(operation.getId());
         billingRequest.setStaffExternalId(surgeon.getSurgeonId());
         billingRequest.setStaffName(surgeon.getSurgeonName());
         billingRequest.setStaffRole(surgeon.getRole().name());

         billingRequest.setFees(fee.getOtFee());

         billingIntegrationService.addStaffBilling(billingRequest);
     });


     // 🔹 Staff (Anesthesiologist, Nurse etc.)
     operation.getSupportingStaff().forEach(staff -> {

         StaffFeeResponse fee = staffFeeService.getByStaffId(staff.getStaffId());

         OTStaffBillingRequest billingRequest = new OTStaffBillingRequest();
         billingRequest.setOperationExternalId(operation.getId());
         billingRequest.setStaffExternalId(staff.getStaffId());
         billingRequest.setStaffName(staff.getStaffName());
         billingRequest.setStaffRole(staff.getRole().name());

         billingRequest.setFees(fee.getOtFee());

         billingIntegrationService.addStaffBilling(billingRequest);
     });
        
        
        // 2. 🔥 Billing close (yahi add karna hai)
        // Abhi Temprory Disable kiya hai billing close yha nhi hogi, jab patient Discharge Hoga Tab hogi Billing Close!
//        billingIntegrationService.closeBilling(operation.getId());  

        return SurgeryEndResponse.builder()
                .operationId(operation.getId())
                .operationStatus(operation.getStatus())
                .actualStartTime(operation.getActualStartTime())
                .actualEndTime(endTime)
                .surgeryDurationMinutes(durationMinutes)
                .endedBy(currentUser.getUserName())
                .postOpRecordId(postOp.getId())
                .build();
    }
}
