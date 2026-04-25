package com.ot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ot.entity.Hospital;
import com.ot.entity.WardBed;
import com.ot.entity.WardRoom;
import com.ot.enums.BedStatus;

@Repository
public interface WardBedRepository extends JpaRepository<WardBed, Long> {

    List<WardBed> findByWardRoom(WardRoom wardRoom);

    List<WardBed> findByWardRoomAndIsActive(WardRoom wardRoom, Boolean isActive);

    List<WardBed> findByWardRoomAndStatus(WardRoom wardRoom, BedStatus status);

    boolean existsByWardRoomAndBedNumber(WardRoom wardRoom, String bedNumber);

	List<WardBed> findByHospital(Hospital hospital);
}