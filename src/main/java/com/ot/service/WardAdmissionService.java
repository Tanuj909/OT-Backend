package com.ot.service;

import java.util.List;

import com.ot.dto.ward.AssignWardRequest;
import com.ot.dto.ward.WardAdmissionResponse;

public interface WardAdmissionService {

    WardAdmissionResponse assignWard(AssignWardRequest request);

    WardAdmissionResponse discharge(Long operationId);

    WardAdmissionResponse getActiveByOperation(Long operationId);

    List<WardAdmissionResponse> getByPatient(String patientId);

    List<WardAdmissionResponse> getByRoom(Long wardRoomId);

    List<WardAdmissionResponse> getByBed(Long wardBedId);

	boolean isPatientAdmitted(String patientId);

	boolean isOperationAdmitted(Long operationId);
}