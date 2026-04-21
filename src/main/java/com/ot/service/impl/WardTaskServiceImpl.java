package com.ot.service.impl;

import com.ot.dto.ward.CompleteWardTaskRequest;
import com.ot.dto.ward.CreateWardTaskRequest;
import com.ot.dto.ward.WardTaskResponse;
import com.ot.entity.*;
import com.ot.enums.TaskStatus;
import com.ot.exception.OperationNotAllowedException;
import com.ot.exception.ResourceNotFoundException;
import com.ot.exception.UnauthorizedException;
import com.ot.exception.ValidationException;
import com.ot.repository.ScheduledOperationRepository;
import com.ot.repository.WardAdmissionRepository;
import com.ot.repository.WardTaskRepository;
import com.ot.security.CustomUserDetails;
import com.ot.service.WardTaskService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WardTaskServiceImpl implements WardTaskService {

    private final WardTaskRepository wardTaskRepository;
    private final WardAdmissionRepository wardAdmissionRepository;
    private final ScheduledOperationRepository scheduledOperationRepository;

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No user is logged in");
        }
        return ((CustomUserDetails) auth.getPrincipal()).getUser();
    }

    // -------------------- Create -------------------- //

    @Transactional
    @Override
    public WardTaskResponse createTask(CreateWardTaskRequest request) {

        User currentUser = currentUser();

        // Operation validate karo
        ScheduledOperation operation = scheduledOperationRepository
                .findById(request.getOperationId())
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        //Get WardAdmission Id By Operation
        
        // WardAdmission validate karo
        WardAdmission admission = wardAdmissionRepository
        		.findByOperationIdAndDischargedWhenIsNull(operation.getId())
//                .findById(request.getWardAdmissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Ward admission not found"));

        // Admission aur operation match hona chahiye
        if (!admission.getOperation().getId().equals(request.getOperationId())) {
            throw new ValidationException("Ward admission does not belong to this operation");
        }

        // Patient already discharged ho gaya to task nahi banana
        if (admission.getDischargedWhen() != null) {
            throw new OperationNotAllowedException("Cannot create task — patient is already discharged");
        }

        // Recurring validation
        if (Boolean.TRUE.equals(request.getIsRecurring()) && request.getIntervalHours() == null) {
            throw new ValidationException("intervalHours is required for recurring tasks");
        }

        WardTask task = WardTask.builder()
                .scheduledOperation(operation)
                .wardAdmission(admission)
                .hospital(currentUser.getHospital())
                // Patient snapshot — admission se lo
                .patientId(admission.getPatientId())
                .patientName(admission.getPatientName())
                .patientMrn(admission.getPatientMrn())
                // Task
                .taskType(request.getTaskType())
                .taskDescription(request.getTaskDescription())
                .taskNotes(request.getTaskNotes())
                .scheduledTime(request.getScheduledTime())
                // Recurring
                .isRecurring(request.getIsRecurring())
                .intervalHours(request.getIntervalHours())
                .recurringEndTime(request.getRecurringEndTime())
                // Assigned by — current logged in user
                .assignedById(currentUser.getId())
                .assignedByName(currentUser.getUserName())
                .assignedAt(LocalDateTime.now())
                // Default status
                .status(TaskStatus.PENDING)
                .build();

        wardTaskRepository.save(task);

        return mapToResponse(task);
    }

    // -------------------- Complete -------------------- //

    @Transactional
    @Override
    public WardTaskResponse completeTask(Long taskId, CompleteWardTaskRequest request) {

        User currentUser = currentUser();

        WardTask task = fetchAndValidateHospital(taskId, currentUser);

        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new OperationNotAllowedException("Task is already completed");
        }

        if (task.getStatus() == TaskStatus.CANCELLED) {
            throw new OperationNotAllowedException("Cannot complete a cancelled task");
        }

        task.setStatus(TaskStatus.COMPLETED);
        // Koi bhi complete kar sakta hai — no restriction
        task.setCompletedById(currentUser.getId());
        task.setCompletedByName(currentUser.getUserName());
        task.setCompletedAt(LocalDateTime.now());
        task.setCompletionNotes(request.getCompletionNotes());
        task.setReadingValue(request.getReadingValue());
        task.setReadingUnit(request.getReadingUnit());

        wardTaskRepository.save(task);

        return mapToResponse(task);
    }

    // -------------------- Cancel -------------------- //

    @Transactional
    @Override
    public WardTaskResponse cancelTask(Long taskId) {

        User currentUser = currentUser();

        WardTask task = fetchAndValidateHospital(taskId, currentUser);

        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new OperationNotAllowedException("Cannot cancel a completed task");
        }

        if (task.getStatus() == TaskStatus.CANCELLED) {
            throw new OperationNotAllowedException("Task is already cancelled");
        }

        task.setStatus(TaskStatus.CANCELLED);
        wardTaskRepository.save(task);

        return mapToResponse(task);
    }

    // -------------------- Get -------------------- //

    @Override
    public WardTaskResponse getById(Long taskId) {
        User currentUser = currentUser();
        WardTask task = fetchAndValidateHospital(taskId, currentUser);
        return mapToResponse(task);
    }

    @Override
    public List<WardTaskResponse> getByOperation(Long operationId) {

        User currentUser = currentUser();

        return wardTaskRepository
                .findByScheduledOperationIdOrderByScheduledTimeAsc(operationId)
                .stream()
                .filter(t -> t.getHospital().getId().equals(currentUser.getHospital().getId()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<WardTaskResponse> getByAdmission(Long wardAdmissionId) {

        User currentUser = currentUser();

        return wardTaskRepository
                .findByWardAdmissionIdOrderByScheduledTimeAsc(wardAdmissionId)
                .stream()
                .filter(t -> t.getHospital().getId().equals(currentUser.getHospital().getId()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<WardTaskResponse> getByOperationAndStatus(Long operationId, TaskStatus status) {

        User currentUser = currentUser();

        return wardTaskRepository
                .findByScheduledOperationIdAndStatusOrderByScheduledTimeAsc(operationId, status)
                .stream()
                .filter(t -> t.getHospital().getId().equals(currentUser.getHospital().getId()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // -------------------- Helpers -------------------- //

    private WardTask fetchAndValidateHospital(Long taskId, User currentUser) {
        WardTask task = wardTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward task not found"));

        if (!task.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this task");
        }
        return task;
    }

    // -------------------- Mapper -------------------- //

    private WardTaskResponse mapToResponse(WardTask t) {
        return WardTaskResponse.builder()
                .id(t.getId())
                .operationId(t.getScheduledOperation().getId())
                .wardAdmissionId(t.getWardAdmission().getId())
                .patientId(t.getPatientId())
                .patientName(t.getPatientName())
                .patientMrn(t.getPatientMrn())
                .taskType(t.getTaskType())
                .taskDescription(t.getTaskDescription())
                .taskNotes(t.getTaskNotes())
                .scheduledTime(t.getScheduledTime())
                .isRecurring(t.getIsRecurring())
                .intervalHours(t.getIntervalHours())
                .recurringEndTime(t.getRecurringEndTime())
                .assignedById(t.getAssignedById())
                .assignedByName(t.getAssignedByName())
                .assignedAt(t.getAssignedAt())
                .status(t.getStatus())
                .completedById(t.getCompletedById())
                .completedByName(t.getCompletedByName())
                .completedAt(t.getCompletedAt())
                .completionNotes(t.getCompletionNotes())
                .readingValue(t.getReadingValue())
                .readingUnit(t.getReadingUnit())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}