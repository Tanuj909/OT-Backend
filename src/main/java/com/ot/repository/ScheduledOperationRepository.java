package com.ot.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ot.entity.OTRoom;
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
	
	// ScheduledOperationRepository mein
	@Query(value = """
	    SELECT DISTINCT os.surgeon_id FROM operation_surgeons os
	    INNER JOIN scheduled_operations so ON os.operation_id = so.id
	    WHERE so.hospital_id = :hospitalId
	    AND so.status IN ('SCHEDULED', 'IN_PROGRESS')
	    AND (
	        (so.scheduled_start_time <= :endTime AND so.scheduled_end_time >= :startTime)
	        OR
	        (so.actual_start_time <= :endTime AND (so.actual_end_time >= :startTime OR so.actual_end_time IS NULL))
	    )
	    """, nativeQuery = true)
	Set<Long> findBusySurgeonIds(
	    @Param("hospitalId") Long hospitalId,
	    @Param("startTime") LocalDateTime startTime,
	    @Param("endTime") LocalDateTime endTime
	);

	@Query(value = """
	    SELECT DISTINCT os.staff_id FROM operation_staff os
	    INNER JOIN scheduled_operations so ON os.operation_id = so.id
	    WHERE so.hospital_id = :hospitalId
	    AND so.status IN ('SCHEDULED', 'IN_PROGRESS')
	    AND (
	        (so.scheduled_start_time <= :endTime AND so.scheduled_end_time >= :startTime)
	        OR
	        (so.actual_start_time <= :endTime AND (so.actual_end_time >= :startTime OR so.actual_end_time IS NULL))
	    )
	    """, nativeQuery = true)
	Set<Long> findBusyStaffIds(
	    @Param("hospitalId") Long hospitalId,
	    @Param("startTime") LocalDateTime startTime,
	    @Param("endTime") LocalDateTime endTime
	);
	
	@Query(value = """
		    SELECT so.* FROM scheduled_operations so
		    WHERE so.hospital_id = :hospitalId
		    AND so.status IN ('SCHEDULED', 'IN_PROGRESS')
		    AND (
		        (so.scheduled_start_time <= :endTime AND so.scheduled_end_time >= :startTime)
		        OR
		        (so.actual_start_time <= :endTime AND (so.actual_end_time >= :startTime OR so.actual_end_time IS NULL))
		    )
		    AND (
		        EXISTS (
		            SELECT 1 FROM operation_surgeons os
		            WHERE os.operation_id = so.id AND os.surgeon_id = :userId
		        )
		        OR
		        EXISTS (
		            SELECT 1 FROM operation_staff os
		            WHERE os.operation_id = so.id AND os.staff_id = :userId
		        )
		    )
		    LIMIT 1
		    """, nativeQuery = true)
		Optional<ScheduledOperation> findAssignedOperationForUser(
		    @Param("userId") Long userId,
		    @Param("hospitalId") Long hospitalId,
		    @Param("startTime") LocalDateTime startTime,
		    @Param("endTime") LocalDateTime endTime
		);
	
	@Query("SELECT COUNT(o) FROM ScheduledOperation o WHERE DATE(o.createdAt) = CURRENT_DATE")
	long countTodayOperations();
	
//----------------------------------------------------Admin Dashboard--------------------------------------------------------------------//
	// ScheduledOperationRepository mein

	@Query("""
		    SELECT so FROM ScheduledOperation so
		    WHERE so.hospital.id = :hospitalId
		    AND so.scheduledStartTime BETWEEN :start AND :end
		    ORDER BY so.scheduledStartTime ASC
		""")
		List<ScheduledOperation> findTodayOperations(
		        @Param("hospitalId") Long hospitalId,
		        @Param("start") LocalDateTime start,
		        @Param("end") LocalDateTime end
		);

	// Pending requests
	@Query("""
	    SELECT so FROM ScheduledOperation so
	    WHERE so.hospital.id = :hospitalId
	    AND so.status = 'REQUESTED'
	    ORDER BY so.createdAt ASC
	    """)
	List<ScheduledOperation> findPendingRequests(@Param("hospitalId") Long hospitalId);

	// Recent operations
	@Query("""
	    SELECT so FROM ScheduledOperation so
	    WHERE so.hospital.id = :hospitalId
	    AND so.status IN ('COMPLETED', 'CANCELLED', 'IN_PROGRESS')
	    ORDER BY so.updatedAt DESC
	    """)
	List<ScheduledOperation> findRecentOperations(
	        @Param("hospitalId") Long hospitalId,
	        Pageable pageable);

	// Overdue operations — IN_PROGRESS and exceeded scheduledEndTime
	@Query("""
	    SELECT so FROM ScheduledOperation so
	    WHERE so.hospital.id = :hospitalId
	    AND so.status = 'IN_PROGRESS'
	    AND so.scheduledEndTime < :now
	    ORDER BY so.scheduledEndTime ASC
	    """)
	List<ScheduledOperation> findOverdueOperations(
	        @Param("hospitalId") Long hospitalId,
	        @Param("now") LocalDateTime now);

	// Stats
	Long countByHospitalIdAndStatus(Long hospitalId, OperationStatus status);

	@Query("""
	    SELECT COUNT(so) FROM ScheduledOperation so
	    WHERE so.hospital.id = :hospitalId
	    AND so.status IN ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')
	    AND DATE(so.scheduledStartTime) = CURRENT_DATE
	    """)
	Long countTodayOperations(@Param("hospitalId") Long hospitalId);
	
	List<ScheduledOperation> findByRoomAndStatus(OTRoom room, OperationStatus status);
	
	Optional<ScheduledOperation> findByIpdAdmissionId(String ipdAdmissionId);
}
