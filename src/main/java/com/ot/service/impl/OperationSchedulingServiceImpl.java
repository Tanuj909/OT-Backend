package com.ot.service.impl;

import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ot.billing.service.OTBillingIntegrationService;
import com.ot.dto.scheduleOperation.AssignedOperationResponse;
import com.ot.dto.scheduleOperation.OperationListResponse;
import com.ot.dto.scheduleOperation.OperationStatusResponse;
import com.ot.dto.scheduleOperation.ScheduleOperationRequest;
import com.ot.embed.StaffAssignment;
import com.ot.embed.SurgeonAssignment;
import com.ot.entity.Hospital;
import com.ot.entity.OTRoom;
import com.ot.entity.OTSchedule;
import com.ot.entity.ScheduledOperation;
import com.ot.entity.User;
import com.ot.enums.OperationStatus;
import com.ot.enums.RoleType;
import com.ot.enums.RoomStatus;
import com.ot.enums.ScheduleType;
import com.ot.enums.StaffRole;
import com.ot.enums.SurgeonRole;
import com.ot.exception.ApiException;
import com.ot.exception.BadRequestException;
import com.ot.exception.ResourceNotFoundException;
import com.ot.exception.UnauthorizedException;
import com.ot.exception.ValidationException;
import com.ot.mapper.OperationMapper;
import com.ot.repository.HospitalRepository;
import com.ot.repository.OTRoomRepository;
import com.ot.repository.OTScheduleRepository;
import com.ot.repository.ScheduledOperationRepository;
import com.ot.security.CustomUserDetails;
import com.ot.service.OperationSchedulingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Slf4j
public class OperationSchedulingServiceImpl implements OperationSchedulingService{

    private final ScheduledOperationRepository operationRepository;
    private final OTRoomRepository roomRepository;
    private final OTScheduleRepository scheduleRepository;
    private final HospitalRepository hospitalRepository;
    
    @Autowired
    private OTBillingIntegrationService billingIntegrationService;
    
	public User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No user is logged in");
        }
        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal(); 
        return cud.getUser(); 
    }
	
	@Override
	public List<OperationListResponse> getRequestedOperations() {

	    User user = currentUser();

	    List<ScheduledOperation> operations =
	            operationRepository.findByHospitalIdAndStatus(
	                    user.getHospital().getId(),
	                    OperationStatus.REQUESTED
	            );

	    return operations.stream()
	            .map(OperationMapper::toListResponse)
	            .toList();
	}

	
	@Override
	public List<OperationListResponse> getAllOperations() {

	    User user = currentUser();

	    // 🔹 Best: DB level filtering (recommended)
	    List<ScheduledOperation> operations =
	            operationRepository.findByHospitalId(user.getHospital().getId());

	    // 🔹 Mapping
	    return operations.stream()
	            .map(OperationMapper::toListResponse)
	            .toList();
	}
	
//	For Staff
	@Override
	public List<AssignedOperationResponse> getMyAssignedOperations(List<String> statuses) {

	    User currentUser = currentUser();
	    Long userId = currentUser.getId();
	    Long hospitalId = currentUser.getHospital().getId();

	    // Default statuses — agar filter nahi diya
	    List<String> effectiveStatuses = (statuses != null && !statuses.isEmpty())
	            ? statuses
	            : List.of(
	                OperationStatus.SCHEDULED.name(),
	                OperationStatus.IN_PROGRESS.name()
	            );

	    List<ScheduledOperation> operations = new ArrayList<>();

	    // Role ke hisaab se query
	    RoleType role = currentUser.getRole();

	    if (Set.of(RoleType.SURGEON, RoleType.ANESTHESIOLOGIST).contains(role)) {
	        // Surgeon table mein dhundho
	        operations = operationRepository.findOperationsBySurgeonId(
	                userId, hospitalId, effectiveStatuses);

	        // Agar surgeon table mein nahi mila — staff table mein bhi check karo
	        if (operations.isEmpty()) {
	            operations = operationRepository.findOperationsByStaffId(
	                    userId, hospitalId, effectiveStatuses);
	        }

	    } else {
	        // Baaki sab staff table mein honge
	        operations = operationRepository.findOperationsByStaffId(
	                userId, hospitalId, effectiveStatuses);
	    }

	    return operations.stream()
	            .map(op -> mapToAssignedResponse(op, userId, role))
	            .collect(Collectors.toList());
	}
	
	@Override
	public List<OperationListResponse> getOperationsByStatus(OperationStatus status) {

	    User user = currentUser();

	    // 🔹 DB level filtering (BEST PRACTICE)
	    List<ScheduledOperation> operations =
	            operationRepository.findByHospitalIdAndStatus(
	                    user.getHospital().getId(),
	                    status
	            );

	    // 🔹 Mapping
	    return operations.stream()
	            .map(OperationMapper::toListResponse)
	            .toList();
	}
	
	
	@Override
	public OperationStatusResponse getOperationStatus(Long operationId) {

	    User user = currentUser();

	    ScheduledOperation operation = operationRepository.findById(operationId)
	            .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

	    // Hospital check
	    if (!operation.getHospital().getId().equals(user.getHospital().getId())) {
	        throw new UnauthorizedException("You are not authorized to access this operation");
	    }

	    OperationStatus status = operation.getStatus();

	    boolean isScheduled = status.equals(OperationStatus.SCHEDULED);
	    boolean isStarted = status.equals(OperationStatus.IN_PROGRESS)
	            || status.equals(OperationStatus.COMPLETED);
	    boolean isCompleted = status.equals(OperationStatus.COMPLETED);

	    return OperationStatusResponse.builder()
	            .operationId(operation.getId())
	            .status(status)
	            .isScheduled(isScheduled)
	            .isStarted(isStarted)
	            .isCompleted(isCompleted)
	            .scheduledStartTime(operation.getScheduledStartTime())
	            .actualStartTime(operation.getActualStartTime())
	            .build();
	}
	


	private AssignedOperationResponse mapToAssignedResponse(
	        ScheduledOperation op, Long userId, RoleType role) {

	    // Primary surgeon
	    String primarySurgeon = op.getSupportingSurgeons().stream()
	            .filter(SurgeonAssignment::isPrimary)
	            .map(SurgeonAssignment::getSurgeonName)
	            .findFirst().orElse(null);

	    // Is user ka assigned role
	    String assignedRole = op.getSupportingSurgeons().stream()
	            .filter(s -> s.getSurgeonId().equals(userId))
	            .map(s -> s.getRole().name())
	            .findFirst()
	            .orElseGet(() -> op.getSupportingStaff().stream()
	                    .filter(s -> s.getStaffId().equals(userId))
	                    .map(s -> s.getRole().name())
	                    .findFirst()
	                    .orElse(role.name()));

	    return AssignedOperationResponse.builder()
	            .operationId(op.getId())
	            .operationReference(op.getOperationReference())
	            .patientName(op.getPatientName())
	            .patientMrn(op.getPatientMrn())
	            .procedureName(op.getProcedureName())
	            .complexity(op.getComplexity())
	            .status(op.getStatus())
	            .scheduledStartTime(op.getScheduledStartTime())
	            .scheduledEndTime(op.getScheduledEndTime())
	            .actualStartTime(op.getActualStartTime())
	            .roomNumber(op.getRoom() != null ? op.getRoom().getRoomNumber() : null)
	            .roomName(op.getRoom() != null ? op.getRoom().getRoomName() : null)
	            .primarySurgeon(primarySurgeon)
	            .assignedRole(assignedRole)
	            .build();
	}
	
    @Transactional
    @Override
    public void schedule(Long operationId, ScheduleOperationRequest request) {
    	
    	User user = currentUser();

        ScheduledOperation operation =
                operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));
        
        //Check Hospital Avaliblity
        Hospital hospital = hospitalRepository.findById(operation.getHospital().getId())
        		.orElseThrow(()-> new ResourceNotFoundException("Hospital Not Found"));
        
        OTRoom room =
                roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        
        if (!operation.getHospital().getId().equals(room.getHospital().getId())) {
            throw new ValidationException("Operation cannot be scheduled in another hospital room!");
        }
        
        // user authorization
        Long userHospitalId = user.getHospital().getId();
        
        //Now Check if the Admin or Receptionist Hospital Id is same as ScheduledOperation & Room
        if (!operation.getHospital().getId().equals(userHospitalId)
                || !room.getHospital().getId().equals(userHospitalId)) {

            throw new UnauthorizedException("You are not authorized to schedule operation");
        }
        
        // room status validation
        if(!room.getStatus().equals(RoomStatus.AVAILABLE)) {
        	throw new BadRequestException("Room Is not Available");
        }
        
        // room active validation
        if (!Boolean.TRUE.equals(room.getIsActive())) {
            throw new ApiException("Room is inactive", HttpStatus.CONFLICT);
        }
        
        // time validation
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new ValidationException("Start time must be before end time");
        }
        
        // operation status validation
        if (!operation.getStatus().equals(OperationStatus.REQUESTED)) {
            throw new ValidationException("Operation already scheduled or processed");
        }
        
        boolean conflict = scheduleRepository.existsByRoomAndTimeOverlap(
                room,
                request.getStartTime(),
                request.getEndTime()
        );

        if (conflict) {
            throw new ValidationException("Room already booked for this time slot");
        }
        
        // update operation
        operation.setRoom(room);
        
        // ADD karo — SurgeonAssignment table mein daalo
        if (request.getSurgeonId() != null) {
            SurgeonAssignment primarySurgeon = SurgeonAssignment.builder()
                    .surgeonId(request.getSurgeonId())
                    .surgeonName(request.getSurgeonName())
                    .role(SurgeonRole.LEAD_SURGEON)
                    .isPrimary(true)
                    .build();

            operation.getSupportingSurgeons().add(primarySurgeon);
        }
        
        // ADD karo — Anesthesiologist
        if (request.getAnesthesiologistId() != null) {
            StaffAssignment anesthesiologist = StaffAssignment.builder()
                    .staffId(request.getAnesthesiologistId())
                    .staffName(request.getAnesthesiologistName())
                    .role(StaffRole.ANESTHESIOLOGIST)
                    .build();
            operation.getSupportingStaff().add(anesthesiologist);
        }
        
        operation.setScheduledStartTime(request.getStartTime());
        operation.setScheduledEndTime(request.getEndTime());
        operation.setStatus(OperationStatus.SCHEDULED);

        // block OT slot
        OTSchedule schedule = OTSchedule.builder()
                .room(room)
                .scheduleDate(request.getStartTime())
                .hospital(operation.getHospital())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .type(ScheduleType.SURGERY)
                .blockedBy(user.getUserName())
                .createdBy(user.getUserName())
                .build();

        scheduleRepository.save(schedule);
        
        // 👇 Billing trigger — last mein
        Long billingMasterId = billingIntegrationService.createBillingMaster(operation);
        
        if (billingMasterId != null) {
            operation.setBillingMasterId(billingMasterId);
        } else {
            log.warn("BillingMaster creation failed for operationId: {}", operation.getId());
        }
        
        //Single Save With single Db Hit!
        operationRepository.save(operation);
    }
}
