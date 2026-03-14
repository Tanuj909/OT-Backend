package com.ot.service;

import java.util.List;

import com.ot.dto.vitalsLog.VitalsLogBulkRequest;
import com.ot.dto.vitalsLog.VitalsLogRequest;
import com.ot.dto.vitalsLog.VitalsLogResponse;

public interface VitalsLogService {

	VitalsLogResponse addVitals(Long operationId, VitalsLogRequest request);

	List<VitalsLogResponse> getVitals(Long operationId);

	VitalsLogResponse getLatestVitals(Long operationId);

	void deleteVitals(Long operationId, Long vitalsId);

	List<VitalsLogResponse> addBulkVitals(Long operationId, VitalsLogBulkRequest bulkRequest);

}
