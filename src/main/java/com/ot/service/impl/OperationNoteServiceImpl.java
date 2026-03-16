package com.ot.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ot.dto.operationNotes.OperationNoteRequest;
import com.ot.dto.operationNotes.OperationNoteResponse;
import com.ot.entity.OperationNote;
import com.ot.entity.ScheduledOperation;
import com.ot.entity.User;
import com.ot.enums.OperationStatus;
import com.ot.enums.RoleType;
import com.ot.exception.ResourceNotFoundException;
import com.ot.exception.UnauthorizedException;
import com.ot.exception.ValidationException;
import com.ot.repository.OperationNoteRepository;
import com.ot.repository.ScheduledOperationRepository;
import com.ot.security.CustomUserDetails;
import com.ot.service.OperationNoteService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OperationNoteServiceImpl implements OperationNoteService {

    private final OperationNoteRepository noteRepository;
    private final ScheduledOperationRepository operationRepository;

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No user is logged in");
        }
        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
        return cud.getUser();
    }

    // ---------------------------------------- Add ---------------------------------------- //

    @Transactional
    @Override
    public OperationNoteResponse addNote(Long operationId, OperationNoteRequest request) {

        User currentUser = currentUser();

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        // Same hospital check
        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        // Note sirf IN_PROGRESS ya SCHEDULED operations mein add ho sakta hai
        if (operation.getStatus().equals(OperationStatus.COMPLETED) ||
            operation.getStatus().equals(OperationStatus.CANCELLED)) {
            throw new ValidationException("Notes cannot be added to " + operation.getStatus() + " operations");
        }

        OperationNote note = OperationNote.builder()
                .hospital(currentUser.getHospital())
                .scheduledOperation(operation)
                .type(request.getType())
                .noteTime(LocalDateTime.now())
                .authorId(String.valueOf(currentUser.getId()))
                .authorName(currentUser.getUserName())
                .authorRole(currentUser.getRole().name())
                .content(request.getContent())
                .isConfidential(request.isConfidential())
                .build();

        noteRepository.save(note);

        return toResponse(note);
    }

    // ---------------------------------------- Get ---------------------------------------- //

    @Override
    public List<OperationNoteResponse> getNotes(Long operationId) {

        User currentUser = currentUser();

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        return noteRepository.findAllByScheduledOperationId(operationId)
                .stream()
                // Confidential notes sirf author hi dekh sakta hai
                .filter(note -> !note.isConfidential() ||
                        note.getAuthorId().equals(String.valueOf(currentUser.getId())))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // --------------------------------------- Update --------------------------------------- //

    @Transactional
    @Override
    public OperationNoteResponse updateNote(Long operationId, Long noteId, OperationNoteRequest request) {

        User currentUser = currentUser();

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        OperationNote note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));

        if (!note.getScheduledOperation().getId().equals(operationId)) {
            throw new ValidationException("Note does not belong to this operation");
        }

        // Sirf author hi update kar sakta hai
        if (!note.getAuthorId().equals(String.valueOf(currentUser.getId()))) {
            throw new UnauthorizedException("Only the author can update this note");
        }

        if (request.getContent() != null)  note.setContent(request.getContent());
        if (request.getType() != null)     note.setType(request.getType());
        note.setConfidential(request.isConfidential());

        noteRepository.save(note);

        return toResponse(note);
    }

    // --------------------------------------- Delete --------------------------------------- //

    @Transactional
    @Override
    public void deleteNote(Long operationId, Long noteId) {

        User currentUser = currentUser();

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        OperationNote note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));

        if (!note.getScheduledOperation().getId().equals(operationId)) {
            throw new ValidationException("Note does not belong to this operation");
        }

        // Sirf author ya ADMIN delete kar sakta hai
        boolean isAuthor = note.getAuthorId().equals(String.valueOf(currentUser.getId()));
        boolean isAdmin = currentUser.getRole().equals(RoleType.ADMIN) ||
                          currentUser.getRole().equals(RoleType.HOSPITAL_ADMIN);

        if (!isAuthor && !isAdmin) {
            throw new UnauthorizedException("Only the author or admin can delete this note");
        }

        noteRepository.delete(note);
    }

    // --------------------------------------- Mapper --------------------------------------- //

    private OperationNoteResponse toResponse(OperationNote note) {
        return OperationNoteResponse.builder()
                .id(note.getId())
                .operationId(note.getScheduledOperation().getId())
                .type(note.getType())
                .noteTime(note.getNoteTime())
                .authorId(note.getAuthorId())
                .authorName(note.getAuthorName())
                .authorRole(note.getAuthorRole())
                .content(note.getContent())
                .isConfidential(note.isConfidential())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}