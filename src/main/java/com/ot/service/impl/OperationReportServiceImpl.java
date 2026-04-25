package com.ot.service.impl;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ot.dto.opertaionReport.OperationReportResponse;
import com.ot.embed.StaffAssignment;
import com.ot.embed.SurgeonAssignment;
import com.ot.entity.DoctorVisit;
import com.ot.entity.IntraOpRecord;
import com.ot.entity.PostOpRecord;
import com.ot.entity.PreOpAssessment;
import com.ot.entity.ScheduledOperation;
import com.ot.entity.User;
import com.ot.entity.WardAdmission;
import com.ot.entity.WardRoom;
import com.ot.enums.StaffRole;
import com.ot.enums.VitalsPhase;
import com.ot.enums.VolumeUnit;
import com.ot.exception.ResourceNotFoundException;
import com.ot.exception.UnauthorizedException;
import com.ot.repository.ConsumableUsageRepository;
import com.ot.repository.DoctorVisitRepository;
import com.ot.repository.ImplantUsedRepository;
import com.ot.repository.ScheduledOperationRepository;
import com.ot.repository.UsedEquipmentRepository;
import com.ot.repository.VitalsLogRepository;
import com.ot.repository.WardAdmissionRepository;
import com.ot.security.CustomUserDetails;
import com.ot.service.OperationReportService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OperationReportServiceImpl implements OperationReportService {
	
	private final ScheduledOperationRepository operationRepository;
	private final VitalsLogRepository  vitalsLogRepository;
	private final UsedEquipmentRepository usedEquipmentRepository;
	private final ConsumableUsageRepository consumableRepository;
	private final ImplantUsedRepository implantUsedRepository;
	private final DoctorVisitRepository doctorVisitRepository;
	private final WardAdmissionRepository wardAdmissionRepository;

	
    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No user is logged in");
        }
        return ((CustomUserDetails) auth.getPrincipal()).getUser();
    }
	
	
	@Override
	public OperationReportResponse getOperationReport(Long operationId) {

	    User currentUser = currentUser();

	    // 1. Operation fetch
	    ScheduledOperation operation = operationRepository.findById(operationId)
	            .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

	    // 2. Hospital check
	    if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
	        throw new UnauthorizedException("You are not authorized to access this operation");
	    }

	    // 3. Surgery duration
	    Long surgeryDuration = null;
	    if (operation.getActualStartTime() != null && operation.getActualEndTime() != null) {
	        surgeryDuration = ChronoUnit.MINUTES.between(
	                operation.getActualStartTime(), operation.getActualEndTime());
	    }

	    // 4. Team
	    String primarySurgeon = operation.getSupportingSurgeons().stream()
	            .filter(SurgeonAssignment::isPrimary)
	            .map(SurgeonAssignment::getSurgeonName)
	            .findFirst().orElse(null);

	    String anesthesiologist = operation.getSupportingStaff().stream()
	            .filter(s -> s.getRole().equals(StaffRole.ANESTHESIOLOGIST))
	            .map(StaffAssignment::getStaffName)
	            .findFirst().orElse(null);

	    List<OperationReportResponse.SurgeonTeamMember> supportingSurgeons =
	            operation.getSupportingSurgeons().stream()
	                    .map(s -> OperationReportResponse.SurgeonTeamMember.builder()
	                            .surgeonId(s.getSurgeonId())
	                            .surgeonName(s.getSurgeonName())
	                            .role(s.getRole())
	                            .isPrimary(s.isPrimary())
	                            .build())
	                    .collect(Collectors.toList());

	    List<OperationReportResponse.StaffTeamMember> supportingStaff =
	            operation.getSupportingStaff().stream()
	                    .map(s -> OperationReportResponse.StaffTeamMember.builder()
	                            .staffId(s.getStaffId())
	                            .staffName(s.getStaffName())
	                            .role(s.getRole())
	                            .build())
	                    .collect(Collectors.toList());

	    // 5. PreOp
	    OperationReportResponse.PreOpSummary preOpSummary = null;
	    if (operation.getPreOp() != null) {
	        PreOpAssessment preOp = operation.getPreOp();
	        preOpSummary = OperationReportResponse.PreOpSummary.builder()
	                .assessmentDate(preOp.getAssessmentDate())
	                .assessedBy(preOp.getAssessedBy())
	                .height(preOp.getHeight())
	                .weight(preOp.getWeight())
	                .bmi(preOp.getBmi())
	                .bloodGroup(preOp.getBloodGroup())
	                .allergies(preOp.getAllergies())
	                .currentMedications(preOp.getCurrentMedications())
	                .pastMedicalHistory(preOp.getPastMedicalHistory())
	                .pastSurgicalHistory(preOp.getPastSurgicalHistory())
	                .physicalExamination(preOp.getPhysicalExamination())
	                .ecgFindings(preOp.getEcgFindings())
	                .labResults(preOp.getLabResults())
	                .radiologyFindings(preOp.getRadiologyFindings())
	                .asaGrade(preOp.getAsaGrade())
	                .npoStatus(preOp.getNpoStatus())
	                .anesthesiaPlan(preOp.getAnesthesiaPlan())
	                .specialInstructions(preOp.getSpecialInstructions())
	                .status(preOp.getStatus())
	                .build();
	    }

	    // 6. IntraOp
	    OperationReportResponse.IntraOpSummary intraOpSummary = null;
	    if (operation.getIntraOp() != null) {
	        IntraOpRecord intraOp = operation.getIntraOp();

	        // IV Fluids
	        List<OperationReportResponse.IVFluidSummary> ivFluids = intraOp.getIvFluids().stream()
	                .map(f -> OperationReportResponse.IVFluidSummary.builder()
	                        .fluidType(f.getFluidType())
	                        .volume(f.getVolume())
	                        .unit(f.getUnit())
	                        .startTime(f.getStartTime())
	                        .endTime(f.getEndTime())
	                        .administeredBy(f.getAdministeredBy())
	                        .build())
	                .collect(Collectors.toList());

	        // Total IV Fluids ML
	        Integer totalIVFluidsML = intraOp.getIvFluids().stream()
	                .mapToInt(f -> f.getUnit().equals(VolumeUnit.LITERS)
	                        ? f.getVolume() * 1000
	                        : f.getVolume())
	                .sum();

	        // Anesthesia Drugs
	        List<OperationReportResponse.AnesthesiaDrugSummary> drugs = intraOp.getAnesthesiaDrugs().stream()
	                .map(d -> OperationReportResponse.AnesthesiaDrugSummary.builder()
	                        .drugName(d.getDrugName())
	                        .dose(d.getDose())
	                        .doseUnit(d.getDoseUnit())
	                        .route(d.getRoute())
	                        .drugType(d.getDrugType())
	                        .administeredAt(d.getAdministeredAt())
	                        .endTime(d.getEndTime())
	                        .administeredBy(d.getAdministeredBy())
	                        .build())
	                .collect(Collectors.toList());

	        // Vitals
	        List<OperationReportResponse.VitalsSummary> vitals = vitalsLogRepository
	                .findByScheduledOperationAndPhase(operation, VitalsPhase.INTRA_OP)
	                .stream()
	                .map(v -> OperationReportResponse.VitalsSummary.builder()
	                        .recordedTime(v.getRecordedTime())
	                        .recordedBy(v.getRecordedBy())
	                        .heartRate(v.getHeartRate())
	                        .systolicBp(v.getSystolicBp())
	                        .diastolicBp(v.getDiastolicBp())
	                        .respiratoryRate(v.getRespiratoryRate())
	                        .temperature(v.getTemperature())
	                        .oxygenSaturation(v.getOxygenSaturation())
	                        .painScale(v.getPainScale())
	                        .phase(v.getPhase())
	                        .build())
	                .collect(Collectors.toList());

	        // Equipment
	        List<OperationReportResponse.EquipmentSummary> equipment = usedEquipmentRepository
	                .findByScheduledOperation(operation)
	                .stream()
	                .map(e -> OperationReportResponse.EquipmentSummary.builder()
	                        .equipmentName(e.getEquipment().getName())
	                        .equipmentCode(e.getEquipment().getAssetCode())
	                        .startTime(e.getUsedFrom())
	                        .endTime(e.getUsedUntil())
	                        .usedBy(null)
	                        .build())
	                .collect(Collectors.toList());

	        // Consumables
	        List<OperationReportResponse.ConsumableSummary> consumables = consumableRepository
	                .findByScheduledOperation(operation)
	                .stream()
	                .map(c -> OperationReportResponse.ConsumableSummary.builder()
	                        .itemName(c.getConsumableName())
	                        .category(c.getCategory())
	                        .consumableCode(c.getConsumableCode())
	                        .quantity(c.getQuantityUsed())
	                        .quantityWasted(c.getQuantityWasted())
	                        .unit(c.getUnitOfMeasure())
	                        .batchNumber(c.getBatchNumber())
	                        .usedBy(c.getIssuedBy())
	                        .build())
	                .collect(Collectors.toList());

	        // Implants
	        List<OperationReportResponse.ImplantSummary> implants = implantUsedRepository
	                .findByScheduledOperation(operation)
	                .stream()
	                .map(i -> OperationReportResponse.ImplantSummary.builder()
	                        .itemName(i.getCatalogItem().getItemName())
	                        .manufacturer(i.getCatalogItem().getManufacturer())
	                        .serialNumber(i.getSerialNumber())
	                        .batchNumber(i.getBatchNumber())
	                        .quantity(i.getQuantity())
	                        .bodyLocation(i.getBodyLocation())
	                        .usedBy(i.getUsedBy())
	                        .build())
	                .collect(Collectors.toList());

	        intraOpSummary = OperationReportResponse.IntraOpSummary.builder()
	                .procedurePerformed(intraOp.getProcedurePerformed())
	                .incisionType(intraOp.getIncisionType())
	                .woundClosure(intraOp.getWoundClosure())
	                .bloodLoss(intraOp.getBloodLoss())
	                .bloodLossUnit(intraOp.getBloodLossUnit())
	                .urineOutput(intraOp.getUrineOutput())
	                .drainOutput(intraOp.getDrainOutput())
	                .intraOpFindings(intraOp.getIntraOpFindings())
	                .specimensCollected(intraOp.getSpecimensCollected())
	                .complications(intraOp.getComplications())
	                .interventions(intraOp.getInterventions())
	                .status(intraOp.getStatus())
	                .ivFluids(ivFluids)
	                .totalIVFluidsML(totalIVFluidsML)
	                .anesthesiaDrugs(drugs)
	                .vitals(vitals)
	                .equipment(equipment)
	                .consumables(consumables)
	                .implants(implants)
	                .build();
	    }

	    // 7. PostOp
	    OperationReportResponse.PostOpSummary postOpSummary = null;
	    if (operation.getPostOp() != null) {
	    	
	    	WardAdmission admission = wardAdmissionRepository
	    	        .findByOperation(operation)
	    	        .orElse(null);
	    	
	    	OperationReportResponse.RecoveryRoomDetails recoveryRoomDetails = null;

	    	if (admission != null && admission.getWardRoom() != null) {

	    	    WardRoom room = admission.getWardRoom();

	    	    recoveryRoomDetails = OperationReportResponse.RecoveryRoomDetails.builder()
	    	            .roomNumber(room.getRoomNumber())
	    	            .roomName(room.getRoomName())
	    	            .roomType(room.getRoomType() != null ? room.getRoomType().name() : null)
	    	            .totalBeds(room.getTotalBeds())
	    	            .availableBeds(room.getAvailableBeds())
	    	            .ratePerHour(room.getRatePerHour())
	    	            .gstPercent(room.getGstPercent())
	    	            .build();
	    	}
	    	
	    	List<DoctorVisit> visits = doctorVisitRepository
	    	        .findByScheduledOperation(operation);

	    	List<OperationReportResponse.DoctorVisitSummary> visitSummaryList =
	    	        visits.stream()
	    	        .map(v -> OperationReportResponse.DoctorVisitSummary.builder()
	    	                .visitId(v.getId())
	    	                .visitTime(v.getVisitTime())
	    	                .doctorName(v.getDoctorName())
	    	                .specialization(v.getDoctorSpecialization())
	    	                .clinicalObservations(v.getClinicalObservations())
	    	                .diagnosis(v.getDiagnosis())
	    	                .treatmentPlan(v.getTreatmentPlan())
	    	                .dischargeRecommended(v.getDischargeRecommended())
	    	                .nextVisit(v.getNextVisitScheduled())
	    	                .status(v.getStatus().name())
	    	                .build())
	    	        .collect(Collectors.toList());
	        PostOpRecord postOp = operation.getPostOp();
	        postOpSummary = OperationReportResponse.PostOpSummary.builder()
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
	                // ✅ NEW
	                .recoveryRoom(recoveryRoomDetails)
	                .doctorVisits(visitSummaryList)
	                .build();
	    }

	    // 8. Build final report
	    return OperationReportResponse.builder()
	            .patientId(operation.getPatientId())
	            .patientName(operation.getPatientName())
	            .patientMrn(operation.getPatientMrn())
	            .ipdAdmissionId(operation.getIpdAdmissionId())
	            .operationId(operation.getId())
	            .operationReference(operation.getOperationReference())
	            .procedureName(operation.getProcedureName())
	            .procedureCode(operation.getProcedureCode())
	            .complexity(operation.getComplexity())
	            .operationStatus(operation.getStatus())
	            .scheduledStartTime(operation.getScheduledStartTime())
	            .scheduledEndTime(operation.getScheduledEndTime())
	            .actualStartTime(operation.getActualStartTime())
	            .actualEndTime(operation.getActualEndTime())
	            .surgeryDurationMinutes(surgeryDuration)
	            .roomNumber(operation.getRoom() != null ? operation.getRoom().getRoomNumber() : null)
	            .roomName(operation.getRoom() != null ? operation.getRoom().getRoomName() : null)
	            .primarySurgeon(primarySurgeon)
	            .anesthesiologist(anesthesiologist)
	            .supportingSurgeons(supportingSurgeons)
	            .supportingStaff(supportingStaff)
	            .preOp(preOpSummary)
	            .intraOp(intraOpSummary)
	            .postOp(postOpSummary)
	            .build();
	}

}
