package com.ot.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ot.dto.otRoom.OTRoomFeatureRequest;
import com.ot.dto.otRoom.OTRoomFeatureResponse;
import com.ot.entity.OTRoomFeature;
import com.ot.exception.ResourceNotFoundException;
import com.ot.exception.ValidationException;
import com.ot.mapper.OTRoomFeatureMapper;
import com.ot.repository.OTRoomFeatureRepository;
import com.ot.service.OTRoomFeatureService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OTRoomFeatureServiceImpl implements OTRoomFeatureService {

    private final OTRoomFeatureRepository repository;
    private final OTRoomFeatureMapper mapper;

    @Override
    public OTRoomFeatureResponse create(OTRoomFeatureRequest request) {

        if (repository.existsByNameIgnoreCase(request.getName())) {
            throw new ValidationException("Feature already exists");
        }

        OTRoomFeature feature = mapper.toEntity(request);
        return mapper.toResponse(repository.save(feature));
    }

    @Override
    public List<OTRoomFeatureResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
    
    @Override
    public List<OTRoomFeatureResponse> getAllActive() {
        return repository.findByIsActiveTrue()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public OTRoomFeatureResponse getById(Long id) {
        OTRoomFeature feature = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found"));

        return mapper.toResponse(feature);
    }

    @Override
    public OTRoomFeatureResponse update(Long id, OTRoomFeatureRequest request) {

        OTRoomFeature feature = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found"));

        feature.setName(request.getName());

        return mapper.toResponse(repository.save(feature));
    }

    @Override
    public void delete(Long id) {

        OTRoomFeature feature = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found"));

        feature.setIsActive(false); // soft delete
        repository.save(feature);
    }
    
    @Override
    public void hardDelete(Long id) {
    	
        OTRoomFeature feature = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found"));
        
        repository.deleteById(id);
    }

    @Override
    public List<OTRoomFeatureResponse> bulkCreate(List<OTRoomFeatureRequest> requests) {

        List<OTRoomFeature> features = requests.stream()
                .filter(req -> !repository.existsByNameIgnoreCase(req.getName()))
                .map(mapper::toEntity)
                .toList();

        return repository.saveAll(features)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
    
    @Override
    public OTRoomFeatureResponse toggleStatus(Long id) {

        OTRoomFeature feature = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found"));

        // toggle logic
        feature.setIsActive(!feature.getIsActive());

        return mapper.toResponse(repository.save(feature));
    }
}
