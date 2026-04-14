package com.ot.service;

import java.util.List;

import com.ot.dto.medication.MedicationUsageRequest;
import com.ot.dto.medication.MedicationUsageResponse;

public interface MedicationUsageService {

    MedicationUsageResponse recordUsage(MedicationUsageRequest request);

    MedicationUsageResponse getById(Long id);

    List<MedicationUsageResponse> getByOperation(Long operationId);

    List<MedicationUsageResponse> getByWardRoom(Long wardRoomId);

    List<MedicationUsageResponse> getByWardBed(Long wardBedId);

	void deleteById(Long id);

	MedicationUsageResponse updateQuantity(Long id, Integer quantity);
}