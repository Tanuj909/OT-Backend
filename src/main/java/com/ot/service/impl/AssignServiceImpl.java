package com.ot.service.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

import com.ot.dto.staffRequest.StaffAssignmentDTO;
import com.ot.dto.staffRequest.StaffAssignmentRequest;
import com.ot.dto.surgeonAssignment.SurgeonAssignmentDTO;
import com.ot.dto.surgeonAssignment.SurgeonAssignmentRequest;
import com.ot.dto.surgeonAssignment.UnAssignSurgeonRequest;
import com.ot.embed.StaffAssignment;
import com.ot.embed.SurgeonAssignment;
import com.ot.entity.ScheduledOperation;
import com.ot.enums.OperationStatus;
import com.ot.exception.ResourceNotFoundException;
import com.ot.exception.ValidationException;
import com.ot.repository.ScheduledOperationRepository;
import com.ot.repository.UserRepository;
import com.ot.service.AssignService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssignServiceImpl implements AssignService {
	
	private final UserRepository userRepository;
	private final ScheduledOperationRepository operationRepository;
	
	
//-------------------------------------Assign Staff to Schedule Operations----------------------------------//
	@Transactional
	@Override
	public void assignStaff(Long operationId, StaffAssignmentRequest request) {

	    ScheduledOperation operation = operationRepository.findById(operationId)
	            .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

	    if (!operation.getStatus().equals(OperationStatus.SCHEDULED)) {
	        throw new ValidationException("Staff can only be assigned after scheduling");
	    }
	    
	    //Get Staff Id's From the Request and Store in the Set!
	    Set<Long> requestedIds = request.getStaff().stream()
	            .map(StaffAssignmentDTO::getStaffId)
	            .collect(Collectors.toSet());
	    
	    // 1. Check for duplicates WITHIN the incoming request itself
	    if(requestedIds.size() < request.getStaff().size()) {
	    	throw new ValidationException("Duplicate staff entries in request");
	    }
	    
	    // 2. Validate each staff member exists in DB
	    requestedIds.forEach(staffId-> {
	    	if(!userRepository.existsById(staffId)) {
	    		throw new ResourceNotFoundException("Staff not found with id: " + staffId);
	    	}
	    });

	    // 3. Check if any staff is ALREADY assigned to this operation
	    Set<Long> alreadyAssignedIds = operation.getSupportingStaff().stream()
	    		.map(StaffAssignment::getStaffId)
	    		.collect(Collectors.toSet());
	    
	    Set<Long> duplicates = requestedIds.stream()
	            .filter(alreadyAssignedIds::contains)
	            .collect(Collectors.toSet());
	    
	    if (!duplicates.isEmpty()) {
	        throw new ValidationException("Staff already assigned to this operation: " + duplicates);
	    }
	    
	    // 4. Map and add to existing staff set
	    Set<StaffAssignment> newStaff = request.getStaff().stream()
	            .map(dto -> {
	                StaffAssignment s = new StaffAssignment();
	                s.setStaffId(dto.getStaffId());
	                s.setStaffName(dto.getStaffName());
	                s.setRole(dto.getRole());
	                return s;
	            })
	            .collect(Collectors.toSet());

	    operation.getSupportingStaff().addAll(newStaff);

	    operationRepository.save(operation);
	}
	
	
//-------------------------------------Get Staff of Schedule Operations----------------------------------//
	@Override
	public Set<StaffAssignment> getAssignedStaff(Long operationId) {

	    ScheduledOperation operation = operationRepository.findById(operationId)
	            .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

	    return operation.getSupportingStaff();
	}
	
	
	
//-------------------------------------Un-Assign Staff From Schedule Operations----------------------------------//
	@Transactional
	@Override
	public void unAssignStaff(Long operationId, Set<Long> staffIds) {

	    ScheduledOperation operation = operationRepository.findById(operationId)
	            .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

	    if (!operation.getStatus().equals(OperationStatus.SCHEDULED)) {
	        throw new ValidationException("Staff can only be unassigned from scheduled operations");
	    }

	    // 1. Check for duplicates within the incoming request
	    if (staffIds.size() != new HashSet<>(staffIds).size()) {
	        throw new ValidationException("Duplicate staff IDs in request");
	    }

	    // 2. Get currently assigned staff IDs
	    Set<Long> assignedIds = operation.getSupportingStaff().stream()
	            .map(StaffAssignment::getStaffId)
	            .collect(Collectors.toSet());

	    // 3. Check if requested IDs are actually assigned
	    Set<Long> notAssigned = staffIds.stream()
	            .filter(id -> !assignedIds.contains(id))
	            .collect(Collectors.toSet());

	    if (!notAssigned.isEmpty()) {
	        throw new ValidationException("These staff are not assigned to this operation: " + notAssigned);
	    }

	    // 4. Remove from set
	    operation.getSupportingStaff()
	            .removeIf(s -> staffIds.contains(s.getStaffId()));

	    operationRepository.save(operation);
	}
	
	
//-------------------------------------------------Surgeon(Methods Below)-------------------------------------------------------------//

//-------------------------------------Assign Surgeon For Schedule Operations----------------------------------//
	@Transactional
	@Override
	public void assignSurgeon(Long operationId, SurgeonAssignmentRequest request) {
		
	    ScheduledOperation operation = operationRepository.findById(operationId)
	            .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

	    if (!operation.getStatus().equals(OperationStatus.SCHEDULED)) {
	        throw new ValidationException("Surgeons can only be assigned to scheduled operations");
	    }
	    
	    Set<Long> requestedIds = request.getSurgeon().stream()
	            .map(SurgeonAssignmentDTO::getSurgeonId)
	            .collect(Collectors.toSet());

	    // 1. Intra-request duplicates
	    if (requestedIds.size() < request.getSurgeon().size()) {
	        throw new ValidationException("Duplicate surgeon entries in request");
	    }
	    
	    // 2. Validate each surgeon exists in DB
	    requestedIds.forEach(surgeonId -> {
	        if (!userRepository.existsById(surgeonId)) {
	            throw new ResourceNotFoundException("Surgeon not found with id: " + surgeonId);
	        }
	    });
	    
	    // 3. Already assigned check
	    Set<Long> alreadyAssignedIds = operation.getSupportingSurgeons().stream()
	            .map(SurgeonAssignment::getSurgeonId)
	            .collect(Collectors.toSet());

	    Set<Long> duplicates = requestedIds.stream()
	            .filter(alreadyAssignedIds::contains)
	            .collect(Collectors.toSet());

	    if (!duplicates.isEmpty()) {
	        throw new ValidationException("Surgeons already assigned to this operation: " + duplicates);
	    }
	    
	 // 4. Primary surgeon check — sirf ek hi primary ho sakta hai
	    long primaryCount = request.getSurgeon().stream()
	            .filter(SurgeonAssignmentDTO::isPrimary)
	            .count();

	    boolean alreadyHasPrimary = operation.getSupportingSurgeons().stream()
	            .anyMatch(SurgeonAssignment::isPrimary);

	    if (primaryCount > 1) {
	        throw new ValidationException("Only one primary surgeon can be assigned");
	    }

	    if (primaryCount == 1 && alreadyHasPrimary) {
	        throw new ValidationException("A primary surgeon is already assigned to this operation");
	    }
	    
	    // 5. Map and add
	    Set<SurgeonAssignment> newSurgeons = request.getSurgeon().stream()
	            .map(dto -> {
	                SurgeonAssignment s = new SurgeonAssignment();
	                s.setSurgeonId(dto.getSurgeonId());
	                s.setSurgeonName(dto.getSurgeonName());
	                s.setRole(dto.getRole());
	                s.setPrimary(dto.isPrimary());
	                return s;
	            })
	            .collect(Collectors.toSet());
	    
	    operation.getSupportingSurgeons().addAll(newSurgeons);

	    operationRepository.save(operation);
	}

	
//-------------------------------------Get Surgeon of Schedule Operations----------------------------------//
	@Override
	public Set<SurgeonAssignment> getAssignedSurgeons(Long operationId) {

	    ScheduledOperation operation = operationRepository.findById(operationId)
	            .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

	    return operation.getSupportingSurgeons();
	}
	
//-------------------------------------Un-Assign Surgeon From Schedule Operations----------------------------------//
	@Transactional
	@Override
	public void unAssignSurgeon(Long operationId, UnAssignSurgeonRequest request) {

	    ScheduledOperation operation = operationRepository.findById(operationId)
	            .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

	    if (!operation.getStatus().equals(OperationStatus.SCHEDULED)) {
	        throw new ValidationException("Surgeons can only be unassigned from scheduled operations");
	    }

	    // 1. Get currently assigned surgeon IDs
	    Set<Long> assignedIds = operation.getSupportingSurgeons().stream()
	            .map(SurgeonAssignment::getSurgeonId)
	            .collect(Collectors.toSet());

	    // 2. Check if requested IDs are actually assigned
	    Set<Long> notAssigned = request.getSurgeonIds().stream()
	            .filter(id -> !assignedIds.contains(id))
	            .collect(Collectors.toSet());

	    if (!notAssigned.isEmpty()) {
	        throw new ValidationException("These surgeons are not assigned to this operation: " + notAssigned);
	    }

	    // 3. Primary surgeon unassign check
	    boolean removingPrimary = operation.getSupportingSurgeons().stream()
	            .anyMatch(s -> request.getSurgeonIds().contains(s.getSurgeonId()) && s.isPrimary());

	    if (removingPrimary) {
	        throw new ValidationException("Cannot unassign primary surgeon directly. Reassign primary first.");
	    }

	    // 4. Remove
	    operation.getSupportingSurgeons()
	            .removeIf(s -> request.getSurgeonIds().contains(s.getSurgeonId()));

	    operationRepository.save(operation);
	}
}
