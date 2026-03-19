package com.ot.service.impl;

@Service
@RequiredArgsConstructor
public class WardVitalsServiceImpl implements WardVitalsService {

    private final VitalsLogRepository vitalsLogRepository;
    private final ScheduledOperationRepository operationRepository;

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No user is logged in");
        }
        return ((CustomUserDetails) auth.getPrincipal()).getUser();
    }

    @Transactional
    @Override
    public WardVitalsResponse recordVitals(Long operationId, WardVitalsRequest request) {

        User currentUser = currentUser();

        // 1. Operation fetch
        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        // 2. Hospital check
        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        // 3. Operation COMPLETED check — sirf completed operations ka ward vitals
        if (!operation.getStatus().equals(OperationStatus.COMPLETED)) {
            throw new ValidationException("Ward vitals can only be recorded for COMPLETED operations");
        }

        // 4. PostOp exists check
        PostOpRecord postOp = operation.getPostOp();
        if (postOp == null) {
            throw new ResourceNotFoundException("PostOp record not found");
        }

        // 5. Patient TRANSFERRED check — ward mein hona chahiye
        if (!postOp.getStatus().equals(RecoveryStatus.TRANSFERRED)) {
            throw new ValidationException("Patient is not yet transferred to ward");
        }

        // 6. Ward fetch from PostOp
        Ward ward = postOp.getWard();
        if (ward == null) {
            throw new ResourceNotFoundException("Ward not found for this patient");
        }

        // 7. Build and save
        VitalsLog vitals = VitalsLog.builder()
                .hospital(operation.getHospital())
                .scheduledOperation(operation)
                .ward(ward)
                .phase(VitalsPhase.POST_OP)
                .recordedTime(LocalDateTime.now())
                .recordedBy(currentUser.getUserName())
                .heartRate(request.getHeartRate())
                .systolicBp(request.getSystolicBp())
                .diastolicBp(request.getDiastolicBp())
                .meanBp(request.getMeanBp())
                .respiratoryRate(request.getRespiratoryRate())
                .temperature(request.getTemperature())
                .oxygenSaturation(request.getOxygenSaturation())
                .etco2(request.getEtco2())
                .painScale(request.getPainScale())
                .consciousness(request.getConsciousness())
                .sedationScore(request.getSedationScore())
                .isStable(request.getIsStable())
                .additionalNotes(request.getAdditionalNotes())
                .build();

        vitalsLogRepository.save(vitals);

        return mapToResponse(vitals);
    }

    @Override
    public List<WardVitalsResponse> getWardVitals(Long operationId) {

        User currentUser = currentUser();

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        return vitalsLogRepository
                .findByScheduledOperationAndPhase(operation, VitalsPhase.POST_OP)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public WardVitalsResponse getLatestVitals(Long operationId) {

        User currentUser = currentUser();

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        VitalsLog latest = vitalsLogRepository
                .findTopByScheduledOperationAndPhaseAndIsStableOrderByRecordedTimeDesc(
                        operation, VitalsPhase.POST_OP, true)
                .orElseThrow(() -> new ResourceNotFoundException("No stable vitals found"));

        return mapToResponse(latest);
    }

    @Override
    public boolean isPatientStable(Long operationId) {

        User currentUser = currentUser();

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        // Latest POST_OP vitals mein isStable check karo
        return vitalsLogRepository
                .findTopByScheduledOperationAndPhaseAndIsStableOrderByRecordedTimeDesc(
                        operation, VitalsPhase.POST_OP, true)
                .isPresent();
    }

    // Mapper
    private WardVitalsResponse mapToResponse(VitalsLog vitals) {
        return WardVitalsResponse.builder()
                .id(vitals.getId())
                .operationId(vitals.getScheduledOperation().getId())
                .wardId(vitals.getWard().getId())
                .wardName(vitals.getWard().getWardName())
                .phase(vitals.getPhase())
                .recordedTime(vitals.getRecordedTime())
                .recordedBy(vitals.getRecordedBy())
                .heartRate(vitals.getHeartRate())
                .systolicBp(vitals.getSystolicBp())
                .diastolicBp(vitals.getDiastolicBp())
                .meanBp(vitals.getMeanBp())
                .respiratoryRate(vitals.getRespiratoryRate())
                .temperature(vitals.getTemperature())
                .oxygenSaturation(vitals.getOxygenSaturation())
                .etco2(vitals.getEtco2())
                .painScale(vitals.getPainScale())
                .consciousness(vitals.getConsciousness())
                .sedationScore(vitals.getSedationScore())
                .isStable(vitals.getIsStable())
                .additionalNotes(vitals.getAdditionalNotes())
                .createdAt(vitals.getCreatedAt())
                .build();
    }
}