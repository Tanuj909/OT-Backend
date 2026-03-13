package com.ot.service.impl;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.ot.constants.OTRoleConstants;
import com.ot.dto.preOpRequest.PreOpAssessmentRequest;
import com.ot.dto.preOpRequest.PreOpStatusUpdateRequest;
import com.ot.dto.preOpResponse.PreOpAssessmentResponse;
import com.ot.entity.PreOpAssessment;
import com.ot.entity.ScheduledOperation;
import com.ot.entity.User;
import com.ot.enums.AssessmentStatus;
import com.ot.enums.OperationStatus;
import com.ot.exception.ResourceNotFoundException;
import com.ot.exception.ValidationException;
import com.ot.mapper.PreOpMapper;
import com.ot.repository.PreOpAssessmentRepository;
import com.ot.repository.ScheduledOperationRepository;
import com.ot.security.CustomUserDetails;
import com.ot.service.PreOpService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PreOpServiceImpl implements PreOpService {

    private final PreOpAssessmentRepository preOpRepository;
    private final ScheduledOperationRepository operationRepository;

    // ---------------------------------------- Helper ---------------------------------------- //

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No user is logged in");
        }
        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
        return cud.getUser();
    }

    // ---------------------------------------- Create ---------------------------------------- //

    @Transactional
    @Override
    public PreOpAssessmentResponse createPreOpAssessment(Long operationId, PreOpAssessmentRequest request) {

        // 1. Current logged in user
        User currentUser = currentUser();

        // 2. Role check
        if (!OTRoleConstants.ALLOWED_ASSESSOR_ROLES.contains(currentUser.getRole())) {
            throw new ValidationException("User with role " + currentUser.getRole() + " cannot create pre-op assessment");
        }

        // 3. Operation fetch
        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        // 4. Same hospital check
        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new ValidationException("You are not authorized to access this operation");
        }

        // 5. Status check
        if (!operation.getStatus().equals(OperationStatus.SCHEDULED)) {
            throw new ValidationException("Pre-op assessment can only be created for scheduled operations");
        }

        // 6. Already exists check
        if (operation.getPreOp() != null) {
            throw new ValidationException("Pre-op assessment already exists for this operation");
        }

        // 7. BMI calculate
        Double bmi = null;
        if (request.getHeight() != null && request.getWeight() != null && request.getHeight() > 0) {
            double heightInMeters = request.getHeight() / 100.0;
            bmi = Math.round((request.getWeight() / (heightInMeters * heightInMeters)) * 100.0) / 100.0;
        }

        // 8. Build and save
        PreOpAssessment preOp = PreOpAssessment.builder()
                .hospital(operation.getHospital())
                .scheduledOperation(operation)
                .patientId(operation.getPatientId())            // operation se auto set
                .assessmentDate(LocalDateTime.now())            // auto set
                .assessedBy(currentUser.getUserName())          // security se auto set
                .height(request.getHeight())
                .weight(request.getWeight())
                .bmi(bmi)
                .bloodGroup(request.getBloodGroup())
                .allergies(request.getAllergies())
                .currentMedications(request.getCurrentMedications())
                .pastMedicalHistory(request.getPastMedicalHistory())
                .pastSurgicalHistory(request.getPastSurgicalHistory())
                .physicalExamination(request.getPhysicalExamination())
                .ecgFindings(request.getEcgFindings())
                .labResults(request.getLabResults())
                .radiologyFindings(request.getRadiologyFindings())
                .asaGrade(request.getAsaGrade())
                .npoStatus(request.getNpoStatus())
                .anesthesiaPlan(request.getAnesthesiaPlan())
                .specialInstructions(request.getSpecialInstructions())
                .status(AssessmentStatus.PENDING)
                .createdBy(currentUser.getUserName())           // security se auto set
                .build();

        preOpRepository.save(preOp);

        return PreOpMapper.toResponse(preOp);
    }

    // ----------------------------------------- Get ------------------------------------------ //

    @Override
    public PreOpAssessmentResponse getPreOpAssessment(Long operationId) {

        User currentUser = currentUser();

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        // Same hospital check
        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new ValidationException("You are not authorized to access this operation");
        }

        if (operation.getPreOp() == null) {
            throw new ResourceNotFoundException("Pre-op assessment not found for this operation");
        }

        return PreOpMapper.toResponse(operation.getPreOp());
    }

    // ---------------------------------------- Update ---------------------------------------- //

    @Transactional
    @Override
    public PreOpAssessmentResponse updatePreOpAssessment(Long operationId, PreOpAssessmentRequest request) {

        User currentUser = currentUser();

        // Role check
        if (!OTRoleConstants.ALLOWED_ASSESSOR_ROLES.contains(currentUser.getRole())) {
            throw new ValidationException("User with role " + currentUser.getRole() + " cannot update pre-op assessment");
        }

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        // Same hospital check
        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new ValidationException("You are not authorized to access this operation");
        }

        PreOpAssessment preOp = operation.getPreOp();
        if (preOp == null) {
            throw new ResourceNotFoundException("Pre-op assessment not found for this operation");
        }

        // COMPLETED lock
        if (preOp.getStatus().equals(AssessmentStatus.COMPLETED)) {
            throw new ValidationException("Completed pre-op assessment cannot be updated");
        }

        // BMI recalculate
        Double height = request.getHeight() != null ? request.getHeight() : preOp.getHeight();
        Double weight = request.getWeight() != null ? request.getWeight() : preOp.getWeight();
        Double bmi = preOp.getBmi();

        if (height != null && weight != null && height > 0) {
            double heightInMeters = height / 100.0;
            bmi = Math.round((weight / (heightInMeters * heightInMeters)) * 100.0) / 100.0;
        }

        // Partial update
        if (request.getHeight() != null)              preOp.setHeight(request.getHeight());
        if (request.getWeight() != null)              preOp.setWeight(request.getWeight());
        if (request.getBloodGroup() != null)          preOp.setBloodGroup(request.getBloodGroup());
        if (request.getAllergies() != null)            preOp.setAllergies(request.getAllergies());
        if (request.getCurrentMedications() != null)  preOp.setCurrentMedications(request.getCurrentMedications());
        if (request.getPastMedicalHistory() != null)  preOp.setPastMedicalHistory(request.getPastMedicalHistory());
        if (request.getPastSurgicalHistory() != null) preOp.setPastSurgicalHistory(request.getPastSurgicalHistory());
        if (request.getPhysicalExamination() != null) preOp.setPhysicalExamination(request.getPhysicalExamination());
        if (request.getEcgFindings() != null)         preOp.setEcgFindings(request.getEcgFindings());
        if (request.getLabResults() != null)          preOp.setLabResults(request.getLabResults());
        if (request.getRadiologyFindings() != null)   preOp.setRadiologyFindings(request.getRadiologyFindings());
        if (request.getAsaGrade() != null)            preOp.setAsaGrade(request.getAsaGrade());
        if (request.getNpoStatus() != null)           preOp.setNpoStatus(request.getNpoStatus());
        if (request.getAnesthesiaPlan() != null)      preOp.setAnesthesiaPlan(request.getAnesthesiaPlan());
        if (request.getSpecialInstructions() != null) preOp.setSpecialInstructions(request.getSpecialInstructions());
        if (request.getStatus() != null)              preOp.setStatus(request.getStatus());

        preOp.setBmi(bmi);

        preOpRepository.save(preOp);

        return PreOpMapper.toResponse(preOp);
    }
    
    
    @Transactional
    @Override
    public PreOpAssessmentResponse updatePreOpStatus(Long operationId, PreOpStatusUpdateRequest request) {

        User currentUser = currentUser();

        // 1. Role check
        if (!OTRoleConstants.ALLOWED_ASSESSOR_ROLES.contains(currentUser.getRole())) {
            throw new ValidationException("User with role " + currentUser.getRole() + " cannot update pre-op status");
        }

        // 2. Operation fetch
        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        // 3. Same hospital check
        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new ValidationException("You are not authorized to access this operation");
        }

        // 4. PreOp exists check
        PreOpAssessment preOp = operation.getPreOp();
        if (preOp == null) {
            throw new ResourceNotFoundException("Pre-op assessment not found for this operation");
        }

        // 5. Null status check
        if (request.getStatus() == null) {
            throw new ValidationException("Status cannot be null");
        }

        // 6. Same status check
        if (preOp.getStatus().equals(request.getStatus())) {
            throw new ValidationException("Pre-op assessment is already in " + request.getStatus() + " status");
        }

        // 7. Reason mandatory for CANCELLED and REASSESSMENT_REQUIRED
        if ((request.getStatus().equals(AssessmentStatus.CANCELLED) ||
             request.getStatus().equals(AssessmentStatus.REASSESSMENT_REQUIRED))
             && (request.getReason() == null || request.getReason().isBlank())) {
            throw new ValidationException("Reason is mandatory for " + request.getStatus() + " status");
        }

        // 8. Valid transition check
        validateStatusTransition(preOp.getStatus(), request.getStatus());

        preOp.setStatus(request.getStatus());
        preOp.setStatusChangeReason(request.getReason());

        preOpRepository.save(preOp);

        return PreOpMapper.toResponse(preOp);
    }
    
 // Valid transitions
    private void validateStatusTransition(AssessmentStatus current, AssessmentStatus next) {
        Map<AssessmentStatus, Set<AssessmentStatus>> allowed = Map.of(
            AssessmentStatus.PENDING,                Set.of(AssessmentStatus.COMPLETED, AssessmentStatus.CANCELLED, AssessmentStatus.REASSESSMENT_REQUIRED),
            AssessmentStatus.REASSESSMENT_REQUIRED,  Set.of(AssessmentStatus.PENDING, AssessmentStatus.CANCELLED),
            AssessmentStatus.COMPLETED,              Set.of(AssessmentStatus.REASSESSMENT_REQUIRED), // galti se complete mark ho gayi toh
            AssessmentStatus.CANCELLED,              Set.of() // CANCELLED terminal state hai
        );

        if (!allowed.get(current).contains(next)) {
            throw new ValidationException("Invalid status transition: " + current + " → " + next);
        }

}}