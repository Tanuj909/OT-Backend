package com.ot.service;

import com.ot.dto.ward.CreateDoctorVisitRequest;
import com.ot.dto.ward.DoctorVisitResponse;
import com.ot.dto.ward.UpdateDoctorVisitRequest;
import com.ot.enums.DoctorVisitStatus;

import java.util.List;

public interface DoctorVisitService {

    // Create
    DoctorVisitResponse createVisit(CreateDoctorVisitRequest request);

    // Update (notes/medication/discharge etc. update karna ho)
    DoctorVisitResponse updateVisit(Long visitId, UpdateDoctorVisitRequest request);

    // Cancel scheduled visit
    DoctorVisitResponse cancelVisit(Long visitId);

    // Get single
    DoctorVisitResponse getById(Long visitId);

    // Get all visits for an operation (latest first)
    List<DoctorVisitResponse> getByOperation(Long operationId);

    // Get all visits for a ward admission (latest first)
    List<DoctorVisitResponse> getByAdmission(Long wardAdmissionId);

    // Get by status (e.g. SCHEDULED future visits)
    List<DoctorVisitResponse> getByOperationAndStatus(Long operationId, DoctorVisitStatus status);

    // Latest visit for an operation
    DoctorVisitResponse getLatestVisit(Long operationId);

    // Discharge recommended kisi visit mein? (useful for frontend flag)
    boolean isDischargeRecommended(Long operationId);
}