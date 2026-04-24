package com.ot.service.impl;

import com.ot.billing.service.OTBillingIntegrationService;
import com.ot.dto.billing.OTDoctorVisitBillingRequest;
import com.ot.dto.billing.OTDoctorVisitBillingResponse;
import com.ot.dto.ward.CreateDoctorVisitRequest;
import com.ot.dto.ward.DoctorVisitResponse;
import com.ot.dto.ward.UpdateDoctorVisitRequest;
import com.ot.entity.DoctorVisit;
import com.ot.entity.ScheduledOperation;
import com.ot.entity.StaffFee;
import com.ot.entity.User;
import com.ot.entity.WardAdmission;
import com.ot.enums.DoctorVisitStatus;
import com.ot.enums.RoleType;
import com.ot.exception.BillingException;
import com.ot.exception.DataConflictException;
import com.ot.exception.OperationNotAllowedException;
import com.ot.exception.ResourceNotFoundException;
import com.ot.exception.UnauthorizedException;
import com.ot.exception.ValidationException;
import com.ot.repository.DoctorVisitRepository;
import com.ot.repository.ScheduledOperationRepository;
import com.ot.repository.StaffFeeRepository;
import com.ot.repository.UserRepository;
import com.ot.repository.WardAdmissionRepository;
import com.ot.security.CustomUserDetails;
import com.ot.service.DoctorVisitService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorVisitServiceImpl implements DoctorVisitService {

    private final DoctorVisitRepository doctorVisitRepository;
    private final ScheduledOperationRepository scheduledOperationRepository;
    private final WardAdmissionRepository wardAdmissionRepository;
    private final UserRepository userRepository;
    private final StaffFeeRepository feeRepository;
    private final OTBillingIntegrationService billingIntegrationService;

    // ── Auth Helper ────────────────────────────────────────────────────────

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No user is logged in");
        }
        return ((CustomUserDetails) auth.getPrincipal()).getUser();
    }

    // ── Create ─────────────────────────────────────────────────────────────

    @Transactional
    @Override
    public DoctorVisitResponse createVisit(CreateDoctorVisitRequest request) {

        User currentUser = currentUser();

        // Role check — Doctor, Nurse, Admin, HospitalAdmin create kar sakte hain
        if (!Set.of(
                RoleType.ADMIN,
                RoleType.HOSPITAL_ADMIN,
                RoleType.DOCTOR,
                RoleType.NURSE
        ).contains(currentUser.getRole())) {
            throw new ValidationException("You are not authorized to create doctor visits");
        }

        // ── Operation fetch & validate ──────────────────────────────────────
        ScheduledOperation operation = scheduledOperationRepository
                .findById(request.getOperationId())
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        // ── Active WardAdmission fetch ──────────────────────────────────────
        // Patient abhi admit hona chahiye — discharged patient ka visit nahi
        WardAdmission admission = wardAdmissionRepository
                .findByOperationIdAndDischargedWhenIsNull(operation.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active ward admission found for this operation. " +
                        "Patient must be admitted in ward before recording a doctor visit."));
        
        
		Long doctorId;

		// 👉 Case 1: DoctorId request se aaya hai
		if (request.getDoctorId() != null) {
			doctorId = request.getDoctorId();
		}
		// 👉 Case 2: DoctorId nahi aaya → current user doctor hona chahiye
		else {
			if (!currentUser.getRole().equals(RoleType.DOCTOR)) {
				throw new ValidationException("Doctor ID is required if logged-in user is not a doctor");
			}
			doctorId = currentUser.getId();
		}
     
        // Check if Doctor Exists or not And it's Role is Doctor
        User doctor = userRepository.findById(doctorId)
        		.orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
        
        // Check if User is Doctor
        if(!doctor.getRole().equals(RoleType.DOCTOR)) {
        	throw new DataConflictException("User is Not Doctor");
        }

        // ── Build entity ────────────────────────────────────────────────────
        DoctorVisit visit = DoctorVisit.builder()
                .scheduledOperation(operation)
                .wardAdmission(admission)
                .hospital(currentUser.getHospital())
                // Patient snapshot from admission
                .patientId(admission.getPatientId())
                .patientName(admission.getPatientName())
                .patientMrn(admission.getPatientMrn())
                // Visit info
                .visitTime(request.getVisitTime() != null
                        ? request.getVisitTime()
                        : LocalDateTime.now())
                // Doctor info
                .doctorId(doctorId)
                .doctorName(doctor.getUserName())
                .doctorSpecialization(request.getDoctorSpecialization())
                // Recorded by — logged in user (could be nurse recording on behalf)
                .recordedById(currentUser.getId())
                .recordedByName(currentUser.getUserName())
                // Clinical
                .clinicalObservations(request.getClinicalObservations())
                .diagnosis(request.getDiagnosis())
                .treatmentPlan(request.getTreatmentPlan())
                // Medications
                .hasMedicationChange(Boolean.TRUE.equals(request.getHasMedicationChange()))
                .medicationsAdded(request.getMedicationsAdded())
                .medicationsDiscontinued(request.getMedicationsDiscontinued())
                .medicationNotes(request.getMedicationNotes())
                // Next visit
                .nextVisitScheduled(request.getNextVisitScheduled())
                .nextVisitInstructions(request.getNextVisitInstructions())
                // Discharge
                .dischargeRecommended(Boolean.TRUE.equals(request.getDischargeRecommended()))
                .dischargeNotes(request.getDischargeNotes())
                .expectedDischargeDate(request.getExpectedDischargeDate())
                // Status — default COMPLETED agar null
                .status(request.getStatus() != null
                        ? request.getStatus()
                        : DoctorVisitStatus.COMPLETED)
                .build();

        doctorVisitRepository.save(visit);
        
     // ================= BILLING CALL (ONLY IF COMPLETED) =================
        if (visit.getStatus() == DoctorVisitStatus.COMPLETED) {
        	try {
        	    OTDoctorVisitBillingResponse billingResponse =
        	            billingIntegrationService.addDoctorVisit(
        	                    mapToBillingRequest(visit)
        	            );

        	    visit.setDoctorVisitBillingId(billingResponse.getId());
        	    doctorVisitRepository.save(visit);

        	} catch (Exception ex) {
        	    throw new RuntimeException("Billing failed, visit cannot be completed",ex);
        	}
        }
        return mapToResponse(visit);
    }

    
    @Transactional
    @Override
    public DoctorVisitResponse completeVisit(Long visitId) {

        User currentUser = currentUser();

        DoctorVisit visit = fetchAndValidateHospital(visitId, currentUser);

        // ❌ Already completed
        if (visit.getStatus() == DoctorVisitStatus.COMPLETED) {
            throw new OperationNotAllowedException("Visit is already completed");
        }

        // ❌ Cancelled visit complete nahi ho sakti
        if (visit.getStatus() == DoctorVisitStatus.CANCELLED) {
            throw new OperationNotAllowedException("Cancelled visit cannot be completed");
        }

        // ❌ Only SCHEDULED allowed
        if (visit.getStatus() != DoctorVisitStatus.SCHEDULED) {
            throw new OperationNotAllowedException("Only scheduled visits can be completed");
        }

        // ✅ Mark as completed
        visit.setStatus(DoctorVisitStatus.COMPLETED);

        doctorVisitRepository.save(visit);

        // ================= BILLING CALL =================
        try {
            OTDoctorVisitBillingResponse billingResponse =
                    billingIntegrationService.addDoctorVisit(
                            mapToBillingRequest(visit)
                    );

            visit.setDoctorVisitBillingId(billingResponse.getId());
            doctorVisitRepository.save(visit);

        } catch (Exception ex) {
            throw new RuntimeException("Billing failed, visit cannot be completed", ex);
        }

        return mapToResponse(visit);
    }
    
    // ── Update ─────────────────────────────────────────────────────────────

    @Transactional
    @Override
    public DoctorVisitResponse updateVisit(Long visitId, UpdateDoctorVisitRequest request) {

        User currentUser = currentUser();

        DoctorVisit visit = fetchAndValidateHospital(visitId, currentUser);

        // Cancelled visit update nahi ho sakti
        if (visit.getStatus() == DoctorVisitStatus.CANCELLED) {
            throw new OperationNotAllowedException("Cannot update a cancelled visit");
        }

        // Partial update — sirf non-null fields
        if (request.getVisitTime()             != null) visit.setVisitTime(request.getVisitTime());
        if (request.getDoctorSpecialization()  != null) visit.setDoctorSpecialization(request.getDoctorSpecialization());
        if (request.getClinicalObservations()  != null) visit.setClinicalObservations(request.getClinicalObservations());
        if (request.getDiagnosis()             != null) visit.setDiagnosis(request.getDiagnosis());
        if (request.getTreatmentPlan()         != null) visit.setTreatmentPlan(request.getTreatmentPlan());
        if (request.getHasMedicationChange()   != null) visit.setHasMedicationChange(request.getHasMedicationChange());
        if (request.getMedicationsAdded()      != null) visit.setMedicationsAdded(request.getMedicationsAdded());
        if (request.getMedicationsDiscontinued() != null) visit.setMedicationsDiscontinued(request.getMedicationsDiscontinued());
        if (request.getMedicationNotes()       != null) visit.setMedicationNotes(request.getMedicationNotes());
        if (request.getNextVisitScheduled()    != null) visit.setNextVisitScheduled(request.getNextVisitScheduled());
        if (request.getNextVisitInstructions() != null) visit.setNextVisitInstructions(request.getNextVisitInstructions());
        if (request.getDischargeRecommended()  != null) visit.setDischargeRecommended(request.getDischargeRecommended());
        if (request.getDischargeNotes()        != null) visit.setDischargeNotes(request.getDischargeNotes());
        if (request.getExpectedDischargeDate() != null) visit.setExpectedDischargeDate(request.getExpectedDischargeDate());
        if (request.getStatus()                != null) visit.setStatus(request.getStatus());

        doctorVisitRepository.save(visit);

        return mapToResponse(visit);
    }

    // ── Cancel ─────────────────────────────────────────────────────────────

    @Transactional
    @Override
    public DoctorVisitResponse cancelVisit(Long visitId) {

        User currentUser = currentUser();

        DoctorVisit visit = fetchAndValidateHospital(visitId, currentUser);

        if (visit.getStatus() == DoctorVisitStatus.COMPLETED) {
            throw new OperationNotAllowedException("Cannot cancel a completed visit");
        }

        if (visit.getStatus() == DoctorVisitStatus.CANCELLED) {
            throw new OperationNotAllowedException("Visit is already cancelled");
        }

        visit.setStatus(DoctorVisitStatus.CANCELLED);
        doctorVisitRepository.save(visit);

        return mapToResponse(visit);
    }

    // ── Get ────────────────────────────────────────────────────────────────

    @Override
    public DoctorVisitResponse getById(Long visitId) {
        User currentUser = currentUser();
        return mapToResponse(fetchAndValidateHospital(visitId, currentUser));
    }

    @Override
    public List<DoctorVisitResponse> getByOperation(Long operationId) {

        User currentUser = currentUser();

        return doctorVisitRepository
                .findByScheduledOperationIdOrderByVisitTimeDesc(operationId)
                .stream()
                .filter(v -> v.getHospital().getId().equals(currentUser.getHospital().getId()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DoctorVisitResponse> getByAdmission(Long wardAdmissionId) {

        User currentUser = currentUser();

        return doctorVisitRepository
                .findByWardAdmissionIdOrderByVisitTimeDesc(wardAdmissionId)
                .stream()
                .filter(v -> v.getHospital().getId().equals(currentUser.getHospital().getId()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DoctorVisitResponse> getByOperationAndStatus(Long operationId, DoctorVisitStatus status) {

        User currentUser = currentUser();

        return doctorVisitRepository
                .findByScheduledOperationIdAndStatusOrderByVisitTimeAsc(operationId, status)
                .stream()
                .filter(v -> v.getHospital().getId().equals(currentUser.getHospital().getId()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DoctorVisitResponse getLatestVisit(Long operationId) {

        User currentUser = currentUser();

        DoctorVisit visit = doctorVisitRepository
                .findTopByScheduledOperationIdOrderByVisitTimeDesc(operationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No doctor visit found for this operation"));

        if (!visit.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this visit");
        }

        return mapToResponse(visit);
    }

    @Override
    public boolean isDischargeRecommended(Long operationId) {
        return doctorVisitRepository
                .existsByScheduledOperationIdAndDischargeRecommendedTrue(operationId);
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private DoctorVisit fetchAndValidateHospital(Long visitId, User currentUser) {
        DoctorVisit visit = doctorVisitRepository.findById(visitId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor visit not found"));

        if (!visit.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this visit");
        }
        return visit;
    }

    // ── Mapper ─────────────────────────────────────────────────────────────

    private DoctorVisitResponse mapToResponse(DoctorVisit v) {
        return DoctorVisitResponse.builder()
                .id(v.getId())
                .operationId(v.getScheduledOperation().getId())
                .wardAdmissionId(v.getWardAdmission().getId())
                .patientId(v.getPatientId())
                .patientName(v.getPatientName())
                .patientMrn(v.getPatientMrn())
                .visitTime(v.getVisitTime())
                .doctorId(v.getDoctorId())
                .doctorName(v.getDoctorName())
                .doctorSpecialization(v.getDoctorSpecialization())
                .recordedById(v.getRecordedById())
                .recordedByName(v.getRecordedByName())
                .clinicalObservations(v.getClinicalObservations())
                .diagnosis(v.getDiagnosis())
                .treatmentPlan(v.getTreatmentPlan())
                .hasMedicationChange(v.getHasMedicationChange())
                .medicationsAdded(v.getMedicationsAdded())
                .medicationsDiscontinued(v.getMedicationsDiscontinued())
                .medicationNotes(v.getMedicationNotes())
                .nextVisitScheduled(v.getNextVisitScheduled())
                .nextVisitInstructions(v.getNextVisitInstructions())
                .dischargeRecommended(v.getDischargeRecommended())
                .dischargeNotes(v.getDischargeNotes())
                .expectedDischargeDate(v.getExpectedDischargeDate())
                .status(v.getStatus())
                .createdAt(v.getCreatedAt())
                .updatedAt(v.getUpdatedAt())
                .build();
    }
    
//    Billing Mapper
    private OTDoctorVisitBillingRequest mapToBillingRequest(DoctorVisit visit) {

        OTDoctorVisitBillingRequest request = new OTDoctorVisitBillingRequest();

        request.setOperationExternalId(visit.getScheduledOperation().getId());
        request.setDoctorExternalId(visit.getDoctorId());
        request.setDoctorName(visit.getDoctorName());
        request.setVisitTime(visit.getVisitTime());

        // optional but recommended (future idempotency)
//        request.setVisitId(visit.getId());
        
        // 🔥 Fetch Doctor Fee from StaffFee table
        Double fees = getDoctorFee(visit.getDoctorId());

        request.setFees(fees); // static for now

        return request;
    }
    
    //Doctor Fees Resolver:
    private Double getDoctorFee(Long doctorId) {

        return feeRepository.findByStaff_Id(doctorId)
                .map(StaffFee::getVisitFee)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Staff fee not configured for doctorId: " + doctorId
                        ));
    }
    
}