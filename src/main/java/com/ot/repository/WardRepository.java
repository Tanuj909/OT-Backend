package com.ot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ot.entity.Hospital;
import com.ot.entity.Ward;
import com.ot.enums.WardType;

public interface WardRepository extends JpaRepository<Ward, Long> {
    List<Ward> findByHospital(Hospital hospital);
    List<Ward> findByHospitalAndWardType(Hospital hospital, WardType wardType);
    List<Ward> findByHospitalAndIsActive(Hospital hospital, Boolean isActive);
    boolean existsByHospitalAndWardNumber(Hospital hospital, String wardNumber);
}