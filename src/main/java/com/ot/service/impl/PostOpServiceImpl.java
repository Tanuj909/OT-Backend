package com.ot.service.impl;

import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ot.dto.postOp.PostOpResponse;
import com.ot.dto.postOp.PostOpTransferRequest;
import com.ot.dto.postOp.PostOpUpdateRequest;
import com.ot.entity.OTRoom;
import com.ot.entity.PostOpRecord;
import com.ot.entity.ScheduledOperation;
import com.ot.entity.User;
import com.ot.entity.Ward;
import com.ot.enums.RecoveryStatus;
import com.ot.enums.RoomStatus;
import com.ot.exception.ResourceNotFoundException;
import com.ot.exception.UnauthorizedException;
import com.ot.exception.ValidationException;
import com.ot.repository.OTRoomRepository;
import com.ot.repository.PostOpRecordRepository;
import com.ot.repository.ScheduledOperationRepository;
import com.ot.repository.WardRepository;
import com.ot.security.CustomUserDetails;
import com.ot.service.PostOpService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostOpServiceImpl implements PostOpService {

    private final PostOpRecordRepository postOpRepository;
    private final ScheduledOperationRepository operationRepository;
    private final OTRoomRepository roomRepository;
    private final WardRepository wardRepository;

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No user is logged in");
        }
        return ((CustomUserDetails) auth.getPrincipal()).getUser();
    }

    @Override
    public PostOpResponse getPostOpRecord(Long operationId) {

        User currentUser = currentUser();

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        PostOpRecord postOp = operation.getPostOp();
        if (postOp == null) {
            throw new ResourceNotFoundException("PostOp record not found for this operation");
        }

        return mapToResponse(postOp);
    }

    @Transactional
    @Override
    public PostOpResponse updatePostOpRecord(Long operationId, PostOpUpdateRequest request) {

        User currentUser = currentUser();

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        PostOpRecord postOp = operation.getPostOp();
        if (postOp == null) {
            throw new ResourceNotFoundException("PostOp record not found");
        }

        // TRANSFERRED check — transfer ho gaya toh update nahi
        if (postOp.getStatus().equals(RecoveryStatus.TRANSFERRED)) {
            throw new ValidationException("Cannot update — patient already transferred");
        }

        // Partial update
        if (request.getAldreteScore() != null)              postOp.setAldreteScore(request.getAldreteScore());
        if (request.getImmediatePostOpCondition() != null)  postOp.setImmediatePostOpCondition(request.getImmediatePostOpCondition());
        if (request.getPainManagement() != null)            postOp.setPainManagement(request.getPainManagement());
        if (request.getMedicationsGiven() != null)          postOp.setMedicationsGiven(request.getMedicationsGiven());
        if (request.getDrainDetails() != null)              postOp.setDrainDetails(request.getDrainDetails());
        if (request.getDressingDetails() != null)           postOp.setDressingDetails(request.getDressingDetails());
        if (request.getPostOpInstructions() != null)        postOp.setPostOpInstructions(request.getPostOpInstructions());
        if (request.getFollowUpPlan() != null)              postOp.setFollowUpPlan(request.getFollowUpPlan());
        if (request.getRecoveryStartTime() != null)         postOp.setRecoveryStartTime(request.getRecoveryStartTime());
        if (request.getRecoveryEndTime() != null)           postOp.setRecoveryEndTime(request.getRecoveryEndTime());

        postOpRepository.save(postOp);

        return mapToResponse(postOp);
    }

    @Transactional
    @Override
    public PostOpResponse transferPatient(Long operationId, PostOpTransferRequest request) {

        User currentUser = currentUser();

        // 1. Operation fetch
        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        // 2. Hospital check
        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        // 3. PostOp exists check
        PostOpRecord postOp = operation.getPostOp();
        if (postOp == null) {
            throw new ResourceNotFoundException("PostOp record not found");
        }

        // 4. Already transferred check
        if (postOp.getStatus().equals(RecoveryStatus.TRANSFERRED)) {
            throw new ValidationException("Patient already transferred to " + postOp.getTransferredTo());
        }

        // 5. IN_RECOVERY check
        if (!postOp.getStatus().equals(RecoveryStatus.IN_RECOVERY)) {
            throw new ValidationException("Patient is not in recovery — current status: " + postOp.getStatus());
        }

        // 6. Ward fetch
        Ward ward = wardRepository.findById(request.getWardId())
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found"));

        // 7. Same hospital check — ward bhi same hospital ka hona chahiye
        if (!ward.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("Ward does not belong to your hospital");
        }

        // 8. Ward active check
        if (!ward.getIsActive()) {
            throw new ValidationException("Ward is inactive — cannot transfer patient");
        }

        // 9. Available beds check
        if (ward.getAvailableBeds() <= 0) {
            throw new ValidationException("No available beds in " + ward.getWardName()
                    + " (Ward: " + ward.getWardNumber() + ")");
        }

        // 10. Update PostOp
        postOp.setTransferredTo(ward.getWardNumber() + " — " + ward.getWardName());
        postOp.setTransferredBy(request.getTransferredBy());
        postOp.setReceivedBy(request.getReceivedBy());
        postOp.setRecoveryEndTime(LocalDateTime.now());
        postOp.setStatus(RecoveryStatus.TRANSFERRED);

        // 11. Ward occupiedBeds update
        ward.setOccupiedBeds(ward.getOccupiedBeds() + 1);
        wardRepository.save(ward);

        // 12. OTRoom → AVAILABLE
        OTRoom room = operation.getRoom();
        if (room != null) {
            room.setStatus(RoomStatus.AVAILABLE);
            roomRepository.save(room);
        }

        postOpRepository.save(postOp);

        return mapToResponse(postOp);
    }

    // Mapper
    private PostOpResponse mapToResponse(PostOpRecord postOp) {
        return PostOpResponse.builder()
                .id(postOp.getId())
                .operationId(postOp.getScheduledOperation().getId())
                .surgeryEndTime(postOp.getSurgeryEndTime())
                .recoveryStartTime(postOp.getRecoveryStartTime())
                .recoveryEndTime(postOp.getRecoveryEndTime())
                .recoveryLocation(postOp.getRecoveryLocation())
                .aldreteScore(postOp.getAldreteScore())
                .immediatePostOpCondition(postOp.getImmediatePostOpCondition())
                .painManagement(postOp.getPainManagement())
                .medicationsGiven(postOp.getMedicationsGiven())
                .drainDetails(postOp.getDrainDetails())
                .dressingDetails(postOp.getDressingDetails())
                .postOpInstructions(postOp.getPostOpInstructions())
                .followUpPlan(postOp.getFollowUpPlan())
                .transferredTo(postOp.getTransferredTo())
                .transferredBy(postOp.getTransferredBy())
                .receivedBy(postOp.getReceivedBy())
                .status(postOp.getStatus())
                .createdBy(postOp.getCreatedBy())
                .createdAt(postOp.getCreatedAt())
                .updatedAt(postOp.getUpdatedAt())
                .build();
    }
}