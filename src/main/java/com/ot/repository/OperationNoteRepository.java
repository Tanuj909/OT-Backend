package com.ot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ot.entity.OperationNote;

public interface OperationNoteRepository extends JpaRepository<OperationNote, Long> {
    List<OperationNote> findAllByScheduledOperationId(Long operationId);
}
