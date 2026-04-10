package com.ot.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ot.entity.OTRoomPricing;

@Repository
public interface OTRoomPricingRepository extends JpaRepository<OTRoomPricing, Long> {

//	OT ROOM PRICING
    Optional<OTRoomPricing> findByRoomId(Long roomId);
    boolean existsByRoomId(Long roomId);
    
//  WARD ROOM PRICING
    Optional<OTRoomPricing> findByWardRoomId(Long roomId);
    boolean existsByWardRoomId(Long roomId);
}
