package com.ot.service;

import java.util.List;

import com.ot.dto.ward.WardRequest;
import com.ot.dto.ward.WardResponse;
import com.ot.dto.ward.WardUpdateRequest;
import com.ot.enums.WardType;

public interface WardService {
    WardResponse createWard(WardRequest request);
    WardResponse getWardById(Long wardId);
    List<WardResponse> getAllWards(WardType wardType, Boolean isActive);
    WardResponse updateWard(Long wardId, WardUpdateRequest request);
    void deactivateWard(Long wardId);
    void activateWard(Long wardId);
}