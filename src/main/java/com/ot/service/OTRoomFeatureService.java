package com.ot.service;

import java.util.List;

import com.ot.dto.otRoom.OTRoomFeatureRequest;
import com.ot.dto.otRoom.OTRoomFeatureResponse;

public interface OTRoomFeatureService {

	OTRoomFeatureResponse create(OTRoomFeatureRequest request);

	List<OTRoomFeatureResponse> getAll();

	OTRoomFeatureResponse getById(Long id);

	OTRoomFeatureResponse update(Long id, OTRoomFeatureRequest request);

	void delete(Long id);

	List<OTRoomFeatureResponse> bulkCreate(List<OTRoomFeatureRequest> requests);

	OTRoomFeatureResponse toggleStatus(Long id);

	List<OTRoomFeatureResponse> getAllActive();

	void hardDelete(Long id);

}
