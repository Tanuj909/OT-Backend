package com.ot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ot.entity.ScheduledOperation;
import com.ot.enums.OperationStatus;

@Repository
public interface ScheduledOperationRepository extends JpaRepository<ScheduledOperation, Long> {
	
	List<ScheduledOperation> findByHospitalId(Long hospitalId);

	List<ScheduledOperation> findByHospitalIdAndStatus(Long hospitalId, OperationStatus status);

	List<ScheduledOperation> findByHospitalIdAndRoomId(Long hospitalId, Long roomId);
}
