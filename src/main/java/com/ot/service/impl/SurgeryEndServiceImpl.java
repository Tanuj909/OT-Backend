package com.ot.service.impl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.ot.constants.OTRoleConstants;
import com.ot.dto.surgeryEnd.SurgeryEndRequest;
import com.ot.dto.surgeryEnd.SurgeryEndResponse;
import com.ot.entity.IntraOpRecord;
import com.ot.entity.OTRoom;
import com.ot.entity.PostOpRecord;
import com.ot.entity.ScheduledOperation;
import com.ot.entity.User;
import com.ot.enums.OperationStatus;
import com.ot.enums.RecoveryStatus;
import com.ot.enums.RoomStatus;
import com.ot.enums.SurgeryStatus;
import com.ot.exception.ResourceNotFoundException;
import com.ot.exception.UnauthorizedException;
import com.ot.exception.ValidationException;
import com.ot.repository.IntraOpRepository;
import com.ot.repository.OTRoomRepository;
import com.ot.repository.PostOpRecordRepository;
import com.ot.repository.ScheduledOperationRepository;
import com.ot.security.CustomUserDetails;
import com.ot.service.SurgeryEndService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SurgeryEndServiceImpl implements SurgeryEndService {

    private final ScheduledOperationRepository operationRepository;
    private final PostOpRecordRepository postOpRepository;
    private final IntraOpRepository intraOpRepository;
    private final OTRoomRepository roomRepository;

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No user is logged in");
        }
        return ((CustomUserDetails) auth.getPrincipal()).getUser();
    }

    @Transactional
    @Override
    public SurgeryEndResponse endSurgery(Long operationId, SurgeryEndRequest request) {

        User currentUser = currentUser();

        // 1. Role check
        if (!OTRoleConstants.ALLOWED_SURGERY_START_ROLES.contains(currentUser.getRole())) {
            throw new ValidationException("User with role " + currentUser.getRole() + " cannot end surgery");
        }

        // 2. Operation fetch
        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        // 3. Hospital check
        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        // 4. Status check — sirf IN_PROGRESS end ho sakti hai
        if (!operation.getStatus().equals(OperationStatus.IN_PROGRESS)) {
            throw new ValidationException("Only IN_PROGRESS operations can be ended, current status: "
                    + operation.getStatus());
        }

        // 5. Already ended check
        if (operation.getPostOp() != null) {
            throw new ValidationException("Surgery already ended for this operation");
        }

        LocalDateTime endTime = LocalDateTime.now();

        // 6. Operation update
        operation.setStatus(OperationStatus.COMPLETED);
        operation.setActualEndTime(endTime);
        operation.setUpdatedBy(currentUser.getUserName());

        // 7. IntraOp COMPLETED
        IntraOpRecord intraOp = operation.getIntraOp();
        if (intraOp != null) {
            intraOp.setStatus(SurgeryStatus.COMPLETED);
            intraOpRepository.save(intraOp);
        }

        // 8. OTRoom — UNDER_CLEANING (cleaning ke baad AVAILABLE hoga)
        OTRoom room = operation.getRoom();
        if (room != null) {
            room.setStatus(RoomStatus.CLEANING);
            roomRepository.save(room);
        }

        // 9. Surgery duration calculate
        long durationMinutes = 0;
        if (operation.getActualStartTime() != null) {
            durationMinutes = ChronoUnit.MINUTES.between(operation.getActualStartTime(), endTime);
        }

        // 10. PostOpRecord auto create
        PostOpRecord postOp = PostOpRecord.builder()
                .hospital(operation.getHospital())
                .scheduledOperation(operation)
                .surgeryEndTime(endTime)
                .recoveryStartTime(LocalDateTime.now())
                .recoveryLocation(request.getRecoveryLocation())
                .immediatePostOpCondition(request.getImmediatePostOpCondition())
                .drainDetails(request.getDrainDetails())
                .dressingDetails(request.getDressingDetails())
                .status(RecoveryStatus.IN_RECOVERY)
                .createdBy(currentUser.getUserName())
                .build();

        postOpRepository.save(postOp);
        operationRepository.save(operation);

        return SurgeryEndResponse.builder()
                .operationId(operation.getId())
                .operationStatus(operation.getStatus())
                .actualStartTime(operation.getActualStartTime())
                .actualEndTime(endTime)
                .surgeryDurationMinutes(durationMinutes)
                .endedBy(currentUser.getUserName())
                .postOpRecordId(postOp.getId())
                .build();
    }
}
