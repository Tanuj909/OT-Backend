package com.ot.service;

import java.util.List;

import com.ot.dto.wardVitals.WardVitalsRequest;
import com.ot.dto.wardVitals.WardVitalsResponse;

public interface WardVitalsService {
    WardVitalsResponse recordVitals(Long operationId, WardVitalsRequest request);
    List<WardVitalsResponse> getWardVitals(Long operationId);
    WardVitalsResponse getLatestVitals(Long operationId);
    boolean isPatientStable(Long operationId);
}