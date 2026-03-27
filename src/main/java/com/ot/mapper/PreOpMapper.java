package com.ot.mapper;

import com.ot.dto.preOp.PreOpAssessmentResponse;
import com.ot.entity.PreOpAssessment;

public class PreOpMapper {
	
	public static PreOpAssessmentResponse toResponse(PreOpAssessment preOp) {
		return PreOpAssessmentResponse.builder()
	            .id(preOp.getId())
	            .operationId(preOp.getScheduledOperation().getId())
	            .patientId(preOp.getPatientId())
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
	            .createdAt(preOp.getCreatedAt())
	            .updatedAt(preOp.getUpdatedAt())
	            .createdBy(preOp.getCreatedBy())
	            .fitForSurgery(preOp.getFitForSurgery())
                .clearanceRemarks(preOp.getClearanceRemarks())
                .airwayAssessment(preOp.getAirwayAssessment())
                .consentTaken(preOp.getConsentTaken())
                .highRisk(preOp.getHighRisk())
                .checklistCompleted(preOp.getChecklistCompleted())
	            .build();
	}
}
