package com.ot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ot.entity.OTRoomFeature;

@Repository
public interface OTRoomFeatureRepository extends JpaRepository<OTRoomFeature, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<OTRoomFeature> findByIsActiveTrue();
    
    List<OTRoomFeature> findByNameContainingIgnoreCase(String keyword);

}