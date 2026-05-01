package com.ot.service;

import java.util.List;

import com.ot.dto.operationtheater.OperationTheaterCreateRequest;
import com.ot.dto.operationtheater.OperationTheaterResponse;
import com.ot.dto.otRoom.OTRoomResponse;
import com.ot.enums.TheaterStatus;


public interface OperationTheaterService {

    OperationTheaterResponse create(OperationTheaterCreateRequest request);

    List<OperationTheaterResponse> getAll();

    OperationTheaterResponse getById(Long id);

    OperationTheaterResponse update(Long id, OperationTheaterCreateRequest request);

    void delete(Long id);

	List<OTRoomResponse> getRoomsByTheater(Long theaterId);

	List<OperationTheaterResponse> getActiveTheaters();

	OperationTheaterResponse updateStatus(Long id, TheaterStatus status);
}
