package com.ot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ot.entity.Hospital;
import com.ot.entity.Ward;
import com.ot.entity.WardRoom;
import com.ot.enums.WardRoomType;

public interface WardRoomRepository extends JpaRepository<WardRoom, Long> {
    List<WardRoom> findByWard(Ward ward);
    List<WardRoom> findByWardAndIsActive(Ward ward, Boolean isActive);
    List<WardRoom> findByHospitalAndRoomType(Hospital hospital, WardRoomType roomType);
    boolean existsByWardAndRoomNumber(Ward ward, String roomNumber);
}