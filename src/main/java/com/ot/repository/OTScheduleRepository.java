package com.ot.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ot.entity.OTRoom;
import com.ot.entity.OTSchedule;

public interface OTScheduleRepository extends JpaRepository<OTSchedule, Long>{
	
    @Query("""
            SELECT COUNT(s) > 0
            FROM OTSchedule s
            WHERE s.room = :room
            AND (
                 :startTime < s.endTime
                 AND :endTime > s.startTime
            )
            """)
     boolean existsByRoomAndTimeOverlap(
             @Param("room") OTRoom room,
             @Param("startTime") LocalDateTime startTime,
             @Param("endTime") LocalDateTime endTime
     );
}
