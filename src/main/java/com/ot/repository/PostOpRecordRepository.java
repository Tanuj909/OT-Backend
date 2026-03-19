package com.ot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ot.entity.PostOpRecord;

public interface PostOpRecordRepository extends JpaRepository<PostOpRecord, Long>{

}