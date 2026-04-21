package com.ot.repository;

import com.ot.entity.Hospital;
import com.ot.entity.User;
import com.ot.enums.RoleType;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    boolean existsByEmail(String email);
    
    boolean existsByRole(RoleType role);  // Check by role instead of userType
    
    List<User> findByRole(RoleType role);
    
    Optional<User> findByEmail(String email);
    
	List<User> findByHospitalIdAndRole(Long hospitalId, RoleType role);
	
	List<User> findByHospitalId(Long hospitalId);
	
	List<User> findByHospitalIdAndRoleNotIn(Long hospitalId, List<RoleType> roles);
	

    @Query(value = """
        SELECT u.* 
        FROM users u
        WHERE u.hospital_id = :hospitalId
        AND u.is_active = true
        AND u.role IN :roles
        AND NOT EXISTS (
            SELECT 1
            FROM operation_surgeons os
            JOIN scheduled_operations so ON os.operation_id = so.id
            WHERE os.surgeon_id = u.id
            AND so.hospital_id = :hospitalId
            AND so.status IN ('SCHEDULED', 'IN_PROGRESS')
        )
        """, nativeQuery = true)
    List<User> findAvailableSurgeons(
            @Param("hospitalId") Long hospitalId,
            @Param("roles") Set<String> roles
    );

    @Query(value = """
            SELECT u.* 
            FROM users u
            WHERE u.hospital_id = :hospitalId
            AND u.is_active = true
            AND u.role IN :roles
            AND NOT EXISTS (
                SELECT 1
                FROM operation_staff os
                JOIN scheduled_operations so ON os.operation_id = so.id
                WHERE os.staff_id = u.id
                AND so.hospital_id = :hospitalId
                AND so.status IN ('SCHEDULED', 'IN_PROGRESS')
            )
            """, nativeQuery = true)
        List<User> findAvailableStaff(
                @Param("hospitalId") Long hospitalId,
                @Param("roles") Set<String> roles
        );

	List<User> findByHospitalAndIsActive(Hospital hospital, boolean b);
}