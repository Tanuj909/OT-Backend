package com.ot.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.ot.dto.equipment.EquipmentAttributeRequest;
import com.ot.dto.equipment.EquipmentAttributeResponse;
import com.ot.entity.Equipment;
import com.ot.entity.EquipmentAttribute;
import com.ot.entity.User;
import com.ot.exception.ResourceNotFoundException;
import com.ot.exception.UnauthorizedException;
import com.ot.repository.EquipmentAttributeRepository;
import com.ot.repository.EquipmentRepository;
import com.ot.security.CustomUserDetails;
import com.ot.service.EquipmentAttributeService;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EquipmentAttributeServiceImpl implements EquipmentAttributeService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentAttributeRepository attributeRepository;

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
    public EquipmentAttributeResponse addAttribute(Long equipmentId, EquipmentAttributeRequest request) {

        User currentUser = currentUser();

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found"));

        // Same hospital check
        if (!equipment.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this equipment");
        }

        // Duplicate attribute name check
        boolean exists = attributeRepository.existsByEquipmentIdAndAttributeName(
                equipmentId, request.getAttributeName());
        if (exists) {
            throw new ValidationException("Attribute '" + request.getAttributeName() + "' already exists for this equipment");
        }

        EquipmentAttribute attribute = EquipmentAttribute.builder()
                .hospital(currentUser.getHospital())
                .equipment(equipment)
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
    public List<EquipmentAttributeResponse> getAttributes(Long equipmentId) {

        User currentUser = currentUser();

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found"));

        if (!equipment.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this equipment");
        }

        return attributeRepository.findAllByEquipmentId(equipmentId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // --------------------------------------- Update --------------------------------------- //

    @Transactional
    @Override
    public EquipmentAttributeResponse updateAttribute(Long equipmentId, Long attributeId, EquipmentAttributeRequest request) {

        User currentUser = currentUser();

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found"));

        if (!equipment.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this equipment");
        }

        EquipmentAttribute attribute = attributeRepository.findById(attributeId)
                .orElseThrow(() -> new ResourceNotFoundException("Attribute not found"));

        // Attribute is equipment ka hi hai?
        if (!attribute.getEquipment().getId().equals(equipmentId)) {
            throw new ValidationException("Attribute does not belong to this equipment");
        }

        // System attribute update nahi ho sakti
        if (attribute.isSystem()) {
            throw new ValidationException("System attributes cannot be updated");
        }

        // Duplicate name check — apna hi naam ho toh skip
        if (request.getAttributeName() != null &&
            !request.getAttributeName().equals(attribute.getAttributeName()) &&
            attributeRepository.existsByEquipmentIdAndAttributeName(equipmentId, request.getAttributeName())) {
            throw new ValidationException("Attribute '" + request.getAttributeName() + "' already exists for this equipment");
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
    public void deleteAttribute(Long equipmentId, Long attributeId) {

        User currentUser = currentUser();

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found"));

        if (!equipment.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this equipment");
        }

        EquipmentAttribute attribute = attributeRepository.findById(attributeId)
                .orElseThrow(() -> new ResourceNotFoundException("Attribute not found"));

        if (!attribute.getEquipment().getId().equals(equipmentId)) {
            throw new ValidationException("Attribute does not belong to this equipment");
        }

        // System attribute delete nahi ho sakti
        if (attribute.isSystem()) {
            throw new ValidationException("System attributes cannot be deleted");
        }

        attributeRepository.delete(attribute);
    }

    // --------------------------------------- Mapper --------------------------------------- //

    private EquipmentAttributeResponse toResponse(EquipmentAttribute attribute) {
        return EquipmentAttributeResponse.builder()
                .id(attribute.getId())
                .equipmentId(attribute.getEquipment().getId())
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
