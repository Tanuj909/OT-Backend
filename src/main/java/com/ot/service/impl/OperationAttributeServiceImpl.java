package com.ot.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ot.dto.operationNotes.OperationAttributeRequest;
import com.ot.dto.operationNotes.OperationAttributeResponse;
import com.ot.entity.OperationAttribute;
import com.ot.entity.ScheduledOperation;
import com.ot.entity.User;
import com.ot.exception.ResourceNotFoundException;
import com.ot.exception.UnauthorizedException;
import com.ot.repository.OperationAttributeRepository;
import com.ot.repository.ScheduledOperationRepository;
import com.ot.security.CustomUserDetails;
import com.ot.service.OperationAttributeService;

import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OperationAttributeServiceImpl implements OperationAttributeService {

    private final OperationAttributeRepository attributeRepository;
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
    public OperationAttributeResponse addAttribute(Long operationId, OperationAttributeRequest request) {

        User currentUser = currentUser();

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        // Duplicate attribute name check
        boolean exists = attributeRepository.existsByScheduledOperationIdAndAttributeName(
                operationId, request.getAttributeName());
        if (exists) {
            throw new ValidationException("Attribute '" + request.getAttributeName() + "' already exists for this operation");
        }

        OperationAttribute attribute = OperationAttribute.builder()
                .hospital(currentUser.getHospital())
                .scheduledOperation(operation)
                .attributeName(request.getAttributeName())
                .attributeValue(request.getAttributeValue())
                .attributeType(request.getAttributeType())
                .isRequired(request.isRequired())
                .isSystem(request.isSystem())
                .build();

        attributeRepository.save(attribute);

        return toResponse(attribute);
    }

    // ---------------------------------------- Get ---------------------------------------- //

    @Override
    public List<OperationAttributeResponse> getAttributes(Long operationId) {

        User currentUser = currentUser();

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        return attributeRepository.findAllByScheduledOperationId(operationId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // --------------------------------------- Update --------------------------------------- //

    @Transactional
    @Override
    public OperationAttributeResponse updateAttribute(Long operationId, Long attributeId, OperationAttributeRequest request) {

        User currentUser = currentUser();

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        OperationAttribute attribute = attributeRepository.findById(attributeId)
                .orElseThrow(() -> new ResourceNotFoundException("Attribute not found"));

        if (!attribute.getScheduledOperation().getId().equals(operationId)) {
            throw new ValidationException("Attribute does not belong to this operation");
        }

        // System attribute update nahi ho sakti
        if (attribute.isSystem()) {
            throw new ValidationException("System attributes cannot be updated");
        }

        // Duplicate name check — apna hi naam ho toh skip
        if (request.getAttributeName() != null &&
            !request.getAttributeName().equals(attribute.getAttributeName()) &&
            attributeRepository.existsByScheduledOperationIdAndAttributeName(
                    operationId, request.getAttributeName())) {
            throw new ValidationException("Attribute '" + request.getAttributeName() + "' already exists for this operation");
        }

        if (request.getAttributeName() != null)  attribute.setAttributeName(request.getAttributeName());
        if (request.getAttributeValue() != null) attribute.setAttributeValue(request.getAttributeValue());
        if (request.getAttributeType() != null)  attribute.setAttributeType(request.getAttributeType());

        attributeRepository.save(attribute);

        return toResponse(attribute);
    }

    // --------------------------------------- Delete --------------------------------------- //

    @Transactional
    @Override
    public void deleteAttribute(Long operationId, Long attributeId) {

        User currentUser = currentUser();

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        OperationAttribute attribute = attributeRepository.findById(attributeId)
                .orElseThrow(() -> new ResourceNotFoundException("Attribute not found"));

        if (!attribute.getScheduledOperation().getId().equals(operationId)) {
            throw new ValidationException("Attribute does not belong to this operation");
        }

        if (attribute.isSystem()) {
            throw new ValidationException("System attributes cannot be deleted");
        }

        attributeRepository.delete(attribute);
    }

    // --------------------------------------- Mapper --------------------------------------- //

    private OperationAttributeResponse toResponse(OperationAttribute attribute) {
        return OperationAttributeResponse.builder()
                .id(attribute.getId())
                .operationId(attribute.getScheduledOperation().getId())
                .attributeName(attribute.getAttributeName())
                .attributeValue(attribute.getAttributeValue())
                .attributeType(attribute.getAttributeType())
                .isRequired(attribute.isRequired())
                .isSystem(attribute.isSystem())
                .createdAt(attribute.getCreatedAt())
                .updatedAt(attribute.getUpdatedAt())
                .build();
    }
}