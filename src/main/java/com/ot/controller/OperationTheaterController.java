package com.ot.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ot.dto.operationtheater.OperationTheaterCreateRequest;
import com.ot.dto.operationtheater.OperationTheaterResponse;
import com.ot.dto.otRoom.OTRoomResponse;
import com.ot.enums.TheaterStatus;
import com.ot.service.OperationTheaterService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/operation-theaters")
@RequiredArgsConstructor
public class OperationTheaterController {

    private final OperationTheaterService service;

    @PostMapping
    public ResponseEntity<OperationTheaterResponse> create(
            @Valid @RequestBody OperationTheaterCreateRequest request) {

        return ResponseEntity.ok(service.create(request));
    }

    @GetMapping
    public ResponseEntity<List<OperationTheaterResponse>> getAll() {

        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OperationTheaterResponse> getById(@PathVariable Long id) {

        return ResponseEntity.ok(service.getById(id));
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<OperationTheaterResponse>> getActiveTheaters() {

        return ResponseEntity.ok(service.getActiveTheaters());
    }

    @PutMapping("/{id}")
    public ResponseEntity<OperationTheaterResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody OperationTheaterCreateRequest request) {

        return ResponseEntity.ok(service.update(id, request));
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<OperationTheaterResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam TheaterStatus status) {

        return ResponseEntity.ok(service.updateStatus(id, status));
    }
    
    @GetMapping("/{theaterId}/rooms")
    public ResponseEntity<List<OTRoomResponse>> getRoomsByTheater(
            @PathVariable Long theaterId) {

        return ResponseEntity.ok(service.getRoomsByTheater(theaterId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        service.delete(id);

        return ResponseEntity.noContent().build();
    }
}
