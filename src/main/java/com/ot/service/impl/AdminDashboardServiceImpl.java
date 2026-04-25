package com.ot.service.impl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ot.dto.dashboards.AdminDashboardResponse;
import com.ot.embed.StaffAssignment;
import com.ot.embed.SurgeonAssignment;
import com.ot.entity.Hospital;
import com.ot.entity.OTRoom;
import com.ot.entity.ScheduledOperation;
import com.ot.entity.User;
import com.ot.entity.WardBed;
import com.ot.enums.BedStatus;
import com.ot.enums.OperationStatus;
import com.ot.enums.RoleType;
import com.ot.enums.RoomStatus;
import com.ot.enums.StaffRole;
import com.ot.repository.OTRoomRepository;
import com.ot.repository.ScheduledOperationRepository;
import com.ot.repository.UserRepository;
import com.ot.repository.WardBedRepository;
import com.ot.security.CustomUserDetails;
import com.ot.service.AdminDashboardService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final ScheduledOperationRepository operationRepository;
    private final OTRoomRepository roomRepository;
    private final UserRepository userRepository;
    private final WardBedRepository wardBedRepository;

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No user is logged in");
        }
        return ((CustomUserDetails) auth.getPrincipal()).getUser();
    }

    @Override
    public AdminDashboardResponse getDashboard() {

        User currentUser = currentUser();
        Long hospitalId = currentUser.getHospital().getId();
        Hospital hospital = currentUser.getHospital();
        LocalDateTime now = LocalDateTime.now();

        return AdminDashboardResponse.builder()
                .overviewStats(buildOverviewStats(hospitalId, hospital, now))
                .otRoomStatus(buildOTRoomStatus(hospitalId, now))
                .todaySchedule(buildTodaySchedule(hospitalId, now))
                .staffAvailability(buildStaffAvailability(hospital))
                .recentOperations(buildRecentOperations(hospitalId))
                .pendingRequests(buildPendingRequests(hospitalId, now))
                .overdueOperations(buildOverdueOperations(hospitalId, now))
                .build();
    }

    // ==================== Overview Stats ==================== //

    private AdminDashboardResponse.OverviewStats buildOverviewStats(
            Long hospitalId, Hospital hospital, LocalDateTime now) {

        // OT Room stats
        List<OTRoom> allRooms = roomRepository.findByHospital(hospital);
        long availableRooms = allRooms.stream()
                .filter(r -> r.getStatus().equals(RoomStatus.AVAILABLE) && r.getIsActive())
                .count();
        long occupiedRooms = allRooms.stream()
                .filter(r -> r.getStatus().equals(RoomStatus.OCCUPIED))
                .count();

        // Ward bed stats
        List<WardBed> allBeds = wardBedRepository.findByHospital(hospital);
        long occupiedBeds = allBeds.stream()
                .filter(b -> b.getStatus().equals(BedStatus.OCCUPIED))
                .count();
        long availableBeds = allBeds.stream()
                .filter(b -> b.getStatus().equals(BedStatus.AVAILABLE) && b.getIsActive())
                .count();

        // Operation stats
        long overdueCount = operationRepository
                .findOverdueOperations(hospitalId, now).size();

        return AdminDashboardResponse.OverviewStats.builder()
                .totalOperationsToday(operationRepository.countTodayOperations(hospitalId))
                .scheduledOperations(operationRepository
                        .countByHospitalIdAndStatus(hospitalId, OperationStatus.SCHEDULED))
                .inProgressOperations(operationRepository
                        .countByHospitalIdAndStatus(hospitalId, OperationStatus.IN_PROGRESS))
                .completedOperationsToday(operationRepository
                        .countByHospitalIdAndStatus(hospitalId, OperationStatus.COMPLETED))
                .cancelledOperationsToday(operationRepository
                        .countByHospitalIdAndStatus(hospitalId, OperationStatus.CANCELLED))
                .pendingOTRequests(operationRepository
                        .countByHospitalIdAndStatus(hospitalId, OperationStatus.REQUESTED))
                .availableOTRooms(availableRooms)
                .occupiedOTRooms(occupiedRooms)
                .totalWardBeds((long) allBeds.size())
                .occupiedWardBeds(occupiedBeds)
                .availableWardBeds(availableBeds)
                .overdueOperations(overdueCount)
                .build();
    }

    // ==================== OT Room Status ==================== //

    private List<AdminDashboardResponse.OTRoomStatusDTO> buildOTRoomStatus(
            Long hospitalId, LocalDateTime now) {

    	return roomRepository.findByHospitalId(hospitalId).stream()
    	        .map((OTRoom room) -> {

    	        	List<ScheduledOperation> ops = operationRepository
    	        	        .findByRoomAndStatus(room, OperationStatus.IN_PROGRESS);

    	        	ScheduledOperation currentOp = ops.isEmpty() ? null : ops.get(0);

    	            boolean isOverdue = currentOp != null
    	                    && currentOp.getScheduledEndTime() != null
    	                    && currentOp.getScheduledEndTime().isBefore(now);

    	            String primarySurgeon = currentOp != null
    	                    ? currentOp.getSupportingSurgeons().stream()
    	                            .filter(SurgeonAssignment::isPrimary)
    	                            .map(SurgeonAssignment::getSurgeonName)
    	                            .findFirst().orElse(null)
    	                    : null;

    	            return AdminDashboardResponse.OTRoomStatusDTO.builder()
    	                    .roomId(room.getId())
    	                    .roomNumber(room.getRoomNumber())
    	                    .roomName(room.getRoomName())
    	                    .status(room.getStatus().name())
    	                    .currentOperationRef(currentOp != null
    	                            ? currentOp.getOperationReference() : null)
    	                    .currentPatientName(currentOp != null
    	                            ? currentOp.getPatientName() : null)
    	                    .operationStartTime(currentOp != null
    	                            ? currentOp.getActualStartTime() : null)
    	                    .scheduledEndTime(currentOp != null
    	                            ? currentOp.getScheduledEndTime() : null)
    	                    .isOverdue(isOverdue)
    	                    .build();
    	        })
    	        .collect(Collectors.toList());
    }

    // ==================== Today's Schedule ==================== //

    private List<AdminDashboardResponse.TodayScheduleDTO> buildTodaySchedule(
            Long hospitalId, LocalDateTime now) {

        // 🔹 Start & End of Today
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().atTime(23, 59, 59);

        List<ScheduledOperation> operations =
                operationRepository.findTodayOperations(hospitalId, startOfDay, endOfDay);

        return operations.stream()
                .map(op -> {

                    // 🔹 Overdue check
                    boolean isOverdue = OperationStatus.IN_PROGRESS.equals(op.getStatus())
                            && op.getScheduledEndTime() != null
                            && op.getScheduledEndTime().isBefore(now);

                    // 🔹 Primary Surgeon (null-safe)
                    String primarySurgeon = (op.getSupportingSurgeons() != null)
                            ? op.getSupportingSurgeons().stream()
                                    .filter(SurgeonAssignment::isPrimary)
                                    .map(SurgeonAssignment::getSurgeonName)
                                    .findFirst()
                                    .orElse(null)
                            : null;

                    // 🔹 Anesthesiologist (null-safe)
                    String anesthesiologist = (op.getSupportingStaff() != null)
                            ? op.getSupportingStaff().stream()
                                    .filter(s -> StaffRole.ANESTHESIOLOGIST.equals(s.getRole()))
                                    .map(StaffAssignment::getStaffName)
                                    .findFirst()
                                    .orElse(null)
                            : null;

                    return AdminDashboardResponse.TodayScheduleDTO.builder()
                            .operationId(op.getId())
                            .operationReference(op.getOperationReference())
                            .patientName(op.getPatientName())
                            .patientMrn(op.getPatientMrn())
                            .procedureName(op.getProcedureName())
                            .roomNumber(op.getRoom() != null
                                    ? op.getRoom().getRoomNumber()
                                    : null)
                            .primarySurgeon(primarySurgeon)
                            .anesthesiologist(anesthesiologist)
                            .scheduledStartTime(op.getScheduledStartTime())
                            .scheduledEndTime(op.getScheduledEndTime())
                            .actualStartTime(op.getActualStartTime())
                            .status(op.getStatus() != null
                                    ? op.getStatus().name()
                                    : null)
                            .isOverdue(isOverdue)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ==================== Staff Availability ==================== //

    private AdminDashboardResponse.StaffAvailabilitySummary buildStaffAvailability(
            Hospital hospital) {

        List<User> allUsers = userRepository.findByHospitalAndIsActive(hospital, true);

        // Busy IDs — currently IN_PROGRESS operations mein assigned
        Set<Long> busySurgeonIds = operationRepository
                .findBusySurgeonIds(hospital.getId(),
                        LocalDateTime.now(), LocalDateTime.now());
        Set<Long> busyStaffIds = operationRepository
                .findBusyStaffIds(hospital.getId(),
                        LocalDateTime.now(), LocalDateTime.now());

        // Role wise count
        long totalSurgeons = countByRole(allUsers, RoleType.SURGEON);
        long busySurgeons = countBusyByRole(allUsers, RoleType.SURGEON,
                busySurgeonIds, busyStaffIds);

        long totalAnesthesiologists = countByRole(allUsers, RoleType.ANESTHESIOLOGIST);
        long busyAnesthesiologists = countBusyByRole(allUsers, RoleType.ANESTHESIOLOGIST,
                busySurgeonIds, busyStaffIds);

        long totalNurses = allUsers.stream()
                .filter(u -> Set.of(RoleType.SCRUB_NURSE, RoleType.CIRCULATING_NURSE,
                        RoleType.ANESTHESIA_NURSE).contains(u.getRole()))
                .count();
        long busyNurses = allUsers.stream()
                .filter(u -> Set.of(RoleType.SCRUB_NURSE, RoleType.CIRCULATING_NURSE,
                        RoleType.ANESTHESIA_NURSE).contains(u.getRole()))
                .filter(u -> busyStaffIds.contains(u.getId()))
                .count();

        long totalTechnicians = allUsers.stream()
                .filter(u -> Set.of(RoleType.OT_TECHNICIAN, RoleType.SURGICAL_TECH,
                        RoleType.ANESTHESIA_TECHNICIAN).contains(u.getRole()))
                .count();
        long busyTechnicians = allUsers.stream()
                .filter(u -> Set.of(RoleType.OT_TECHNICIAN, RoleType.SURGICAL_TECH,
                        RoleType.ANESTHESIA_TECHNICIAN).contains(u.getRole()))
                .filter(u -> busyStaffIds.contains(u.getId()))
                .count();

        return AdminDashboardResponse.StaffAvailabilitySummary.builder()
                .totalSurgeons((int) totalSurgeons)
                .availableSurgeons((int) (totalSurgeons - busySurgeons))
                .busySurgeons((int) busySurgeons)
                .totalAnesthesiologists((int) totalAnesthesiologists)
                .availableAnesthesiologists((int) (totalAnesthesiologists - busyAnesthesiologists))
                .busyAnesthesiologists((int) busyAnesthesiologists)
                .totalNurses((int) totalNurses)
                .availableNurses((int) (totalNurses - busyNurses))
                .busyNurses((int) busyNurses)
                .totalTechnicians((int) totalTechnicians)
                .availableTechnicians((int) (totalTechnicians - busyTechnicians))
                .busyTechnicians((int) busyTechnicians)
                .build();
    }

    // ==================== Recent Operations ==================== //

    private List<AdminDashboardResponse.RecentOperationDTO> buildRecentOperations(
            Long hospitalId) {

        return operationRepository.findRecentOperations(hospitalId,
                PageRequest.of(0, 10)).stream()
                .map(op -> {

                    Long duration = null;
                    if (op.getActualStartTime() != null && op.getActualEndTime() != null) {
                        duration = ChronoUnit.MINUTES.between(
                                op.getActualStartTime(), op.getActualEndTime());
                    }

                    String primarySurgeon = op.getSupportingSurgeons().stream()
                            .filter(SurgeonAssignment::isPrimary)
                            .map(SurgeonAssignment::getSurgeonName)
                            .findFirst().orElse(null);

                    return AdminDashboardResponse.RecentOperationDTO.builder()
                            .operationId(op.getId())
                            .operationReference(op.getOperationReference())
                            .patientName(op.getPatientName())
                            .procedureName(op.getProcedureName())
                            .roomNumber(op.getRoom() != null
                                    ? op.getRoom().getRoomNumber() : null)
                            .primarySurgeon(primarySurgeon)
                            .status(op.getStatus().name())
                            .scheduledStartTime(op.getScheduledStartTime())
                            .actualStartTime(op.getActualStartTime())
                            .actualEndTime(op.getActualEndTime())
                            .durationMinutes(duration)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ==================== Pending Requests ==================== //

    private List<AdminDashboardResponse.PendingOTRequestDTO> buildPendingRequests(
            Long hospitalId, LocalDateTime now) {

        return operationRepository.findPendingRequests(hospitalId).stream()
                .map(op -> {
                    long pendingHours = ChronoUnit.HOURS.between(op.getCreatedAt(), now);

                    return AdminDashboardResponse.PendingOTRequestDTO.builder()
                            .operationId(op.getId())
                            .operationReference(op.getOperationReference())
                            .patientName(op.getPatientName())
                            .patientMrn(op.getPatientMrn())
                            .procedureName(op.getProcedureName())
                            .complexity(op.getComplexity() != null
                                    ? op.getComplexity().name() : null)
                            .requestedAt(op.getCreatedAt())
                            .pendingSinceHours(pendingHours)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ==================== Overdue Operations ==================== //

    private List<AdminDashboardResponse.OverdueOperationDTO> buildOverdueOperations(
            Long hospitalId, LocalDateTime now) {

        return operationRepository.findOverdueOperations(hospitalId, now).stream()
                .map(op -> {
                    long exceededMinutes = ChronoUnit.MINUTES.between(
                            op.getScheduledEndTime(), now);

                    String primarySurgeon = op.getSupportingSurgeons().stream()
                            .filter(SurgeonAssignment::isPrimary)
                            .map(SurgeonAssignment::getSurgeonName)
                            .findFirst().orElse(null);

                    return AdminDashboardResponse.OverdueOperationDTO.builder()
                            .operationId(op.getId())
                            .operationReference(op.getOperationReference())
                            .patientName(op.getPatientName())
                            .procedureName(op.getProcedureName())
                            .roomNumber(op.getRoom() != null
                                    ? op.getRoom().getRoomNumber() : null)
                            .primarySurgeon(primarySurgeon)
                            .scheduledEndTime(op.getScheduledEndTime())
                            .actualStartTime(op.getActualStartTime())
                            .exceededByMinutes(exceededMinutes)
                            .status(op.getStatus().name())
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ==================== Helpers ==================== //

    private long countByRole(List<User> users, RoleType role) {
        return users.stream().filter(u -> u.getRole().equals(role)).count();
    }

    private long countBusyByRole(List<User> users, RoleType role,
            Set<Long> busySurgeonIds, Set<Long> busyStaffIds) {
        return users.stream()
                .filter(u -> u.getRole().equals(role))
                .filter(u -> busySurgeonIds.contains(u.getId())
                        || busyStaffIds.contains(u.getId()))
                .count();
    }
}
