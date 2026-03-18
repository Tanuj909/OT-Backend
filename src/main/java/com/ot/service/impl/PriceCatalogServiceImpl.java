package com.ot.service.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ot.dto.priceCatalog.PriceCatalogRequest;
import com.ot.dto.priceCatalog.PriceCatalogResponse;
import com.ot.dto.priceCatalog.PriceCatalogUpdateRequest;
import com.ot.entity.Hospital;
import com.ot.entity.OTItemCatalog;
import com.ot.entity.PriceCatalog;
import com.ot.entity.User;
import com.ot.enums.CatalogItemType;
import com.ot.enums.RoleType;
import com.ot.exception.ResourceNotFoundException;
import com.ot.exception.UnauthorizedException;
import com.ot.exception.ValidationException;
import com.ot.repository.OTItemCatalogRepository;
import com.ot.repository.PriceCatalogRepository;
import com.ot.security.CustomUserDetails;
import com.ot.service.PriceCatalogService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PriceCatalogServiceImpl implements PriceCatalogService {

    private final PriceCatalogRepository priceCatalogRepository;
    private final OTItemCatalogRepository catalogRepository;

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No user is logged in");
        }
        return ((CustomUserDetails) auth.getPrincipal()).getUser();
    }

    @Transactional
    @Override
    public PriceCatalogResponse createPrice(PriceCatalogRequest request) {

        User currentUser = currentUser();

        // Role check
        if (!Set.of(RoleType.ADMIN, RoleType.HOSPITAL_ADMIN).contains(currentUser.getRole())) {
            throw new ValidationException("Only admin can create price entries");
        }

        // Catalog item fetch
        OTItemCatalog catalogItem = catalogRepository.findById(request.getCatalogItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Catalog item not found"));

        // Same hospital check
        if (!catalogItem.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this item");
        }

        // Already exists check — OneToOne
        if (priceCatalogRepository.existsByCatalogItem(catalogItem)) {
            throw new ValidationException("Price already exists for this catalog item");
        }

        PriceCatalog price = PriceCatalog.builder()
                .hospital(currentUser.getHospital())
                .catalogItem(catalogItem)
                .hsnCode(request.getHsnCode())
                .basePrice(request.getBasePrice())
                .discountPercent(request.getDiscountPercent())
                .gstPercent(request.getGstPercent())
                .createdBy(currentUser.getUserName())
                .build();

        priceCatalogRepository.save(price);

        return mapToResponse(price);
    }

    @Override
    public PriceCatalogResponse getPriceByCatalogItemId(Long catalogItemId) {

        User currentUser = currentUser();

        PriceCatalog price = priceCatalogRepository.findByCatalogItemId(catalogItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Price not found for this catalog item"));

        if (!price.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this price");
        }

        return mapToResponse(price);
    }

    @Override
    public PriceCatalogResponse getPriceById(Long priceId) {

        User currentUser = currentUser();

        PriceCatalog price = priceCatalogRepository.findById(priceId)
                .orElseThrow(() -> new ResourceNotFoundException("Price not found"));

        if (!price.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this price");
        }

        return mapToResponse(price);
    }

    @Override
    public List<PriceCatalogResponse> getAllPrices(Boolean isActive, CatalogItemType itemType) {

        User currentUser = currentUser();
        Hospital hospital = currentUser.getHospital();

        if (itemType != null) {
            return priceCatalogRepository
                    .findByHospitalAndCatalogItemItemType(hospital, itemType)
                    .stream().map(this::mapToResponse).collect(Collectors.toList());
        } else if (isActive != null) {
            return priceCatalogRepository
                    .findByHospitalAndIsActive(hospital, isActive)
                    .stream().map(this::mapToResponse).collect(Collectors.toList());
        }

        return priceCatalogRepository.findAll()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public PriceCatalogResponse updatePrice(Long priceId, PriceCatalogUpdateRequest request) {

        User currentUser = currentUser();

        if (!Set.of(RoleType.ADMIN, RoleType.HOSPITAL_ADMIN).contains(currentUser.getRole())) {
            throw new ValidationException("Only admin can update price entries");
        }

        PriceCatalog price = priceCatalogRepository.findById(priceId)
                .orElseThrow(() -> new ResourceNotFoundException("Price not found"));

        if (!price.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to update this price");
        }

        // Partial update
        if (request.getHsnCode() != null)        price.setHsnCode(request.getHsnCode());
        if (request.getBasePrice() != null)      price.setBasePrice(request.getBasePrice());
        if (request.getDiscountPercent() != null) price.setDiscountPercent(request.getDiscountPercent());
        if (request.getGstPercent() != null)     price.setGstPercent(request.getGstPercent());
        if (request.getIsActive() != null)       price.setIsActive(request.getIsActive());

        price.setUpdatedBy(currentUser.getUserName());

        priceCatalogRepository.save(price);

        return mapToResponse(price);
    }

    @Transactional
    @Override
    public void deactivatePrice(Long priceId) {

        User currentUser = currentUser();

        if (!Set.of(RoleType.ADMIN, RoleType.HOSPITAL_ADMIN).contains(currentUser.getRole())) {
            throw new ValidationException("Only admin can deactivate price entries");
        }

        PriceCatalog price = priceCatalogRepository.findById(priceId)
                .orElseThrow(() -> new ResourceNotFoundException("Price not found"));

        if (!price.getIsActive()) {
            throw new ValidationException("Price is already inactive");
        }

        price.setIsActive(false);
        priceCatalogRepository.save(price);
    }

    @Transactional
    @Override
    public void activatePrice(Long priceId) {

        User currentUser = currentUser();

        if (!Set.of(RoleType.ADMIN, RoleType.HOSPITAL_ADMIN).contains(currentUser.getRole())) {
            throw new ValidationException("Only admin can activate price entries");
        }

        PriceCatalog price = priceCatalogRepository.findById(priceId)
                .orElseThrow(() -> new ResourceNotFoundException("Price not found"));

        if (price.getIsActive()) {
            throw new ValidationException("Price is already active");
        }

        price.setIsActive(true);
        priceCatalogRepository.save(price);
    }

    // Mapper
    private PriceCatalogResponse mapToResponse(PriceCatalog price) {
        return PriceCatalogResponse.builder()
                .id(price.getId())
                .catalogItemId(price.getCatalogItem().getId())
                .itemCode(price.getCatalogItem().getItemCode())
                .itemName(price.getCatalogItem().getItemName())
                .itemType(price.getCatalogItem().getItemType())
                .hsnCode(price.getHsnCode())
                .basePrice(price.getBasePrice())
                .discountPercent(price.getDiscountPercent())
                .discountAmount(price.getDiscountAmount())
                .priceAfterDiscount(price.getPriceAfterDiscount())
                .gstPercent(price.getGstPercent())
                .gstAmount(price.getGstAmount())
                .totalPrice(price.getTotalPrice())
                .isActive(price.getIsActive())
                .createdBy(price.getCreatedBy())
                .updatedBy(price.getUpdatedBy())
                .createdAt(price.getCreatedAt())
                .updatedAt(price.getUpdatedAt())
                .build();
    }
}