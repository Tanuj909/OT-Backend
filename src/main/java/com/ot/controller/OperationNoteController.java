package com.ot.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ot.dto.operationNotes.OperationNoteRequest;
import com.ot.dto.operationNotes.OperationNoteResponse;
import com.ot.dto.response.ApiResponse;
import com.ot.service.OperationNoteService;

import lombok.RequiredArgsConstructor;

//Operation Notes
@RestController
@RequestMapping("/api/operations/{operationId}/notes")
@RequiredArgsConstructor
public class OperationNoteController {

 private final OperationNoteService noteService;

 @PostMapping
 public ResponseEntity<ApiResponse<OperationNoteResponse>> addNote(
         @PathVariable Long operationId,
         @RequestBody OperationNoteRequest request) {
     return ResponseEntity.status(HttpStatus.CREATED)
             .body(ApiResponse.success("Note added successfully",
                     noteService.addNote(operationId, request)));
 }

 @GetMapping
 public ResponseEntity<ApiResponse<List<OperationNoteResponse>>> getNotes(
         @PathVariable Long operationId) {
     return ResponseEntity.ok(ApiResponse.success("Notes fetched successfully",
             noteService.getNotes(operationId)));
 }

 @PutMapping("/{noteId}")
 public ResponseEntity<ApiResponse<OperationNoteResponse>> updateNote(
         @PathVariable Long operationId,
         @PathVariable Long noteId,
         @RequestBody OperationNoteRequest request) {
     return ResponseEntity.ok(ApiResponse.success("Note updated successfully",
             noteService.updateNote(operationId, noteId, request)));
 }

 @DeleteMapping("/{noteId}")
 public ResponseEntity<ApiResponse<Void>> deleteNote(
         @PathVariable Long operationId,
         @PathVariable Long noteId) {
     noteService.deleteNote(operationId, noteId);
     return ResponseEntity.ok(ApiResponse.success("Note deleted successfully", null));
 }
}
