package com.ot.service;

import java.util.List;

import com.ot.dto.operationNotes.OperationNoteRequest;
import com.ot.dto.operationNotes.OperationNoteResponse;

public interface OperationNoteService {
    OperationNoteResponse addNote(Long operationId, OperationNoteRequest request);
    List<OperationNoteResponse> getNotes(Long operationId);
    OperationNoteResponse updateNote(Long operationId, Long noteId, OperationNoteRequest request);
    void deleteNote(Long operationId, Long noteId);
}
