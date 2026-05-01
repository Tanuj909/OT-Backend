package com.ot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ot.entity.OperationTheater;
import com.ot.enums.TheaterStatus;

public interface OperationTheaterRepository extends JpaRepository<OperationTheater, Long> {

    List<OperationTheater> findByHospitalId(Long hospitalId);

    Optional<OperationTheater> findByIdAndHospitalId(Long id, Long hospitalId);
    
    List<OperationTheater> findByStatusAndHospitalId(
            TheaterStatus status,
            Long hospitalId
    );
    

}