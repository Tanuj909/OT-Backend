package com.ot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ot.entity.ScheduledOperation;
import com.ot.enums.OperationStatus;

@Repository
public interface ScheduledOperationRepository extends JpaRepository<ScheduledOperation, Long> {
	
	List<ScheduledOperation> findByHospitalId(Long hospitalId);

	List<ScheduledOperation> findByHospitalIdAndStatus(Long hospitalId, OperationStatus status);

	List<ScheduledOperation> findByHospitalIdAndRoomId(Long hospitalId, Long roomId);
	

	// Surgeon ke liye
	@Query(value = """
	    SELECT so.* FROM scheduled_operations so
	    INNER JOIN operation_surgeons os ON so.id = os.operation_id
	    WHERE os.surgeon_id = :userId
	    AND so.hospital_id = :hospitalId
	    AND so.status IN :statuses
	    """, nativeQuery = true)
	List<ScheduledOperation> findOperationsBySurgeonId(
	    @Param("userId") Long userId,
	    @Param("hospitalId") Long hospitalId,
	    @Param("statuses") List<String> statuses
	);

	// Staff ke liye
	@Query(value = """
	    SELECT so.* FROM scheduled_operations so
	    INNER JOIN operation_staff os ON so.id = os.operation_id
	    WHERE os.staff_id = :userId
	    AND so.hospital_id = :hospitalId
	    AND so.status IN :statuses
	    """, nativeQuery = true)
	List<ScheduledOperation> findOperationsByStaffId(
	    @Param("userId") Long userId,
	    @Param("hospitalId") Long hospitalId,
	    @Param("statuses") List<String> statuses
	);
}
