package com.ot.service.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ot.dto.ward.WardRoomRequest;
import com.ot.dto.ward.WardRoomResponse;
import com.ot.dto.ward.WardRoomUpdateRequest;
import com.ot.entity.User;
import com.ot.entity.Ward;
import com.ot.entity.WardRoom;
import com.ot.enums.RoleType;
import com.ot.exception.ResourceNotFoundException;
import com.ot.exception.UnauthorizedException;
import com.ot.exception.ValidationException;
import com.ot.repository.WardRepository;
import com.ot.repository.WardRoomRepository;
import com.ot.security.CustomUserDetails;
import com.ot.service.WardRoomService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WardRoomServiceImpl implements WardRoomService {

    private final WardRoomRepository wardRoomRepository;
    private final WardRepository wardRepository;

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
    public WardRoomResponse createWardRoom(WardRoomRequest request) {

        User currentUser = currentUser();

        // Role check
        if (!Set.of(RoleType.ADMIN, RoleType.HOSPITAL_ADMIN).contains(currentUser.getRole())) {
            throw new ValidationException("Only admin can create ward rooms");
        }

        // Ward fetch
        Ward ward = wardRepository.findById(request.getWardId())
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found"));

        // Same hospital check
        if (!ward.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this ward");
        }

        // Ward active check
        if (!ward.getIsActive()) {
            throw new ValidationException("Cannot add room to inactive ward");
        }

        // Duplicate roomNumber check
        if (wardRoomRepository.existsByWardAndRoomNumber(ward, request.getRoomNumber())) {
            throw new ValidationException("Room number already exists in this ward: "
                    + request.getRoomNumber());
        }

        WardRoom room = WardRoom.builder()
                .ward(ward)
                .hospital(currentUser.getHospital())
                .roomNumber(request.getRoomNumber())
                .roomName(request.getRoomName())
                .roomType(request.getRoomType())
                .totalBeds(request.getTotalBeds())
                .occupiedBeds(0)                            // 👈 explicit
                .availableBeds(request.getTotalBeds())      // 👈 explicit
                .ratePerHour(request.getRatePerHour())
                .discountPercent(request.getDiscountPercent())
                .gstPercent(request.getGstPercent())
                .hsnCode(request.getHsnCode())
                .createdBy(currentUser.getUserName())
                .build();

        wardRoomRepository.save(room);

        // Ward ka totalBeds update karo
        ward.setTotalBeds((ward.getTotalBeds() != null
                ? ward.getTotalBeds() : 0) + request.getTotalBeds());
        wardRepository.save(ward);

        return mapToResponse(room);
    }

    // ----------------------------------------- Get ------------------------------------------ //

    @Override
    public WardRoomResponse getWardRoomById(Long roomId) {

        User currentUser = currentUser();

        WardRoom room = wardRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward room not found"));

        if (!room.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this room");
        }

        return mapToResponse(room);
    }

    @Override
    public List<WardRoomResponse> getWardRoomsByWardId(Long wardId, Boolean isActive) {

        User currentUser = currentUser();

        Ward ward = wardRepository.findById(wardId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found"));

        if (!ward.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this ward");
        }

        if (isActive != null) {
            return wardRoomRepository.findByWardAndIsActive(ward, isActive)
                    .stream().map(this::mapToResponse).collect(Collectors.toList());
        }

        return wardRoomRepository.findByWard(ward)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<WardRoomResponse> getAvailableRooms(Long wardId) {

        User currentUser = currentUser();

        Ward ward = wardRepository.findById(wardId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found"));

        if (!ward.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this ward");
        }

        return wardRoomRepository.findByWardAndIsActive(ward, true)
                .stream()
                .filter(r -> r.getAvailableBeds() != null && r.getAvailableBeds() > 0)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ---------------------------------------- Update ---------------------------------------- //

    @Transactional
    @Override
    public WardRoomResponse updateWardRoom(Long roomId, WardRoomUpdateRequest request) {

        User currentUser = currentUser();

        if (!Set.of(RoleType.ADMIN, RoleType.HOSPITAL_ADMIN).contains(currentUser.getRole())) {
            throw new ValidationException("Only admin can update ward rooms");
        }

        WardRoom room = wardRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward room not found"));

        if (!room.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to update this room");
        }

        // Duplicate roomNumber check
        if (request.getRoomNumber() != null
                && !request.getRoomNumber().equals(room.getRoomNumber())
                && wardRoomRepository.existsByWardAndRoomNumber(room.getWard(), request.getRoomNumber())) {
            throw new ValidationException("Room number already exists: " + request.getRoomNumber());
        }

        // TotalBeds kam nahi ho sakta occupied beds se
        if (request.getTotalBeds() != null) {
            if (room.getOccupiedBeds() != null && request.getTotalBeds() < room.getOccupiedBeds()) {
                throw new ValidationException("Total beds cannot be less than occupied beds: "
                        + room.getOccupiedBeds());
            }

            // Ward ka totalBeds bhi update karo
            Ward ward = room.getWard();
            int diff = request.getTotalBeds() - (room.getTotalBeds() != null ? room.getTotalBeds() : 0);
            ward.setTotalBeds((ward.getTotalBeds() != null ? ward.getTotalBeds() : 0) + diff);
            wardRepository.save(ward);

            room.setTotalBeds(request.getTotalBeds());
        }

        if (request.getRoomNumber() != null)    room.setRoomNumber(request.getRoomNumber());
        if (request.getRoomName() != null)      room.setRoomName(request.getRoomName());
        if (request.getRoomType() != null)      room.setRoomType(request.getRoomType());
        if (request.getRatePerHour() != null)   room.setRatePerHour(request.getRatePerHour());
        if (request.getDiscountPercent() != null) room.setDiscountPercent(request.getDiscountPercent());
        if (request.getGstPercent() != null)    room.setGstPercent(request.getGstPercent());
        if (request.getHsnCode() != null)       room.setHsnCode(request.getHsnCode());
        if (request.getIsActive() != null)      room.setIsActive(request.getIsActive());

        wardRoomRepository.save(room);

        return mapToResponse(room);
    }

    // ---------------------------------------- Deactivate/Activate ---------------------------------------- //

    @Transactional
    @Override
    public void deactivateWardRoom(Long roomId) {

        User currentUser = currentUser();

        if (!Set.of(RoleType.ADMIN, RoleType.HOSPITAL_ADMIN).contains(currentUser.getRole())) {
            throw new ValidationException("Only admin can deactivate ward rooms");
        }

        WardRoom room = wardRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward room not found"));

        if (!room.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to deactivate this room");
        }

        if (!room.getIsActive()) {
            throw new ValidationException("Room is already inactive");
        }

        if (room.getOccupiedBeds() != null && room.getOccupiedBeds() > 0) {
            throw new ValidationException("Cannot deactivate room with "
                    + room.getOccupiedBeds() + " occupied beds");
        }

        room.setIsActive(false);
        wardRoomRepository.save(room);
    }

    @Transactional
    @Override
    public void activateWardRoom(Long roomId) {

        User currentUser = currentUser();

        if (!Set.of(RoleType.ADMIN, RoleType.HOSPITAL_ADMIN).contains(currentUser.getRole())) {
            throw new ValidationException("Only admin can activate ward rooms");
        }

        WardRoom room = wardRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward room not found"));

        if (room.getIsActive()) {
            throw new ValidationException("Room is already active");
        }

        room.setIsActive(true);
        wardRoomRepository.save(room);
    }

    // ---------------------------------------- Mapper ---------------------------------------- //

    private WardRoomResponse mapToResponse(WardRoom room) {
        return WardRoomResponse.builder()
                .id(room.getId())
                .wardId(room.getWard().getId())
                .wardNumber(room.getWard().getWardNumber())
                .wardName(room.getWard().getWardName())
                .roomNumber(room.getRoomNumber())
                .roomName(room.getRoomName())
                .roomType(room.getRoomType())
                .totalBeds(room.getTotalBeds())
                .occupiedBeds(room.getOccupiedBeds())
                .availableBeds(room.getAvailableBeds())
                .ratePerHour(room.getRatePerHour())
                .discountPercent(room.getDiscountPercent())
                .gstPercent(room.getGstPercent())
                .hsnCode(room.getHsnCode())
                .isActive(room.getIsActive())
                .createdBy(room.getCreatedBy())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }
}
