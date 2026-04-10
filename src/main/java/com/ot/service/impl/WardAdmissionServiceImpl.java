package com.ot.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ot.dto.ward.AssignWardRequest;
import com.ot.dto.ward.WardAdmissionResponse;
import com.ot.entity.ScheduledOperation;
import com.ot.entity.User;
import com.ot.entity.Ward;
import com.ot.entity.WardAdmission;
import com.ot.entity.WardBed;
import com.ot.entity.WardRoom;
import com.ot.enums.BedStatus;
import com.ot.enums.OperationStatus;
import com.ot.exception.OperationNotAllowedException;
import com.ot.exception.ResourceNotFoundException;
import com.ot.exception.UnauthorizedException;
import com.ot.exception.ValidationException;
import com.ot.repository.ScheduledOperationRepository;
import com.ot.repository.WardAdmissionRepository;
import com.ot.repository.WardBedRepository;
import com.ot.repository.WardRoomRepository;
import com.ot.security.CustomUserDetails;
import com.ot.service.WardAdmissionService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WardAdmissionServiceImpl implements WardAdmissionService {

    private final WardAdmissionRepository wardAdmissionRepository;
    private final ScheduledOperationRepository scheduledOperationRepository;
    private final WardRoomRepository wardRoomRepository;
    private final WardBedRepository wardBedRepository;

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No user is logged in");
        }
        return ((CustomUserDetails) auth.getPrincipal()).getUser();
    }

    // -------------------- Assign -------------------- //

    @Transactional
    @Override
    public WardAdmissionResponse assignWard(AssignWardRequest request) {

        User currentUser = currentUser();

        // Operation fetch + hospital check
        ScheduledOperation operation = scheduledOperationRepository
                .findById(request.getOperationId())
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }
        
        //Transfer Only if the Operation is Complete
        if(!operation.getStatus().equals(OperationStatus.COMPLETED)) {
        	throw new OperationNotAllowedException("Operation Is not Complete Yet!");
        }

        // Duplicate active admission check
        wardAdmissionRepository
                .findByOperationIdAndDischargedWhenIsNull(request.getOperationId())
                .ifPresent(a -> {
                    throw new ValidationException("Operation already has an active ward admission");
                });

        // Room fetch + validate
        WardRoom room = wardRoomRepository.findById(request.getWardRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Ward room not found"));

        if (!room.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this room");
        }

        if (!room.getIsActive()) {
            throw new ValidationException("Ward room is inactive");
        }

        // Check if room has available beds
        if (room.getAvailableBeds() == null || room.getAvailableBeds() <= 0) {
            throw new ValidationException("No available beds in this room");
        }

        // Bed fetch + validate
        WardBed bed = wardBedRepository.findById(request.getWardBedId())
                .orElseThrow(() -> new ResourceNotFoundException("Ward bed not found"));

        if (!bed.getWardRoom().getId().equals(room.getId())) {
            throw new ValidationException("Bed does not belong to the selected room");
        }

        if (!bed.getIsActive()) {
            throw new ValidationException("Bed is inactive");
        }

        if (bed.getStatus() != BedStatus.AVAILABLE) {
            throw new ValidationException("Bed is not available. Current status: " + bed.getStatus());
        }

        // ✅ Bed OCCUPIED karo
        bed.setStatus(BedStatus.OCCUPIED);
        wardBedRepository.save(bed);

        // ✅ Room counts sync karo
        room.setOccupiedBeds((room.getOccupiedBeds() != null ? room.getOccupiedBeds() : 0) + 1);
        room.setAvailableBeds(room.getTotalBeds() - room.getOccupiedBeds());
        wardRoomRepository.save(room);

        // ✅ Ward counts sync karo
        Ward ward = room.getWard();
        ward.setOccupiedBeds((ward.getOccupiedBeds() != null ? ward.getOccupiedBeds() : 0) + 1);
        ward.setAvailableBeds(ward.getTotalBeds() - ward.getOccupiedBeds());
        // Note: Save ward if you have WardRepository, otherwise it will be auto-saved 
        // if you have proper cascade configuration

        // ✅ WardAdmission record create karo — history ke liye
        WardAdmission admission = WardAdmission.builder()
                .operation(operation)
                .wardRoom(room)
                .wardBed(bed)
                .hospital(currentUser.getHospital())
                .patientId(String.valueOf(operation.getPatientId())) // Convert if patientId is Long in operation
                .patientName(operation.getPatientName())
                .patientMrn(operation.getPatientMrn())
                .admissionTime(LocalDateTime.now())
                .admittedBy(currentUser.getUserName())
                .build();

        wardAdmissionRepository.save(admission);

        return mapToResponse(admission);
    }

    // -------------------- Discharge -------------------- //

    @Transactional
    @Override
    public WardAdmissionResponse discharge(Long operationId) {

        User currentUser = currentUser();

        // Active admission dhundo
        WardAdmission admission = wardAdmissionRepository
                .findByOperationIdAndDischargedWhenIsNull(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("No active admission found for this operation"));

        if (!admission.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to discharge this admission");
        }

        // ✅ Admission record close karo
        admission.setDischargedWhen(LocalDateTime.now());
        admission.setDischargedBy(currentUser.getUserName());
        wardAdmissionRepository.save(admission);

        // ✅ Bed wapas AVAILABLE karo — sirf status, kuch aur nahi
        WardBed bed = admission.getWardBed();
        bed.setStatus(BedStatus.AVAILABLE);
        wardBedRepository.save(bed);

        // ✅ Room counts sync karo
        WardRoom room = admission.getWardRoom();
        room.setOccupiedBeds(Math.max(0, (room.getOccupiedBeds() != null ? room.getOccupiedBeds() : 0) - 1));
        room.setAvailableBeds(room.getTotalBeds() - room.getOccupiedBeds());
        wardRoomRepository.save(room);

        // ✅ Ward counts sync karo
        var ward = room.getWard();
        ward.setOccupiedBeds(Math.max(0, (ward.getOccupiedBeds() != null ? ward.getOccupiedBeds() : 0) - 1));
        ward.setAvailableBeds(ward.getTotalBeds() - ward.getOccupiedBeds());

        return mapToResponse(admission);
    }

    // -------------------- Get -------------------- //

    @Override
    public WardAdmissionResponse getActiveByOperation(Long operationId) {

        User currentUser = currentUser();

        WardAdmission admission = wardAdmissionRepository
                .findByOperationIdAndDischargedWhenIsNull(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("No active admission found for this operation"));

        if (!admission.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this admission");
        }

        return mapToResponse(admission);
    }

    @Override
    public List<WardAdmissionResponse> getByPatient(String patientId) {

        User currentUser = currentUser();

        return wardAdmissionRepository
                .findByPatientIdOrderByAdmissionTimeDesc(patientId)
                .stream()
                .filter(a -> a.getHospital().getId().equals(currentUser.getHospital().getId()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<WardAdmissionResponse> getByRoom(Long wardRoomId) {

        User currentUser = currentUser();

        return wardAdmissionRepository
                .findByWardRoomIdOrderByAdmissionTimeDesc(wardRoomId)
                .stream()
                .filter(a -> a.getHospital().getId().equals(currentUser.getHospital().getId()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<WardAdmissionResponse> getByBed(Long wardBedId) {

        User currentUser = currentUser();

        return wardAdmissionRepository
                .findByWardBedIdOrderByAdmissionTimeDesc(wardBedId)
                .stream()
                .filter(a -> a.getHospital().getId().equals(currentUser.getHospital().getId()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // -------------------- Mapper -------------------- //

    private WardAdmissionResponse mapToResponse(WardAdmission a) {
        return WardAdmissionResponse.builder()
                .id(a.getId())
                .operationId(a.getOperation().getId())
                .wardRoomId(a.getWardRoom().getId())
                .roomNumber(a.getWardRoom().getRoomNumber())
                .roomName(a.getWardRoom().getRoomName())
                .wardBedId(a.getWardBed().getId())
                .bedNumber(a.getWardBed().getBedNumber())
                .patientId(a.getPatientId())
                .patientName(a.getPatientName())
                .patientMrn(a.getPatientMrn())
                .admissionTime(a.getAdmissionTime())
                .admittedBy(a.getAdmittedBy())
                .dischargedWhen(a.getDischargedWhen())
                .dischargedBy(a.getDischargedBy())
                .createdAt(a.getCreatedAt())
                .build();
    }
}