package com.ot.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ot.billing.service.OTBillingIntegrationService;
import com.ot.dto.billing.OTItemBillingRequest;
import com.ot.dto.billing.OTItemBillingResponse;
import com.ot.dto.billing.OTItemBillingUpdateRequest;
import com.ot.dto.medication.MedicationUsageRequest;
import com.ot.dto.medication.MedicationUsageResponse;
import com.ot.entity.Hospital;
import com.ot.entity.MedicationUsage;
import com.ot.entity.OTItemCatalog;
import com.ot.entity.PriceCatalog;
import com.ot.entity.ScheduledOperation;
import com.ot.entity.User;
import com.ot.enums.CatalogItemType;
import com.ot.exception.ResourceNotFoundException;
import com.ot.exception.UnauthorizedException;
import com.ot.exception.ValidationException;
import com.ot.repository.MedicationUsageRepository;
import com.ot.repository.OTItemCatalogRepository;
import com.ot.repository.PriceCatalogRepository;
import com.ot.repository.ScheduledOperationRepository;
import com.ot.security.CustomUserDetails;
import com.ot.service.MedicationUsageService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicationUsageServiceImpl implements MedicationUsageService {

    private final MedicationUsageRepository medicationUsageRepository;
    private final ScheduledOperationRepository scheduledOperationRepository;
    private final OTBillingIntegrationService billingIntegrationService;
    private final OTItemCatalogRepository catalogRepository;
    private final PriceCatalogRepository priceCatalogRepository;

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
    public MedicationUsageResponse recordUsage(MedicationUsageRequest request) {

        User currentUser = currentUser();
        Hospital hospital = currentUser.getHospital();

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new ValidationException("Quantity must be greater than 0");
        }
        
        // Catalog item fetch
        OTItemCatalog catalogItem = catalogRepository.findById(request.getCatalogId())
                .orElseThrow(() -> new ResourceNotFoundException("Catalog item not found"));
        
        //Catalog Price
        PriceCatalog price = priceCatalogRepository
                .findByCatalogItem(catalogItem)
                .orElse(null);

        // Operation optional hai
        ScheduledOperation operation = null;
        if (request.getOperationId() != null) {
            operation = scheduledOperationRepository.findById(request.getOperationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

            if (!operation.getHospital().getId().equals(hospital.getId())) {
                throw new UnauthorizedException("You are not authorized to access this operation");
            }
        }

        MedicationUsage usage = MedicationUsage.builder()
                .hospital(hospital)
                .scheduledOperation(operation)
                .wardRoomId(request.getWardRoomId())
                .wardBedId(request.getWardBedId())
                .itemCode(catalogItem.getItemCode())
                .name(catalogItem.getItemName())
                .batchNumber(request.getBatchNumber())
                .category(catalogItem.getCategory())
                .type(catalogItem.getItemType().name())
                .quantity(request.getQuantity())
                .givenBy(currentUser.getUserName())
                .build();

        medicationUsageRepository.save(usage);
        
        if (operation.getBillingMasterId() == null) {
            throw new ValidationException("Billing not initialized for this operation");
        }
        
        OTItemBillingRequest billingRequest = new OTItemBillingRequest();
        billingRequest.setOperationExternalId(operation.getId());
        billingRequest.setItemExternalId(null); // optional
        billingRequest.setItemName(catalogItem.getItemName());
        billingRequest.setItemCode(catalogItem.getItemCode());
        billingRequest.setItemType(CatalogItemType.MEDICATION);
        billingRequest.setQuantity(request.getQuantity());
        billingRequest.setUnitPrice(price.getBasePrice());
        billingRequest.setDiscountPercent(price.getDiscountPercent());
        billingRequest.setGstPercent(price.getGstPercent());

        OTItemBillingResponse billingResponse =
                billingIntegrationService.addItemToBilling(billingRequest);

        // 🔥 STORE BILLING ITEM ID
        if (billingResponse != null) {
        	usage.setBillingItemId(billingResponse.getId());
        	medicationUsageRepository.save(usage); // update with billing id
        } else {
            log.warn("Item billing failed for operationId: {}", operation.getId());
        }

        return mapToResponse(usage);
    }
    
    // ----------------------------------------- Update ------------------------------------------ //
    @Transactional
    @Override
    public MedicationUsageResponse updateQuantity(Long id, Integer quantity) {

        User currentUser = currentUser();

        // Validation
        if (quantity == null || quantity <= 0) {
            throw new ValidationException("Quantity must be greater than 0");
        }

        // Fetch
        MedicationUsage usage = medicationUsageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medication usage not found"));

        // Hospital check
        if (!usage.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to update this record");
        }

        // Update quantity
        usage.setQuantity(quantity);

        // Save DB
        medicationUsageRepository.save(usage);

        // ==================== BILLING UPDATE ==================== //
        if (usage.getBillingItemId() != null) {

            OTItemBillingUpdateRequest billingRequest = new OTItemBillingUpdateRequest();
            billingRequest.setQuantity(quantity); // 🔥 only quantity update

            billingIntegrationService.updateItemInBilling(
                    usage.getBillingItemId(),
                    billingRequest
            );

        } else {
            log.warn("Billing item not found for medicationUsageId: {}", id);
        }

        return mapToResponse(usage);
    }
    
    // ----------------------------------------- Get ------------------------------------------ //

    @Override
    public MedicationUsageResponse getById(Long id) {

        User currentUser = currentUser();

        MedicationUsage usage = medicationUsageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medication usage not found"));

        if (!usage.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this record");
        }

        return mapToResponse(usage);
    }

    @Override
    public List<MedicationUsageResponse> getByOperation(Long operationId) {

        User currentUser = currentUser();

        return medicationUsageRepository
                .findByHospitalAndScheduledOperationId(currentUser.getHospital(), operationId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<MedicationUsageResponse> getByWardRoom(Long wardRoomId) {

        User currentUser = currentUser();

        return medicationUsageRepository
                .findByHospitalAndWardRoomId(currentUser.getHospital(), wardRoomId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<MedicationUsageResponse> getByWardBed(Long wardBedId) {

        User currentUser = currentUser();

        return medicationUsageRepository
                .findByHospitalAndWardBedId(currentUser.getHospital(), wardBedId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }
    
    
    // ----------------------------------------- Delete ------------------------------------------ //
    @Transactional
    @Override
    public void deleteById(Long id) {

        User currentUser = currentUser();

        MedicationUsage usage = medicationUsageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medication usage not found"));

        // 🔐 Hospital-level security (VERY IMPORTANT)
        if (!usage.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to delete this record");
        }
        
        // 🔥 Remove from billing first
        billingIntegrationService.removeItemFromBilling(usage.getBillingItemId());

        medicationUsageRepository.delete(usage);
    }

    // ---------------------------------------- Mapper ---------------------------------------- //

    private MedicationUsageResponse mapToResponse(MedicationUsage u) {
        return MedicationUsageResponse.builder()
                .id(u.getId())
                .operationId(u.getScheduledOperation() != null ? u.getScheduledOperation().getId() : null)
                .wardRoomId(u.getWardRoomId())
                .wardBedId(u.getWardBedId())
                .billingItemId(u.getBillingItemId())
                .itemCode(u.getItemCode())
                .name(u.getName())
                .batchNumber(u.getBatchNumber())
                .category(u.getCategory())
                .type(u.getType())
                .quantity(u.getQuantity())
                .givenBy(u.getGivenBy())
                .createdAt(u.getCreatedAt())
                .build();
    }
}