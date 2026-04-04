package com.ot.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ot.billing.service.OTBillingIntegrationService;
import com.ot.dto.billing.OTItemBillingRequest;
import com.ot.dto.implantUsed.ImplantUsedRequest;
import com.ot.dto.implantUsed.ImplantUsedResponse;
import com.ot.dto.implantUsed.ImplantUsedUpdateRequest;
import com.ot.entity.ImplantUsed;
import com.ot.entity.OTItemCatalog;
import com.ot.entity.PriceCatalog;
import com.ot.entity.ScheduledOperation;
import com.ot.entity.User;
import com.ot.enums.CatalogItemType;
import com.ot.enums.OperationStatus;
import com.ot.exception.ResourceNotFoundException;
import com.ot.exception.UnauthorizedException;
import com.ot.exception.ValidationException;
import com.ot.repository.ImplantUsedRepository;
import com.ot.repository.OTItemCatalogRepository;
import com.ot.repository.PriceCatalogRepository;
import com.ot.repository.ScheduledOperationRepository;
import com.ot.security.CustomUserDetails;
import com.ot.service.ImplantUsedService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImplantUsedServiceImpl implements ImplantUsedService {

    private final ImplantUsedRepository implantUsedRepository;
    private final ScheduledOperationRepository operationRepository;
    private final OTItemCatalogRepository catalogRepository;
    private final OTBillingIntegrationService billingIntegrationService;
    private final PriceCatalogRepository priceCatalogRepository;

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No user is logged in");
        }
        return ((CustomUserDetails) auth.getPrincipal()).getUser();
    }

    @Transactional
    @Override
    public ImplantUsedResponse addImplant(Long operationId, ImplantUsedRequest request) {

        User currentUser = currentUser();

        // 1. Operation fetch
        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        // 2. Hospital check
        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        // 3. Operation IN_PROGRESS check
        if (!operation.getStatus().equals(OperationStatus.IN_PROGRESS)) {
            throw new ValidationException("Implants can only be added to IN_PROGRESS operations");
        }

        // 4. Catalog item fetch
        OTItemCatalog catalogItem = catalogRepository.findById(request.getCatalogItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Catalog item not found"));

        // 5. Catalog item type check
        if (!catalogItem.getItemType().equals(CatalogItemType.IMPLANT)) {
            throw new ValidationException("Selected catalog item is not an implant");
        }

        // 6. Catalog item active check
        if (!catalogItem.getIsActive()) {
            throw new ValidationException("Selected catalog item is inactive");
        }
        
        //Catalog Price
        PriceCatalog price = priceCatalogRepository
                .findByCatalogItem(catalogItem)
                .orElse(null);

        // 7. Build and save
        ImplantUsed implant = ImplantUsed.builder()
                .scheduledOperation(operation)
                .hospital(operation.getHospital())
                .catalogItem(catalogItem)
                .serialNumber(request.getSerialNumber())
                .batchNumber(request.getBatchNumber())
                .quantity(request.getQuantity() != null ? request.getQuantity() : 1)
                .bodyLocation(request.getBodyLocation())
                .notes(request.getNotes())
                .usedBy(currentUser.getUserName())
                .build();

        implantUsedRepository.save(implant);
        
        if (operation.getBillingMasterId() == null) {
            throw new ValidationException("Billing not initialized for this operation");
        }
        
        OTItemBillingRequest billingRequest = new OTItemBillingRequest();
        billingRequest.setOperationExternalId(operation.getId());
        billingRequest.setItemExternalId(null); // optional
        billingRequest.setItemName(implant.getCatalogItem().getItemName());
        billingRequest.setItemCode(implant.getCatalogItem().getItemCode());
        billingRequest.setItemType(CatalogItemType.IMPLANT);
        billingRequest.setQuantity(request.getQuantity());
        billingRequest.setUnitPrice(price.getBasePrice());
        billingRequest.setDiscountPercent(price.getDiscountPercent());
        billingRequest.setGstPercent(price.getGstPercent());

        billingIntegrationService.addItemToBilling(billingRequest);

        return mapToResponse(implant);
    }

    @Override
    public List<ImplantUsedResponse> getImplants(Long operationId) {

        User currentUser = currentUser();

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        return implantUsedRepository.findByScheduledOperation(operation)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public ImplantUsedResponse updateImplant(Long operationId, Long implantId, ImplantUsedUpdateRequest request) {

        User currentUser = currentUser();

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        if (!operation.getStatus().equals(OperationStatus.IN_PROGRESS)) {
            throw new ValidationException("Implants can only be updated during IN_PROGRESS operations");
        }

        ImplantUsed implant = implantUsedRepository.findById(implantId)
                .orElseThrow(() -> new ResourceNotFoundException("Implant not found"));

        // Partial update
        if (request.getSerialNumber() != null)  implant.setSerialNumber(request.getSerialNumber());
        if (request.getBatchNumber() != null)   implant.setBatchNumber(request.getBatchNumber());
        if (request.getQuantity() != null)      implant.setQuantity(request.getQuantity());
        if (request.getBodyLocation() != null)  implant.setBodyLocation(request.getBodyLocation());
        if (request.getNotes() != null)         implant.setNotes(request.getNotes());

        implantUsedRepository.save(implant);

        return mapToResponse(implant);
    }

    @Transactional
    @Override
    public void removeImplant(Long operationId, Long implantId) {

        User currentUser = currentUser();

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        if (!operation.getStatus().equals(OperationStatus.IN_PROGRESS)) {
            throw new ValidationException("Implants can only be removed during IN_PROGRESS operations");
        }

        ImplantUsed implant = implantUsedRepository.findById(implantId)
                .orElseThrow(() -> new ResourceNotFoundException("Implant not found"));

        implantUsedRepository.delete(implant);
    }

    // Mapper
    private ImplantUsedResponse mapToResponse(ImplantUsed implant) {
        return ImplantUsedResponse.builder()
                .id(implant.getId())
                .operationId(implant.getScheduledOperation().getId())
                .catalogItemId(implant.getCatalogItem().getId())
                .itemCode(implant.getCatalogItem().getItemCode())
                .itemName(implant.getCatalogItem().getItemName())
                .manufacturer(implant.getCatalogItem().getManufacturer())
                .serialNumber(implant.getSerialNumber())
                .batchNumber(implant.getBatchNumber())
                .quantity(implant.getQuantity())
                .bodyLocation(implant.getBodyLocation())
                .notes(implant.getNotes())
                .usedBy(implant.getUsedBy())
                .usedAt(implant.getUsedAt())
                .createdAt(implant.getCreatedAt())
                .build();
    }
}