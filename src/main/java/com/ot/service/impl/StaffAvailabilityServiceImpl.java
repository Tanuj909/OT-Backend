package com.ot.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.ot.dto.request.CreateStaffAvailabilityRequest;
import com.ot.dto.response.StaffAvailabilityResponse;
import com.ot.dto.staffRequest.StaffRosterResponse;
import com.ot.entity.ScheduledOperation;
import com.ot.entity.StaffAvailability;
import com.ot.entity.StaffSchedule;
import com.ot.entity.User;
import com.ot.enums.RoleType;
import com.ot.enums.StaffAvailabilityStatus;
import com.ot.exception.ForbiddenException;
import com.ot.exception.ResourceNotFoundException;
import com.ot.exception.ValidationException;
import com.ot.repository.ScheduledOperationRepository;
import com.ot.repository.StaffAvailabilityRepository;
import com.ot.repository.StaffScheduleRepository;
import com.ot.repository.UserRepository;
import com.ot.security.CustomUserDetails;
import com.ot.service.StaffAvailabilityService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StaffAvailabilityServiceImpl implements StaffAvailabilityService{
	
    private final UserRepository userRepository;
    private final StaffAvailabilityRepository staffAvailabilityRepository;
    private final StaffScheduleRepository staffScheduleRepository;
    private final ScheduledOperationRepository operationRepository;
	
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
	public StaffAvailabilityResponse createStaffAvailability(CreateStaffAvailabilityRequest request) {

	    User admin = currentUser();

	    if(admin.getRole() != RoleType.ADMIN){
	        throw new ForbiddenException("Only hospital admin can manage staff availability");
	    }

	    User staff = userRepository.findById(request.getStaffId())
	            .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
	    
	    if(!staff.getHospital().getId().equals(admin.getHospital().getId())){
	        throw new ForbiddenException("Staff does not belong to your hospital");
	    }
	    
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

	    StaffAvailability availability = StaffAvailability.builder()
	            .staff(staff)
	            .date(request.getDate())
	            .startTime(request.getStartTime())
	            .endTime(request.getEndTime())
	            .status(request.getStatus())
	            .build();

	    StaffAvailability saved = staffAvailabilityRepository.save(availability);

	    return StaffAvailabilityResponse.builder()
	            .id(saved.getId())
	            .staffId(saved.getStaff().getId())
	            .date(saved.getDate())
	            .startTime(saved.getStartTime())
	            .endTime(saved.getEndTime())
	            .status(saved.getStatus())
	            .build();

	}
	
	
	@Override
	public List<StaffAvailabilityResponse> getStaffAvailabilityByStaff(Long staffId) {

	    User admin = currentUser();

	    if(admin.getRole() != RoleType.ADMIN){
	        throw new ForbiddenException("Only hospital admin can view staff availability");
	    }

	    User staff = userRepository.findById(staffId)
	            .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));

	    if(!staff.getHospital().getId().equals(admin.getHospital().getId())){
	        throw new ForbiddenException("Staff does not belong to your hospital");
	    }

	    List<StaffAvailability> availabilityList =
	            staffAvailabilityRepository.findByStaffId(staffId);

	    return availabilityList.stream()
	            .map(a -> StaffAvailabilityResponse.builder()
	                    .id(a.getId())
	                    .staffId(a.getStaff().getId())
	                    .date(a.getDate())
	                    .startTime(a.getStartTime())
	                    .endTime(a.getEndTime())
	                    .status(a.getStatus())
	                    .build())
	            .toList();
	}
	
	
    @Override
    public boolean isStaffAvailable(Long staffId, LocalDate date, LocalTime start, LocalTime end) {
        // Step 1: date specific availability check
        List<StaffAvailability> availabilityList = staffAvailabilityRepository.findByStaffIdAndDate(staffId, date);
        if (!availabilityList.isEmpty()) {
            for (StaffAvailability availability : availabilityList) {
                // Leave or unavailable
                if (availability.getStatus() == StaffAvailabilityStatus.UNAVAILABLE) {
                    return false;
                }
                // Check time slot inside availability
                boolean withinSlot = !start.isBefore(availability.getStartTime()) && !end.isAfter(availability.getEndTime());
                if (withinSlot) {
                    return true;
                }
            }
            return false;
        }

        // Step 2: fallback to weekly schedule
        DayOfWeek day = date.getDayOfWeek();
        StaffSchedule schedule = staffScheduleRepository.findByStaffIdAndDayOfWeek(staffId, day).orElse(null);
        if (schedule == null) {
            return false;
        }
        return !start.isBefore(schedule.getStartTime()) && !end.isAfter(schedule.getEndTime());
    }
    
    
    @Override
    public StaffRosterResponse getStaffAvailability(
            LocalDateTime startTime, LocalDateTime endTime) {

        User currentUser = currentUser();
        Long hospitalId = currentUser.getHospital().getId();

        // Validation
        if (startTime.isAfter(endTime)) {
            throw new ValidationException("Start time cannot be after end time");
        }

        // 1. Busy IDs fetch karo
        Set<Long> busySurgeonIds = operationRepository
                .findBusySurgeonIds(hospitalId, startTime, endTime);

        Set<Long> busyStaffIds = operationRepository
                .findBusyStaffIds(hospitalId, startTime, endTime);

        // 2. Sab users fetch karo hospital ke
        List<User> allUsers = userRepository.findByHospitalAndIsActive(
                currentUser.getHospital(), true);

        // 3. Map karo role wise
        Map<RoleType, List<StaffRosterResponse.StaffMemberAvailability>> roleMap =
                new HashMap<>();

        allUsers.forEach(user -> {
            boolean isBusy = isBusy(user, busySurgeonIds, busyStaffIds);

            Long assignedOpId = null;
            String assignedOpRef = null;

            if (isBusy) {
                Optional<ScheduledOperation> assignedOp = findAssignedOperation(
                        user, startTime, endTime, hospitalId);
                if (assignedOp.isPresent()) {
                    assignedOpId = assignedOp.get().getId();
                    assignedOpRef = assignedOp.get().getOperationReference();
                }
            }

            StaffRosterResponse.StaffMemberAvailability availability =
                    StaffRosterResponse.StaffMemberAvailability.builder()
                            .id(user.getId())
                            .userName(user.getUserName())
                            .email(user.getEmail())
                            .role(user.getRole())
                            .isAvailable(!isBusy)
                            .assignedOperationId(assignedOpId)
                            .assignedOperationRef(assignedOpRef)
                            .build();

            roleMap.computeIfAbsent(user.getRole(), k -> new ArrayList<>()).add(availability);
        });

        // 4. Total counts
        long totalBusy = allUsers.stream()
                .filter(u -> isBusy(u, busySurgeonIds, busyStaffIds))
                .count();

        return StaffRosterResponse.builder()
                .startTime(startTime)
                .endTime(endTime)
                .surgeons(roleMap.getOrDefault(RoleType.SURGEON, List.of()))
                .anesthesiologists(roleMap.getOrDefault(RoleType.ANESTHESIOLOGIST, List.of()))
                .scrubNurses(roleMap.getOrDefault(RoleType.SCRUB_NURSE, List.of()))
                .circulatingNurses(roleMap.getOrDefault(RoleType.CIRCULATING_NURSE, List.of()))
                .anesthesiaNurses(roleMap.getOrDefault(RoleType.ANESTHESIA_NURSE, List.of()))
                .otTechnicians(roleMap.getOrDefault(RoleType.OT_TECHNICIAN, List.of()))
                .surgicalTechs(roleMap.getOrDefault(RoleType.SURGICAL_TECH, List.of()))
                .anesthesiaTechnicians(roleMap.getOrDefault(RoleType.ANESTHESIA_TECHNICIAN, List.of()))
                .orderlies(roleMap.getOrDefault(RoleType.ORDERLY, List.of()))
                .otAssistants(roleMap.getOrDefault(RoleType.OT_ASSISTANT, List.of()))
                .totalAvailable((int) (allUsers.size() - totalBusy))
                .totalBusy((int) totalBusy)
                .build();
    }

    // Helper — user busy hai?
    private boolean isBusy(User user, Set<Long> busySurgeonIds, Set<Long> busyStaffIds) {
        return busySurgeonIds.contains(user.getId()) ||
               busyStaffIds.contains(user.getId());
    }

    // Helper — assigned operation find karo
    private Optional<ScheduledOperation> findAssignedOperation(
            User user, LocalDateTime startTime, LocalDateTime endTime, Long hospitalId) {
        return operationRepository
                .findAssignedOperationForUser(user.getId(), hospitalId, startTime, endTime);
    }
    
	@Override
	@Transactional
	public void deleteStaffAvailability(Long availabilityId) {

	    User admin = currentUser();

	    if(admin.getRole() != RoleType.ADMIN){
	        throw new ForbiddenException("Only hospital admin can delete staff availability");
	    }

	    StaffAvailability availability = staffAvailabilityRepository.findById(availabilityId)
	            .orElseThrow(() -> new ResourceNotFoundException("Availability not found"));

	    if(!availability.getStaff().getHospital().getId()
	            .equals(admin.getHospital().getId())){
	        throw new ForbiddenException("You cannot delete this availability");
	    }

	    staffAvailabilityRepository.delete(availability);
	}

}
