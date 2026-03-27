package com.ot.service.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ot.dto.oTItemCatalog.OTItemCatalogRequest;
import com.ot.dto.oTItemCatalog.OTItemCatalogResponse;
import com.ot.dto.oTItemCatalog.OTItemCatalogUpdateRequest;
import com.ot.entity.Hospital;
import com.ot.entity.OTItemCatalog;
import com.ot.entity.User;
import com.ot.enums.CatalogItemType;
import com.ot.enums.RoleType;
import com.ot.exception.ResourceNotFoundException;
import com.ot.exception.UnauthorizedException;
import com.ot.exception.ValidationException;
import com.ot.repository.OTItemCatalogRepository;
import com.ot.security.CustomUserDetails;
import com.ot.service.OTItemCatalogService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OTItemCatalogServiceImpl implements OTItemCatalogService {

    private final OTItemCatalogRepository catalogRepository;

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No user is logged in");
        }
        return ((CustomUserDetails) auth.getPrincipal()).getUser();
    }

    // ---------------------------------------- Create ---------------------------------------- //

    @Transactional
    @Override
    public OTItemCatalogResponse createItem(OTItemCatalogRequest request) {

        User currentUser = currentUser();

        // Role check — sirf ADMIN ya HOSPITAL_ADMIN
        if (!Set.of(RoleType.ADMIN, RoleType.HOSPITAL_ADMIN).contains(currentUser.getRole())) {
            throw new ValidationException("Only admin can create catalog items");
        }

        // Duplicate itemCode check
        if (catalogRepository.existsByHospitalAndItemCode(currentUser.getHospital(), request.getItemCode())) {
            throw new ValidationException("Item code already exists: " + request.getItemCode());
        }

        System.out.println("Tyep:" + request.getItemType());
        OTItemCatalog item = OTItemCatalog.builder()
                .hospital(currentUser.getHospital())
                .itemCode(request.getItemCode())
                .itemName(request.getItemName())
                .itemType(request.getItemType())
                .category(request.getCategory())
                .subCategory(request.getSubCategory())
                .manufacturer(request.getManufacturer())
                .modelNumber(request.getModelNumber())
                .description(request.getDescription())
                .unit(request.getUnit())
                .createdBy(currentUser.getUserName())
                .build();

        catalogRepository.save(item);

        return mapToResponse(item);
    }

    // ----------------------------------------- Get ------------------------------------------ //

    @Override
    public OTItemCatalogResponse getItemById(Long itemId) {

        User currentUser = currentUser();

        OTItemCatalog item = catalogRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        if (!item.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this item");
        }

        return mapToResponse(item);
    }

    @Override
    public List<OTItemCatalogResponse> getAllItems(CatalogItemType itemType, String category, Boolean isActive) {

        User currentUser = currentUser();
        Hospital hospital = currentUser.getHospital();

        // Dynamic filter
        if (itemType != null && category != null && isActive != null) {
            return catalogRepository
                    .findByHospitalAndItemTypeAndCategoryAndIsActive(hospital, itemType, category, isActive)
                    .stream().map(this::mapToResponse).collect(Collectors.toList());
        } else if (itemType != null) {
            return catalogRepository.findByHospitalAndItemType(hospital, itemType)
                    .stream().map(this::mapToResponse).collect(Collectors.toList());
        } else if (category != null) {
            return catalogRepository.findByHospitalAndCategory(hospital, category)
                    .stream().map(this::mapToResponse).collect(Collectors.toList());
        } else if (isActive != null) {
            return catalogRepository.findByHospitalAndIsActive(hospital, isActive)
                    .stream().map(this::mapToResponse).collect(Collectors.toList());
        }

        // No filter — sab return karo
        return catalogRepository.findByHospital(hospital)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
    }

    @Override
    public List<OTItemCatalogResponse> searchItems(String keyword) {

        User currentUser = currentUser();

        return catalogRepository
                .findByHospitalAndItemNameContainingIgnoreCase(currentUser.getHospital(), keyword)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<OTItemCatalogResponse> getItemsByType(CatalogItemType itemType) {

        User currentUser = currentUser();

        return catalogRepository.findByHospitalAndItemType(currentUser.getHospital(), itemType)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ---------------------------------------- Update ---------------------------------------- //

    @Transactional
    @Override
    public OTItemCatalogResponse updateItem(Long itemId, OTItemCatalogUpdateRequest request) {

        User currentUser = currentUser();

        if (!Set.of(RoleType.ADMIN, RoleType.HOSPITAL_ADMIN).contains(currentUser.getRole())) {
            throw new ValidationException("Only admin can update catalog items");
        }

        OTItemCatalog item = catalogRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        if (!item.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to update this item");
        }

        // Partial update
        if (request.getItemName() != null)     item.setItemName(request.getItemName());
        if (request.getCategory() != null)     item.setCategory(request.getCategory());
        if (request.getSubCategory() != null)  item.setSubCategory(request.getSubCategory());
        if (request.getManufacturer() != null) item.setManufacturer(request.getManufacturer());
        if (request.getModelNumber() != null)  item.setModelNumber(request.getModelNumber());
        if (request.getDescription() != null)  item.setDescription(request.getDescription());
        if (request.getUnit() != null)         item.setUnit(request.getUnit());
        if (request.getIsActive() != null)     item.setIsActive(request.getIsActive());

        catalogRepository.save(item);

        return mapToResponse(item);
    }

    // ---------------------------------------- Delete ---------------------------------------- //

    @Transactional
    @Override
    public void deactivateItem(Long itemId) {

        User currentUser = currentUser();

        if (!Set.of(RoleType.ADMIN, RoleType.HOSPITAL_ADMIN).contains(currentUser.getRole())) {
            throw new ValidationException("Only admin can deactivate catalog items");
        }

        OTItemCatalog item = catalogRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        if (!item.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to deactivate this item");
        }

        if (!item.getIsActive()) {
            throw new ValidationException("Item is already inactive");
        }

        item.setIsActive(false);
        catalogRepository.save(item);
    }

    @Transactional
    @Override
    public void activateItem(Long itemId) {

        User currentUser = currentUser();

        if (!Set.of(RoleType.ADMIN, RoleType.HOSPITAL_ADMIN).contains(currentUser.getRole())) {
            throw new ValidationException("Only admin can activate catalog items");
        }

        OTItemCatalog item = catalogRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        if (item.getIsActive()) {
            throw new ValidationException("Item is already active");
        }

        item.setIsActive(true);
        catalogRepository.save(item);
    }

    @Transactional
    @Override
    public void hardDeleteItem(Long itemId) {

        User currentUser = currentUser();

        // Sirf SUPER_ADMIN hard delete kar sakta hai
        if (!currentUser.getRole().equals(RoleType.SUPER_ADMIN) && !currentUser.getRole().equals(RoleType.ADMIN) ) {
            throw new ValidationException("Only Super_Admin & Admin can permanently delete catalog items");
        }

        OTItemCatalog item = catalogRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        catalogRepository.delete(item);
    }

    // ---------------------------------------- Mapper ---------------------------------------- //

    private OTItemCatalogResponse mapToResponse(OTItemCatalog item) {
        return OTItemCatalogResponse.builder()
                .id(item.getId())
                .itemCode(item.getItemCode())
                .itemName(item.getItemName())
                .itemType(item.getItemType())
                .category(item.getCategory())
                .subCategory(item.getSubCategory())
                .manufacturer(item.getManufacturer())
                .modelNumber(item.getModelNumber())
                .description(item.getDescription())
                .unit(item.getUnit())
                .isActive(item.getIsActive())
                .createdBy(item.getCreatedBy())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
