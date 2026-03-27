package com.ot.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ot.dto.otRoom.FeatureMappingRequest;
import com.ot.dto.otRoom.OTRoomCreateRequest;
import com.ot.dto.otRoom.OTRoomResponse;
import com.ot.dto.otRoom.UpdateRoomStatusRequest;
import com.ot.service.OTRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/ot-rooms")
@RequiredArgsConstructor
public class OTRoomController {

    private final OTRoomService service;

    @PostMapping
    public ResponseEntity<OTRoomResponse> create(
            @Valid @RequestBody OTRoomCreateRequest request) {

        return ResponseEntity.ok(service.create(request));
    }

    @GetMapping
    public ResponseEntity<List<OTRoomResponse>> getAll() {

        return ResponseEntity.ok(service.getAll());
    }
    
 // ✅ Map Features
    @PostMapping("/{roomId}/features")
    public ResponseEntity<?> mapFeatures(@PathVariable Long roomId,
                                         @RequestBody FeatureMappingRequest request) {
        service.mapFeatures(roomId, request);
        return ResponseEntity.ok("Features mapped successfully");
    }

    // ✅ Unmap Features
    @DeleteMapping("/{roomId}/features")
    public ResponseEntity<?> unmapFeatures(@PathVariable Long roomId,
                                           @RequestBody FeatureMappingRequest request) {
        service.unmapFeatures(roomId, request);
        return ResponseEntity.ok("Features unmapped successfully");
    }

    // ✅ Get Room Features
    @GetMapping("/{roomId}/features")
    public ResponseEntity<?> getRoomFeatures(@PathVariable Long roomId) {
        return ResponseEntity.ok(service.getRoomFeatures(roomId));
    }

    @GetMapping("/theater/{id}")
    public ResponseEntity<List<OTRoomResponse>> getByTheater(@PathVariable Long id) {

        return ResponseEntity.ok(service.getByTheater(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OTRoomResponse> getById(@PathVariable Long id) {

        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OTRoomResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody OTRoomCreateRequest request) {

        return ResponseEntity.ok(service.update(id, request));
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<OTRoomResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoomStatusRequest request) {

        return ResponseEntity.ok(service.updateStatus(id, request));
    }
    
    @GetMapping("/available")
    public ResponseEntity<List<OTRoomResponse>> getAvailableRooms() {

        return ResponseEntity.ok(service.getAvailableRooms());
    }
    
    @PatchMapping("/{id}/enable")
    public ResponseEntity<Void> enableRoom(@PathVariable Long id) {

        service.enableRoom(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<Void> disableRoom(@PathVariable Long id) {

        service.disableRoom(id);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        service.delete(id);

        return ResponseEntity.noContent().build();
    }
}