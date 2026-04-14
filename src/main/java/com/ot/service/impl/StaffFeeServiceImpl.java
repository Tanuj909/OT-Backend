package com.ot.service.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.ot.dto.staffRequest.StaffFeeRequest;
import com.ot.dto.staffRequest.StaffFeeResponse;
import com.ot.dto.staffRequest.StaffFeeUpdateRequest;
import com.ot.entity.Hospital;
import com.ot.entity.StaffFee;
import com.ot.entity.User;
import com.ot.enums.RoleType;
import com.ot.exception.ResourceNotFoundException;
import com.ot.exception.UnauthorizedException;
import com.ot.exception.ValidationException;
import com.ot.repository.StaffFeeRepository;
import com.ot.repository.UserRepository;
import com.ot.security.CustomUserDetails;
import com.ot.service.StaffFeeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StaffFeeServiceImpl implements StaffFeeService {

    private final StaffFeeRepository staffFeeRepository;
    private final UserRepository userRepository;

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
    public StaffFeeResponse createStaffFee(StaffFeeRequest request) {

        User currentUser = currentUser();

        if (!Set.of(RoleType.ADMIN, RoleType.HOSPITAL_ADMIN).contains(currentUser.getRole())) {
            throw new ValidationException("Only admin can create staff fees");
        }

        Hospital hospital = currentUser.getHospital();

        // Staff fetch
        User staff = userRepository.findById(request.getStaffId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));

        // Same hospital check
        if (!staff.getHospital().getId().equals(hospital.getId())) {
            throw new UnauthorizedException("Staff does not belong to your hospital");
        }

        // Duplicate check — ek staff ka ek hi fee record
        if (staffFeeRepository.existsByStaffAndHospital(staff, hospital)) {
            throw new ValidationException("Fee record already exists for this staff. Use update instead.");
        }

        StaffFee staffFee = StaffFee.builder()
                .staff(staff)
                .hospital(hospital)
                .consultationFee(request.getConsultationFee())
                .otFee(request.getOtFee())
                .visitFee(request.getVisitFee())
                .emergencyFee(request.getEmergencyFee())
                .createdBy(currentUser.getUserName())
                .build();

        staffFeeRepository.save(staffFee);

        return mapToResponse(staffFee);
    }

    // ----------------------------------------- Get ------------------------------------------ //

    @Override
    public StaffFeeResponse getByStaffId(Long staffId) {

        User currentUser = currentUser();

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));

        StaffFee staffFee = staffFeeRepository.findByStaffAndHospital(staff, currentUser.getHospital())
                .orElseThrow(() -> new ResourceNotFoundException("Fee record not found for this staff"));

        return mapToResponse(staffFee);
    }

    @Override
    public List<StaffFeeResponse> getAllStaffFees(Boolean isActive) {

        User currentUser = currentUser();
        Hospital hospital = currentUser.getHospital();

        if (isActive != null) {
            return staffFeeRepository.findByHospitalAndIsActive(hospital, isActive)
                    .stream().map(this::mapToResponse).collect(Collectors.toList());
        }

        return staffFeeRepository.findByHospital(hospital)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ---------------------------------------- Update ---------------------------------------- //

    @Transactional
    @Override
    public StaffFeeResponse updateStaffFee(Long staffId, StaffFeeUpdateRequest request) {

        User currentUser = currentUser();

        if (!Set.of(RoleType.ADMIN, RoleType.HOSPITAL_ADMIN).contains(currentUser.getRole())) {
            throw new ValidationException("Only admin can update staff fees");
        }

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));

        StaffFee staffFee = staffFeeRepository.findByStaffAndHospital(staff, currentUser.getHospital())
                .orElseThrow(() -> new ResourceNotFoundException("Fee record not found for this staff"));

        if (request.getConsultationFee() != null) staffFee.setConsultationFee(request.getConsultationFee());
        if (request.getOtFee() != null)           staffFee.setOtFee(request.getOtFee());
        if (request.getVisitFee() != null)        staffFee.setVisitFee(request.getVisitFee());
        if (request.getEmergencyFee() != null)    staffFee.setEmergencyFee(request.getEmergencyFee());
        if (request.getIsActive() != null)         staffFee.setIsActive(request.getIsActive());

        staffFeeRepository.save(staffFee);

        return mapToResponse(staffFee);
    }

    // ---------------------------------------- Mapper ---------------------------------------- //

    private StaffFeeResponse mapToResponse(StaffFee sf) {
        return StaffFeeResponse.builder()
                .id(sf.getId())
                .staffId(sf.getStaff().getId())
                .staffName(sf.getStaff().getUserName())
                .staffUserName(sf.getStaff().getUserName())
                .consultationFee(sf.getConsultationFee())
                .otFee(sf.getOtFee())
                .visitFee(sf.getVisitFee())
                .emergencyFee(sf.getEmergencyFee())
                .isActive(sf.getIsActive())
                .createdBy(sf.getCreatedBy())
                .createdAt(sf.getCreatedAt())
                .updatedAt(sf.getUpdatedAt())
                .build();
    }
}