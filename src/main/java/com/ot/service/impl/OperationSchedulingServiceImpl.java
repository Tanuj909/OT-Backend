package com.ot.service.impl;


import java.awt.Checkbox;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ot.dto.scheduleOperation.ScheduleOperationRequest;
import com.ot.embed.StaffAssignment;
import com.ot.embed.SurgeonAssignment;
import com.ot.entity.Hospital;
import com.ot.entity.OTRoom;
import com.ot.entity.OTSchedule;
import com.ot.entity.ScheduledOperation;
import com.ot.entity.User;
import com.ot.enums.OperationStatus;
import com.ot.enums.RoomStatus;
import com.ot.enums.ScheduleType;
import com.ot.enums.StaffRole;
import com.ot.enums.SurgeonRole;
import com.ot.exception.BadRequestException;
import com.ot.exception.OperationNotAllowedException;
import com.ot.exception.ResourceNotFoundException;
import com.ot.exception.UnauthorizedException;
import com.ot.repository.HospitalRepository;
import com.ot.repository.OTRoomRepository;
import com.ot.repository.OTScheduleRepository;
import com.ot.repository.ScheduledOperationRepository;
import com.ot.security.CustomUserDetails;
import com.ot.service.OperationSchedulingService;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OperationSchedulingServiceImpl implements OperationSchedulingService{

    private final ScheduledOperationRepository operationRepository;
    private final OTRoomRepository roomRepository;
    private final OTScheduleRepository scheduleRepository;
    private final HospitalRepository hospitalRepository;
    
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
        if (!room.getIsActive()) {
            throw new OperationNotAllowedException("Room is inactive");
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

        operationRepository.save(operation);

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
    }
}
