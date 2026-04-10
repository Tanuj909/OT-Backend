package com.ot.service;

import java.util.List;

import com.ot.dto.ward.WardBedRequest;
import com.ot.dto.ward.WardBedResponse;
import com.ot.dto.ward.WardBedUpdateRequest;
import com.ot.enums.BedStatus;

public interface WardBedService {

    WardBedResponse createBed(WardBedRequest request);

    WardBedResponse getBedById(Long bedId);

    List<WardBedResponse> getBedsByRoom(Long roomId, Boolean isActive, BedStatus status);

    WardBedResponse updateBed(Long bedId, WardBedUpdateRequest request);

    WardBedResponse markMaintenance(Long bedId);

    WardBedResponse markAvailable(Long bedId);

    void deactivateBed(Long bedId);

    void activateBed(Long bedId);
}