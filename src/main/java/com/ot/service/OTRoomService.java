package com.ot.service;

import java.util.List;
import com.ot.dto.otRoom.FeatureMappingRequest;
import com.ot.dto.otRoom.OTRoomCreateRequest;
import com.ot.dto.otRoom.OTRoomFeatureResponse;
import com.ot.dto.otRoom.OTRoomResponse;
import com.ot.dto.otRoom.UpdateRoomStatusRequest;


public interface OTRoomService {

    OTRoomResponse create(OTRoomCreateRequest request);

    List<OTRoomResponse> getAll();

    List<OTRoomResponse> getByTheater(Long theaterId);

    OTRoomResponse getById(Long id);

    OTRoomResponse update(Long id, OTRoomCreateRequest request);

    void delete(Long id);

	OTRoomResponse updateStatus(Long id, UpdateRoomStatusRequest request);

	List<OTRoomResponse> getAvailableRooms();

	void enableRoom(Long id);

	void disableRoom(Long id);

	void mapFeatures(Long roomId, FeatureMappingRequest request);

	void unmapFeatures(Long roomId, FeatureMappingRequest request);

	List<OTRoomFeatureResponse> getRoomFeatures(Long roomId);

}