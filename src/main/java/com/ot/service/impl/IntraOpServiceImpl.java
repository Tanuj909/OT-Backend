package com.ot.service.impl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ot.dto.intraOp.AnesthesiaTimeRequest;
import com.ot.dto.intraOp.IntraOpRequest;
import com.ot.dto.intraOp.IntraOpResponse;
import com.ot.dto.intraOp.IntraOpStatusRequest;
import com.ot.dto.intraOp.IntraOpSummaryResponse;
import com.ot.embed.StaffAssignment;
import com.ot.embed.SurgeonAssignment;
import com.ot.entity.IntraOpRecord;
import com.ot.entity.ScheduledOperation;
import com.ot.entity.User;
import com.ot.enums.OperationStatus;
import com.ot.enums.StaffRole;
import com.ot.enums.SurgeryStatus;
import com.ot.enums.VolumeUnit;
import com.ot.exception.ResourceNotFoundException;
import com.ot.exception.UnauthorizedException;
import com.ot.mapper.IVFluidMapper;
import com.ot.mapper.IntraOpMapper;
import com.ot.repository.IntraOpRepository;
import com.ot.repository.ScheduledOperationRepository;
import com.ot.security.CustomUserDetails;
import com.ot.service.IntraOpService;

import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IntraOpServiceImpl implements IntraOpService{
	
	private final ScheduledOperationRepository operationRepository;
	private final IntraOpRepository intraOpRepository;
	
	public User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No user is logged in");
        }
        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal(); 
        return cud.getUser(); 
    }
	
	
	@Transactional
	@Override
	public IntraOpResponse createIntraOpRecord(Long operationId, IntraOpRequest request) {

	    User currentUser = currentUser();

	    // 1. Operation fetch
	    ScheduledOperation operation = operationRepository.findById(operationId)
	            .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

	    // 2. Same hospital check
	    if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
	        throw new UnauthorizedException("You are not authorized to access this operation");
	    }

	    // 3. Status check — sirf IN_PROGRESS operation ka IntraOp ban sakta hai
	    if (!operation.getStatus().equals(OperationStatus.IN_PROGRESS)) {
	        throw new ValidationException("IntraOp record can only be created for IN_PROGRESS operations, current status: " + operation.getStatus());
	    }

	    // 4. Already exists check
	    if (operation.getIntraOp() != null) {
	        throw new ValidationException("IntraOp record already exists for this operation");
	    }

	    // 5. Build and save
	    IntraOpRecord intraOp = IntraOpRecord.builder()
	            .hospital(operation.getHospital())
	            .scheduledOperation(operation)
	            .anesthesiaStartTime(LocalDateTime.now())   // auto set
	            .procedurePerformed(request.getProcedurePerformed())
	            .incisionType(request.getIncisionType())
	            .woundClosure(request.getWoundClosure())
	            .bloodLoss(request.getBloodLoss())
	            .bloodLossUnit(request.getBloodLossUnit())
	            .urineOutput(request.getUrineOutput())
	            .drainOutput(request.getDrainOutput())
	            .intraOpFindings(request.getIntraOpFindings())
	            .specimensCollected(request.getSpecimensCollected())
	            .implantsUsed(request.getImplantsUsed())
	            .complications(request.getComplications())
	            .interventions(request.getInterventions())
	            .status(SurgeryStatus.IN_PROGRESS)          // auto set
	            .createdBy(currentUser.getUserName())        // security se auto set
	            .build();

	    intraOpRepository.save(intraOp);

	    return IntraOpMapper.toResponse(intraOp, operation.getActualStartTime());
	}

	// Get
	@Override
	public IntraOpResponse getIntraOpRecord(Long operationId) {

	    User currentUser = currentUser();

	    ScheduledOperation operation = operationRepository.findById(operationId)
	            .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

	    if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
	        throw new UnauthorizedException("You are not authorized to access this operation");
	    }

	    if (operation.getIntraOp() == null) {
	        throw new ResourceNotFoundException("IntraOp record not found for this operation");
	    }

	    return IntraOpMapper.toResponse(operation.getIntraOp(), operation.getActualStartTime());
	}

	// Update
	@Transactional
	@Override
	public IntraOpResponse updateIntraOpRecord(Long operationId, IntraOpRequest request) {

	    User currentUser = currentUser();

	    ScheduledOperation operation = operationRepository.findById(operationId)
	            .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

	    if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
	        throw new UnauthorizedException("You are not authorized to access this operation");
	    }

	    IntraOpRecord intraOp = operation.getIntraOp();
	    if (intraOp == null) {
	        throw new ResourceNotFoundException("IntraOp record not found for this operation");
	    }

	    // COMPLETED ya ABORTED ho gayi toh update nahi
	    if (intraOp.getStatus().equals(SurgeryStatus.COMPLETED) ||
	        intraOp.getStatus().equals(SurgeryStatus.ABORTED)) {
	        throw new ValidationException("Cannot update — surgery is already " + intraOp.getStatus());
	    }

	    // Partial update
	    if (request.getProcedurePerformed() != null)  intraOp.setProcedurePerformed(request.getProcedurePerformed());
	    if (request.getIncisionType() != null)         intraOp.setIncisionType(request.getIncisionType());
	    if (request.getWoundClosure() != null)         intraOp.setWoundClosure(request.getWoundClosure());
	    if (request.getBloodLoss() != null)            intraOp.setBloodLoss(request.getBloodLoss());
	    if (request.getBloodLossUnit() != null)        intraOp.setBloodLossUnit(request.getBloodLossUnit());
	    if (request.getUrineOutput() != null)          intraOp.setUrineOutput(request.getUrineOutput());
	    if (request.getDrainOutput() != null)          intraOp.setDrainOutput(request.getDrainOutput());
	    if (request.getIntraOpFindings() != null)      intraOp.setIntraOpFindings(request.getIntraOpFindings());
	    if (request.getSpecimensCollected() != null)   intraOp.setSpecimensCollected(request.getSpecimensCollected());
	    if (request.getImplantsUsed() != null)         intraOp.setImplantsUsed(request.getImplantsUsed());
	    if (request.getComplications() != null)        intraOp.setComplications(request.getComplications());
	    if (request.getInterventions() != null)        intraOp.setInterventions(request.getInterventions());

	    intraOpRepository.save(intraOp);

	    return IntraOpMapper.toResponse(intraOp, operation.getActualStartTime());
	}
	
	
	@Transactional
	@Override
	public IntraOpResponse updateIntraOpStatus(Long operationId, IntraOpStatusRequest request) {

	    User currentUser = currentUser();

	    ScheduledOperation operation = operationRepository.findById(operationId)
	            .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

	    if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
	        throw new UnauthorizedException("You are not authorized to access this operation");
	    }

	    IntraOpRecord intraOp = operation.getIntraOp();
	    if (intraOp == null) {
	        throw new ResourceNotFoundException("IntraOp record not found");
	    }

	    if (request.getStatus() == null) {
	        throw new ValidationException("Status cannot be null");
	    }

	    // Same status check
	    if (intraOp.getStatus().equals(request.getStatus())) {
	        throw new ValidationException("IntraOp is already in " + request.getStatus() + " status");
	    }

	    // Reason mandatory for ABORTED and COMPLICATED
	    if ((request.getStatus().equals(SurgeryStatus.ABORTED) ||
	         request.getStatus().equals(SurgeryStatus.COMPLICATED))
	         && (request.getReason() == null || request.getReason().isBlank())) {
	        throw new ValidationException("Reason is mandatory for " + request.getStatus() + " status");
	    }

	    // Valid transition check
	    validateSurgeryStatusTransition(intraOp.getStatus(), request.getStatus());

	    intraOp.setStatus(request.getStatus());

	    // ABORTED ya COMPLETED ho gayi — operation status bhi update karo
	    if (request.getStatus().equals(SurgeryStatus.ABORTED)) {
	        operation.setStatus(OperationStatus.CANCELLED);
	        operation.setActualEndTime(LocalDateTime.now());
	    }

	    intraOpRepository.save(intraOp);
	    operationRepository.save(operation);

	    return IntraOpMapper.toResponse(intraOp, operation.getActualStartTime());
	}

	private void validateSurgeryStatusTransition(SurgeryStatus current, SurgeryStatus next) {
	    Map<SurgeryStatus, Set<SurgeryStatus>> allowed = Map.of(
	        SurgeryStatus.IN_PROGRESS,  Set.of(SurgeryStatus.COMPLETED, SurgeryStatus.ABORTED, SurgeryStatus.COMPLICATED),
	        SurgeryStatus.COMPLICATED,  Set.of(SurgeryStatus.COMPLETED, SurgeryStatus.ABORTED),
	        SurgeryStatus.COMPLETED,    Set.of(),   // terminal
	        SurgeryStatus.ABORTED,      Set.of()    // terminal
	    );

	    if (!allowed.get(current).contains(next)) {
	        throw new ValidationException("Invalid status transition: " + current + " → " + next);
	    }
	}
	
	@Transactional
	@Override
	public IntraOpResponse updateAnesthesiaTime(Long operationId, AnesthesiaTimeRequest request) {

	    User currentUser = currentUser();

	    ScheduledOperation operation = operationRepository.findById(operationId)
	            .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

	    if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
	        throw new UnauthorizedException("You are not authorized to access this operation");
	    }

	    IntraOpRecord intraOp = operation.getIntraOp();
	    if (intraOp == null) {
	        throw new ResourceNotFoundException("IntraOp record not found");
	    }

	    // Completed ya Aborted ho gayi toh update nahi
	    if (intraOp.getStatus().equals(SurgeryStatus.COMPLETED) ||
	        intraOp.getStatus().equals(SurgeryStatus.ABORTED)) {
	        throw new ValidationException("Cannot update anesthesia time — surgery is already " + intraOp.getStatus());
	    }

	    // End time before start time check
	    if (request.getAnesthesiaStartTime() != null &&
	        request.getAnesthesiaEndTime() != null &&
	        request.getAnesthesiaEndTime().isBefore(request.getAnesthesiaStartTime())) {
	        throw new ValidationException("Anesthesia end time cannot be before start time");
	    }

	    if (request.getAnesthesiaStartTime() != null) intraOp.setAnesthesiaStartTime(request.getAnesthesiaStartTime());
	    if (request.getAnesthesiaEndTime() != null)   intraOp.setAnesthesiaEndTime(request.getAnesthesiaEndTime());

	    intraOpRepository.save(intraOp);

	    return IntraOpMapper.toResponse(intraOp, operation.getActualStartTime());
	}
	
	
	@Override
	public IntraOpSummaryResponse getIntraOpSummary(Long operationId) {

	    User currentUser = currentUser();

	    ScheduledOperation operation = operationRepository.findById(operationId)
	            .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

	    if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
	        throw new UnauthorizedException("You are not authorized to access this operation");
	    }

	    IntraOpRecord intraOp = operation.getIntraOp();
	    if (intraOp == null) {
	        throw new ResourceNotFoundException("IntraOp record not found");
	    }

	    // Surgery duration calculate
	    Long surgeryDuration = null;
	    if (operation.getActualStartTime() != null && operation.getActualEndTime() != null) {
	        surgeryDuration = ChronoUnit.MINUTES.between(operation.getActualStartTime(), operation.getActualEndTime());
	    }

	    // Anesthesia duration calculate
	    Long anesthesiaDuration = null;
	    if (intraOp.getAnesthesiaStartTime() != null && intraOp.getAnesthesiaEndTime() != null) {
	        anesthesiaDuration = ChronoUnit.MINUTES.between(intraOp.getAnesthesiaStartTime(), intraOp.getAnesthesiaEndTime());
	    }

	    // Total IV fluids in ML
	    Integer totalIVFluidsML = intraOp.getIvFluids().stream()
	            .mapToInt(f -> f.getUnit().equals(VolumeUnit.LITERS) ? f.getVolume() * 1000 : f.getVolume())
	            .sum();

	    // Primary surgeon
	    String primarySurgeon = operation.getSupportingSurgeons().stream()
	            .filter(SurgeonAssignment::isPrimary)
	            .map(SurgeonAssignment::getSurgeonName)
	            .findFirst()
	            .orElse("Not assigned");

	    // Anesthesiologist
	    String anesthesiologist = operation.getSupportingStaff().stream()
	            .filter(s -> s.getRole().equals(StaffRole.ANESTHESIOLOGIST))
	            .map(StaffAssignment::getStaffName)
	            .findFirst()
	            .orElse("Not assigned");

	    return IntraOpSummaryResponse.builder()
	            .operationId(operation.getId())
	            .operationReference(operation.getOperationReference())
	            .patientName(operation.getPatientName())
	            .procedureName(operation.getProcedureName())
	            .surgeryStartTime(operation.getActualStartTime())
	            .surgeryEndTime(operation.getActualEndTime())
	            .surgeryDurationMinutes(surgeryDuration)
	            .anesthesiaStartTime(intraOp.getAnesthesiaStartTime())
	            .anesthesiaEndTime(intraOp.getAnesthesiaEndTime())
	            .anesthesiaDurationMinutes(anesthesiaDuration)
	            .procedurePerformed(intraOp.getProcedurePerformed())
	            .incisionType(intraOp.getIncisionType())
	            .woundClosure(intraOp.getWoundClosure())
	            .intraOpFindings(intraOp.getIntraOpFindings())
	            .complications(intraOp.getComplications())
	            .interventions(intraOp.getInterventions())
	            .specimensCollected(intraOp.getSpecimensCollected())
	            .implantsUsed(intraOp.getImplantsUsed())
	            .totalBloodLoss(intraOp.getBloodLoss())
	            .bloodLossUnit(intraOp.getBloodLossUnit())
	            .urineOutput(intraOp.getUrineOutput())
	            .drainOutput(intraOp.getDrainOutput())
	            .totalIVFluidsML(totalIVFluidsML)
	            .ivFluids(intraOp.getIvFluids().stream().map(IVFluidMapper::toResponse).collect(Collectors.toList()))
	            .primarySurgeon(primarySurgeon)
	            .anesthesiologist(anesthesiologist)
	            .status(intraOp.getStatus())
	            .build();
	}

	
	
}
