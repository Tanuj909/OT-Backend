package com.ot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ot.entity.OperationAttribute;

public interface OperationAttributeRepository extends JpaRepository<OperationAttribute, Long> {
    List<OperationAttribute> findAllByScheduledOperationId(Long operationId);
    boolean existsByScheduledOperationIdAndAttributeName(Long operationId, String attributeName);
}