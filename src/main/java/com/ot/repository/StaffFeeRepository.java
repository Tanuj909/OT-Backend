package com.ot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ot.entity.Hospital;
import com.ot.entity.StaffFee;
import com.ot.entity.User;

@Repository
public interface StaffFeeRepository extends JpaRepository<StaffFee, Long> {

    // Ek staff ka fee record — hospital ke andar ek hi hoga
    Optional<StaffFee> findByStaffAndHospital(User staff, Hospital hospital);

    boolean existsByStaffAndHospital(User staff, Hospital hospital);

    List<StaffFee> findByHospital(Hospital hospital);

    List<StaffFee> findByHospitalAndIsActive(Hospital hospital, Boolean isActive);
}