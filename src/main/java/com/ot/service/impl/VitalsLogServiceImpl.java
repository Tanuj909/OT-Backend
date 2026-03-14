package com.ot.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ot.dto.vitalsLog.VitalsLogBulkRequest;
import com.ot.dto.vitalsLog.VitalsLogRequest;
import com.ot.dto.vitalsLog.VitalsLogResponse;
import com.ot.entity.ScheduledOperation;
import com.ot.entity.User;
import com.ot.entity.VitalsLog;
import com.ot.enums.OperationStatus;
import com.ot.exception.ResourceNotFoundException;
import com.ot.exception.UnauthorizedException;
import com.ot.repository.ScheduledOperationRepository;
import com.ot.repository.VitalsLogRepository;
import com.ot.security.CustomUserDetails;
import com.ot.service.VitalsLogService;

import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VitalsLogServiceImpl implements VitalsLogService {

    private final VitalsLogRepository vitalsLogRepository;
    private final ScheduledOperationRepository operationRepository;

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No user is logged in");
        }
        return ((CustomUserDetails) auth.getPrincipal()).getUser();
    }

    // ---------------------------------------- Add Vitals ---------------------------------------- //

    @Transactional
    @Override
    public VitalsLogResponse addVitals(Long operationId, VitalsLogRequest request) {

        User currentUser = currentUser();

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        // Hospital check
        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        // Sirf IN_PROGRESS operation ka vitals log ho sakta hai
        if (!operation.getStatus().equals(OperationStatus.IN_PROGRESS)) {
            throw new ValidationException("Vitals can only be recorded for IN_PROGRESS operations");
        }

        // meanBp auto calculate karo agar systolic aur diastolic dono hain
        Integer meanBp = request.getMeanBp();
        if (meanBp == null && request.getSystolicBp() != null && request.getDiastolicBp() != null) {
            meanBp = (request.getSystolicBp() + (2 * request.getDiastolicBp())) / 3;
        }

        VitalsLog vitalsLog = VitalsLog.builder()
                .hospital(operation.getHospital())
                .scheduledOperation(operation)
                .recordedTime(LocalDateTime.now())
                .recordedBy(currentUser.getUserName())
                .heartRate(request.getHeartRate())
                .systolicBp(request.getSystolicBp())
                .diastolicBp(request.getDiastolicBp())
                .meanBp(meanBp)
                .respiratoryRate(request.getRespiratoryRate())
                .temperature(request.getTemperature())
                .oxygenSaturation(request.getOxygenSaturation())
                .etco2(request.getEtco2())
                .painScale(request.getPainScale())
                .consciousness(request.getConsciousness())
                .sedationScore(request.getSedationScore())
                .additionalNotes(request.getAdditionalNotes())
                .build();

        vitalsLogRepository.save(vitalsLog);

        return mapToResponse(vitalsLog);
    }
    
    // ---------------------------------------- Bulk Upload Vitals ---------------------------------------- //
    @Transactional
    @Override
    public List<VitalsLogResponse> addBulkVitals(Long operationId, VitalsLogBulkRequest bulkRequest) {

        User currentUser = currentUser();

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        // Hospital check
        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        // Status check
        if (!operation.getStatus().equals(OperationStatus.IN_PROGRESS)) {
            throw new ValidationException("Vitals can only be recorded for IN_PROGRESS operations");
        }

        // Empty list check
        if (bulkRequest.getVitals() == null || bulkRequest.getVitals().isEmpty()) {
            throw new ValidationException("Vitals list cannot be empty");
        }

        // Max limit check — ek baar mein zyada nahi
        if (bulkRequest.getVitals().size() > 50) {
            throw new ValidationException("Cannot add more than 50 vitals at once");
        }

        List<VitalsLog> vitalsLogs = bulkRequest.getVitals().stream()
                .map(request -> {
                    // meanBp auto calculate
                    Integer meanBp = request.getMeanBp();
                    if (meanBp == null && request.getSystolicBp() != null && request.getDiastolicBp() != null) {
                        meanBp = (request.getSystolicBp() + (2 * request.getDiastolicBp())) / 3;
                    }

                    return VitalsLog.builder()
                            .hospital(operation.getHospital())
                            .scheduledOperation(operation)
                            .recordedTime(LocalDateTime.now())
                            .recordedBy(currentUser.getUserName())
                            .heartRate(request.getHeartRate())
                            .systolicBp(request.getSystolicBp())
                            .diastolicBp(request.getDiastolicBp())
                            .meanBp(meanBp)
                            .respiratoryRate(request.getRespiratoryRate())
                            .temperature(request.getTemperature())
                            .oxygenSaturation(request.getOxygenSaturation())
                            .etco2(request.getEtco2())
                            .painScale(request.getPainScale())
                            .consciousness(request.getConsciousness())
                            .sedationScore(request.getSedationScore())
                            .additionalNotes(request.getAdditionalNotes())
                            .build();
                })
                .collect(Collectors.toList());

        vitalsLogRepository.saveAll(vitalsLogs);

        return vitalsLogs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ---------------------------------------- Get All ---------------------------------------- //

    @Override
    public List<VitalsLogResponse> getVitals(Long operationId) {

        User currentUser = currentUser();

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        return vitalsLogRepository
                .findByScheduledOperationIdOrderByRecordedTimeAsc(operationId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ---------------------------------------- Get Latest ---------------------------------------- //

    @Override
    public VitalsLogResponse getLatestVitals(Long operationId) {

        User currentUser = currentUser();

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        return vitalsLogRepository
                .findByScheduledOperationIdOrderByRecordedTimeAsc(operationId)
                .stream()
                .reduce((first, second) -> second) // last element
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No vitals found for this operation"));
    }

    // ---------------------------------------- Delete ---------------------------------------- //

    @Transactional
    @Override
    public void deleteVitals(Long operationId, Long vitalsId) {

        User currentUser = currentUser();

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        VitalsLog vitalsLog = vitalsLogRepository.findById(vitalsId)
                .orElseThrow(() -> new ResourceNotFoundException("Vitals log not found"));

        // Sirf same operation ka vitals delete ho
        if (!vitalsLog.getScheduledOperation().getId().equals(operationId)) {
            throw new ValidationException("Vitals log does not belong to this operation");
        }

        vitalsLogRepository.delete(vitalsLog);
    }
    
    

    // ---------------------------------------- Mapper ---------------------------------------- //

    private VitalsLogResponse mapToResponse(VitalsLog vitalsLog) {
        return VitalsLogResponse.builder()
                .id(vitalsLog.getId())
                .operationId(vitalsLog.getScheduledOperation().getId())
                .recordedTime(vitalsLog.getRecordedTime())
                .recordedBy(vitalsLog.getRecordedBy())
                .heartRate(vitalsLog.getHeartRate())
                .systolicBp(vitalsLog.getSystolicBp())
                .diastolicBp(vitalsLog.getDiastolicBp())
                .meanBp(vitalsLog.getMeanBp())
                .respiratoryRate(vitalsLog.getRespiratoryRate())
                .temperature(vitalsLog.getTemperature())
                .oxygenSaturation(vitalsLog.getOxygenSaturation())
                .etco2(vitalsLog.getEtco2())
                .painScale(vitalsLog.getPainScale())
                .consciousness(vitalsLog.getConsciousness())
                .sedationScore(vitalsLog.getSedationScore())
                .additionalNotes(vitalsLog.getAdditionalNotes())
                .createdAt(vitalsLog.getCreatedAt())
                .build();
    }
}
