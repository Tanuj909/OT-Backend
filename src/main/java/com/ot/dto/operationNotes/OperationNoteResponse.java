package com.ot.dto.operationNotes;

import java.time.LocalDateTime;

import com.ot.enums.NoteType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OperationNoteResponse {
    private Long id;
    private Long operationId;
    private NoteType type;
    private LocalDateTime noteTime;
    private String authorId;
    private String authorName;
    private String authorRole;
    private String content;
    private boolean isConfidential;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}