package com.ot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ot.entity.OTRoom;
import com.ot.enums.RoomStatus;

public interface OTRoomRepository extends JpaRepository<OTRoom, Long> {

    List<OTRoom> findByHospitalId(Long hospitalId);

    List<OTRoom> findByOperationTheaterId(Long operationTheaterId);

    Optional<OTRoom> findByIdAndHospitalId(Long id, Long hospitalId);
    
    List<OTRoom> findByHospitalIdAndStatus(Long hospitalId, RoomStatus status);
    
    List<OTRoom> findByOperationTheaterIdAndOperationTheaterHospitalId(
    	    Long operationTheaterId, Long hospitalId
    	);
}