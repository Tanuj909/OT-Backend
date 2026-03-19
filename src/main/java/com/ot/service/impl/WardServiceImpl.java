package com.ot.service.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ot.dto.ward.WardRequest;
import com.ot.dto.ward.WardResponse;
import com.ot.dto.ward.WardUpdateRequest;
import com.ot.entity.Hospital;
import com.ot.entity.User;
import com.ot.entity.Ward;
import com.ot.enums.RoleType;
import com.ot.enums.WardType;
import com.ot.exception.ResourceNotFoundException;
import com.ot.exception.UnauthorizedException;
import com.ot.exception.ValidationException;
import com.ot.repository.WardRepository;
import com.ot.security.CustomUserDetails;
import com.ot.service.WardService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WardServiceImpl implements WardService {

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
    public WardResponse createWard(WardRequest request) {

        User currentUser = currentUser();

        // Role check
        if (!Set.of(RoleType.ADMIN, RoleType.HOSPITAL_ADMIN).contains(currentUser.getRole())) {
            throw new ValidationException("Only admin can create wards");
        }

        // Duplicate wardNumber check
        if (wardRepository.existsByHospitalAndWardNumber(currentUser.getHospital(), request.getWardNumber())) {
            throw new ValidationException("Ward number already exists: " + request.getWardNumber());
        }

        Ward ward = Ward.builder()
                .hospital(currentUser.getHospital())
                .wardNumber(request.getWardNumber())
                .wardName(request.getWardName())
                .wardType(request.getWardType())
                .totalBeds(request.getTotalBeds())
                .createdBy(currentUser.getUserName())
                .build();

        wardRepository.save(ward);

        return mapToResponse(ward);
    }

    // ----------------------------------------- Get ------------------------------------------ //

    @Override
    public WardResponse getWardById(Long wardId) {

        User currentUser = currentUser();

        Ward ward = wardRepository.findById(wardId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found"));

        if (!ward.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this ward");
        }

        return mapToResponse(ward);
    }

    @Override
    public List<WardResponse> getAllWards(WardType wardType, Boolean isActive) {

        User currentUser = currentUser();
        Hospital hospital = currentUser.getHospital();

        if (wardType != null) {
            return wardRepository.findByHospitalAndWardType(hospital, wardType)
                    .stream().map(this::mapToResponse).collect(Collectors.toList());
        } else if (isActive != null) {
            return wardRepository.findByHospitalAndIsActive(hospital, isActive)
                    .stream().map(this::mapToResponse).collect(Collectors.toList());
        }

        return wardRepository.findByHospital(hospital)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ---------------------------------------- Update ---------------------------------------- //

    @Transactional
    @Override
    public WardResponse updateWard(Long wardId, WardUpdateRequest request) {

        User currentUser = currentUser();

        if (!Set.of(RoleType.ADMIN, RoleType.HOSPITAL_ADMIN).contains(currentUser.getRole())) {
            throw new ValidationException("Only admin can update wards");
        }

        Ward ward = wardRepository.findById(wardId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found"));

        if (!ward.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to update this ward");
        }

        // Duplicate wardNumber check — sirf agar wardNumber change ho raha ho
        if (request.getWardNumber() != null
                && !request.getWardNumber().equals(ward.getWardNumber())
                && wardRepository.existsByHospitalAndWardNumber(currentUser.getHospital(), request.getWardNumber())) {
            throw new ValidationException("Ward number already exists: " + request.getWardNumber());
        }

        // Partial update
        if (request.getWardNumber() != null)  ward.setWardNumber(request.getWardNumber());
        if (request.getWardName() != null)    ward.setWardName(request.getWardName());
        if (request.getWardType() != null)    ward.setWardType(request.getWardType());
        if (request.getIsActive() != null)    ward.setIsActive(request.getIsActive());
        if (request.getTotalBeds() != null) {
            // totalBeds kam nahi ho sakta occupied beds se
            if (request.getTotalBeds() < ward.getOccupiedBeds()) {
                throw new ValidationException("Total beds cannot be less than occupied beds: "
                        + ward.getOccupiedBeds());
            }
            ward.setTotalBeds(request.getTotalBeds());
        }

        wardRepository.save(ward);

        return mapToResponse(ward);
    }

    // ---------------------------------------- Delete ---------------------------------------- //

    @Transactional
    @Override
    public void deactivateWard(Long wardId) {

        User currentUser = currentUser();

        if (!Set.of(RoleType.ADMIN, RoleType.HOSPITAL_ADMIN).contains(currentUser.getRole())) {
            throw new ValidationException("Only admin can deactivate wards");
        }

        Ward ward = wardRepository.findById(wardId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found"));

        if (!ward.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to deactivate this ward");
        }

        if (!ward.getIsActive()) {
            throw new ValidationException("Ward is already inactive");
        }

        // Occupied beds check — patients hain toh deactivate nahi kar sakte
        if (ward.getOccupiedBeds() > 0) {
            throw new ValidationException("Cannot deactivate ward with " + ward.getOccupiedBeds() + " occupied beds");
        }

        ward.setIsActive(false);
        wardRepository.save(ward);
    }

    @Transactional
    @Override
    public void activateWard(Long wardId) {

        User currentUser = currentUser();

        if (!Set.of(RoleType.ADMIN, RoleType.HOSPITAL_ADMIN).contains(currentUser.getRole())) {
            throw new ValidationException("Only admin can activate wards");
        }

        Ward ward = wardRepository.findById(wardId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found"));

        if (ward.getIsActive()) {
            throw new ValidationException("Ward is already active");
        }

        ward.setIsActive(true);
        wardRepository.save(ward);
    }

    // ---------------------------------------- Mapper ---------------------------------------- //

    private WardResponse mapToResponse(Ward ward) {
        return WardResponse.builder()
                .id(ward.getId())
                .wardNumber(ward.getWardNumber())
                .wardName(ward.getWardName())
                .wardType(ward.getWardType())
                .totalBeds(ward.getTotalBeds())
                .occupiedBeds(ward.getOccupiedBeds())
                .availableBeds(ward.getAvailableBeds())
                .isActive(ward.getIsActive())
                .createdBy(ward.getCreatedBy())
                .createdAt(ward.getCreatedAt())
                .updatedAt(ward.getUpdatedAt())
                .build();
    }
}