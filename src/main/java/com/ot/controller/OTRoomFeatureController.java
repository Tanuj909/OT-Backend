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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ot.dto.otRoom.OTRoomFeatureRequest;
import com.ot.repository.OTRoomFeatureRepository;
import com.ot.service.OTRoomFeatureService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ot-features")
@RequiredArgsConstructor
public class OTRoomFeatureController {

    private final OTRoomFeatureService service;
    private final OTRoomFeatureRepository roomFeatureRepository;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody OTRoomFeatureRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(service.getAll());
    }
    
    @GetMapping("/active")
    public ResponseEntity<?> getAllActive() {
        return ResponseEntity.ok(service.getAllActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody OTRoomFeatureRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok("Feature deleted successfully");
    }
    
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> hardDelete(@PathVariable Long id) {
        service.hardDelete(id);
        return ResponseEntity.ok("Feature deleted successfully");
    }

    @PostMapping("/bulk")
    public ResponseEntity<?> bulkCreate(@RequestBody List<OTRoomFeatureRequest> requests) {
        return ResponseEntity.ok(service.bulkCreate(requests));
    }
    
    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String keyword) {
        return ResponseEntity.ok(
        		roomFeatureRepository.findByNameContainingIgnoreCase(keyword)
        );
    }
    
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<?> toggle(@PathVariable Long id) {
        return ResponseEntity.ok(service.toggleStatus(id));
    }
}