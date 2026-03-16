package com.ot.dto.operationNotes;

import com.ot.enums.NoteType;

import lombok.Data;

@Data
public class OperationNoteRequest {
    private NoteType type;
    private String content;
    private boolean isConfidential;
}