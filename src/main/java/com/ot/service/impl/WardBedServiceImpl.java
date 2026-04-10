package com.ot.service.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ot.dto.ward.WardBedRequest;
import com.ot.dto.ward.WardBedResponse;
import com.ot.dto.ward.WardBedUpdateRequest;
import com.ot.entity.User;
import com.ot.entity.WardBed;
import com.ot.entity.WardRoom;
import com.ot.enums.BedStatus;
import com.ot.enums.RoleType;
import com.ot.exception.ResourceNotFoundException;
import com.ot.exception.UnauthorizedException;
import com.ot.exception.ValidationException;
import com.ot.repository.WardBedRepository;
import com.ot.repository.WardRoomRepository;
import com.ot.security.CustomUserDetails;
import com.ot.service.WardBedService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WardBedServiceImpl implements WardBedService {

    private final WardBedRepository wardBedRepository;
    private final WardRoomRepository wardRoomRepository;

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No user is logged in");
        }
        return ((CustomUserDetails) auth.getPrincipal()).getUser();
    }

    // ---------------------------------------- Create ---------------------------------------- //

    @Transactional
    @Override
    public WardBedResponse createBed(WardBedRequest request) {

        User currentUser = currentUser();

        if (!Set.of(RoleType.ADMIN, RoleType.HOSPITAL_ADMIN).contains(currentUser.getRole())) {
            throw new ValidationException("Only admin can create ward beds");
        }

        WardRoom room = wardRoomRepository.findById(request.getWardRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Ward room not found"));

        if (!room.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this room");
        }

        if (!room.getIsActive()) {
            throw new ValidationException("Cannot add bed to inactive room");
        }

        if (wardBedRepository.existsByWardRoomAndBedNumber(room, request.getBedNumber())) {
            throw new ValidationException("Bed number already exists in this room: " + request.getBedNumber());
        }

        WardBed bed = WardBed.builder()
                .wardRoom(room)
                .hospital(currentUser.getHospital())
                .bedNumber(request.getBedNumber())
                .createdBy(currentUser.getUserName())
                .build();

        wardBedRepository.save(bed);

        // Room + Ward totalBeds aur availableBeds update karo
        room.setTotalBeds((room.getTotalBeds() != null ? room.getTotalBeds() : 0) + 1);
        room.setAvailableBeds((room.getAvailableBeds() != null ? room.getAvailableBeds() : 0) + 1);
        wardRoomRepository.save(room);

        var ward = room.getWard();
        ward.setTotalBeds((ward.getTotalBeds() != null ? ward.getTotalBeds() : 0) + 1);
        ward.setAvailableBeds((ward.getAvailableBeds() != null ? ward.getAvailableBeds() : 0) + 1);

        return mapToResponse(bed);
    }

    // ----------------------------------------- Get ------------------------------------------ //

    @Override
    public WardBedResponse getBedById(Long bedId) {

        User currentUser = currentUser();

        WardBed bed = wardBedRepository.findById(bedId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward bed not found"));

        if (!bed.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this bed");
        }

        return mapToResponse(bed);
    }

    @Override
    public List<WardBedResponse> getBedsByRoom(Long roomId, Boolean isActive, BedStatus status) {

        User currentUser = currentUser();

        WardRoom room = wardRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward room not found"));

        if (!room.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this room");
        }

        if (status != null) {
            return wardBedRepository.findByWardRoomAndStatus(room, status)
                    .stream().map(this::mapToResponse).collect(Collectors.toList());
        }

        if (isActive != null) {
            return wardBedRepository.findByWardRoomAndIsActive(room, isActive)
                    .stream().map(this::mapToResponse).collect(Collectors.toList());
        }

        return wardBedRepository.findByWardRoom(room)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ---------------------------------------- Update ---------------------------------------- //

    @Transactional
    @Override
    public WardBedResponse updateBed(Long bedId, WardBedUpdateRequest request) {

        User currentUser = currentUser();

        if (!Set.of(RoleType.ADMIN, RoleType.HOSPITAL_ADMIN).contains(currentUser.getRole())) {
            throw new ValidationException("Only admin can update ward beds");
        }

        WardBed bed = wardBedRepository.findById(bedId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward bed not found"));

        if (!bed.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to update this bed");
        }

        if (request.getBedNumber() != null
                && !request.getBedNumber().equals(bed.getBedNumber())
                && wardBedRepository.existsByWardRoomAndBedNumber(bed.getWardRoom(), request.getBedNumber())) {
            throw new ValidationException("Bed number already exists: " + request.getBedNumber());
        }

        if (request.getBedNumber() != null) bed.setBedNumber(request.getBedNumber());
        if (request.getIsActive() != null)  bed.setIsActive(request.getIsActive());

        wardBedRepository.save(bed);

        return mapToResponse(bed);
    }

    // ---------------------------------------- Status ---------------------------------------- //

    @Transactional
    @Override
    public WardBedResponse markMaintenance(Long bedId) {

        User currentUser = currentUser();

        if (!Set.of(RoleType.ADMIN, RoleType.HOSPITAL_ADMIN).contains(currentUser.getRole())) {
            throw new ValidationException("Only admin can change bed status");
        }

        WardBed bed = wardBedRepository.findById(bedId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward bed not found"));

        if (!bed.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to update this bed");
        }

        if (bed.getStatus() == BedStatus.OCCUPIED) {
            throw new ValidationException("Cannot mark occupied bed as maintenance. Discharge patient first.");
        }

        if (bed.getStatus() == BedStatus.MAINTENANCE) {
            throw new ValidationException("Bed is already in maintenance");
        }

        bed.setStatus(BedStatus.MAINTENANCE);
        wardBedRepository.save(bed);

        return mapToResponse(bed);
    }

    @Transactional
    @Override
    public WardBedResponse markAvailable(Long bedId) {

        User currentUser = currentUser();

        if (!Set.of(RoleType.ADMIN, RoleType.HOSPITAL_ADMIN).contains(currentUser.getRole())) {
            throw new ValidationException("Only admin can change bed status");
        }

        WardBed bed = wardBedRepository.findById(bedId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward bed not found"));

        if (!bed.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to update this bed");
        }

        if (bed.getStatus() == BedStatus.OCCUPIED) {
            throw new ValidationException("Cannot mark occupied bed as available. Discharge patient first.");
        }

        if (bed.getStatus() == BedStatus.AVAILABLE) {
            throw new ValidationException("Bed is already available");
        }

        bed.setStatus(BedStatus.AVAILABLE);
        wardBedRepository.save(bed);

        return mapToResponse(bed);
    }

    // ---------------------------------------- Activate / Deactivate ---------------------------------------- //

    @Transactional
    @Override
    public void deactivateBed(Long bedId) {

        User currentUser = currentUser();

        if (!Set.of(RoleType.ADMIN, RoleType.HOSPITAL_ADMIN).contains(currentUser.getRole())) {
            throw new ValidationException("Only admin can deactivate ward beds");
        }

        WardBed bed = wardBedRepository.findById(bedId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward bed not found"));

        if (!bed.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to deactivate this bed");
        }

        if (!bed.getIsActive()) {
            throw new ValidationException("Bed is already inactive");
        }

        if (bed.getStatus() == BedStatus.OCCUPIED) {
            throw new ValidationException("Cannot deactivate occupied bed. Discharge patient first.");
        }

        bed.setIsActive(false);
        wardBedRepository.save(bed);
    }

    @Transactional
    @Override
    public void activateBed(Long bedId) {

        User currentUser = currentUser();

        if (!Set.of(RoleType.ADMIN, RoleType.HOSPITAL_ADMIN).contains(currentUser.getRole())) {
            throw new ValidationException("Only admin can activate ward beds");
        }

        WardBed bed = wardBedRepository.findById(bedId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward bed not found"));

        if (!bed.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to activate this bed");
        }

        if (bed.getIsActive()) {
            throw new ValidationException("Bed is already active");
        }

        bed.setIsActive(true);
        wardBedRepository.save(bed);
    }

    // ---------------------------------------- Mapper ---------------------------------------- //

    private WardBedResponse mapToResponse(WardBed bed) {
        return WardBedResponse.builder()
                .id(bed.getId())
                .wardRoomId(bed.getWardRoom().getId())
                .roomNumber(bed.getWardRoom().getRoomNumber())
                .roomName(bed.getWardRoom().getRoomName())
                .wardId(bed.getWardRoom().getWard().getId())
                .wardNumber(bed.getWardRoom().getWard().getWardNumber())
                .wardName(bed.getWardRoom().getWard().getWardName())
                .bedNumber(bed.getBedNumber())
                .status(bed.getStatus())
                .isActive(bed.getIsActive())
                .createdBy(bed.getCreatedBy())
                .createdAt(bed.getCreatedAt())
                .updatedAt(bed.getUpdatedAt())
                .build();
    }
}