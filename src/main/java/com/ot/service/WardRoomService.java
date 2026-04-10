package com.ot.service;

import java.util.List;

import com.ot.dto.ward.WardRoomRequest;
import com.ot.dto.ward.WardRoomResponse;
import com.ot.dto.ward.WardRoomUpdateRequest;

public interface WardRoomService {
    WardRoomResponse createWardRoom(WardRoomRequest request);
    WardRoomResponse getWardRoomById(Long roomId);
    List<WardRoomResponse> getWardRoomsByWardId(Long wardId, Boolean isActive);
    List<WardRoomResponse> getAvailableRooms(Long wardId);
    WardRoomResponse updateWardRoom(Long roomId, WardRoomUpdateRequest request);
    void deactivateWardRoom(Long roomId);
    void activateWardRoom(Long roomId);
}