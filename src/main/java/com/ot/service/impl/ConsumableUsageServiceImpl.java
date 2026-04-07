package com.ot.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ot.billing.service.OTBillingIntegrationService;
import com.ot.dto.billing.OTItemBillingRequest;
import com.ot.dto.billing.OTItemBillingResponse;
import com.ot.dto.billing.OTItemBillingUpdateRequest;
import com.ot.dto.consumableUsage.ConsumableSummaryResponse;
import com.ot.dto.consumableUsage.ConsumableUsageRequest;
import com.ot.dto.consumableUsage.ConsumableUsageResponse;
import com.ot.entity.ConsumableUsage;
import com.ot.entity.ScheduledOperation;
import com.ot.entity.User;
import com.ot.enums.CatalogItemType;
import com.ot.enums.OperationStatus;
import com.ot.exception.ResourceNotFoundException;
import com.ot.exception.UnauthorizedException;
import com.ot.repository.ConsumableUsageRepository;
import com.ot.repository.ScheduledOperationRepository;
import com.ot.security.CustomUserDetails;
import com.ot.service.ConsumableUsageService;

import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumableUsageServiceImpl implements ConsumableUsageService {

    private final ConsumableUsageRepository consumableRepository;
    private final ScheduledOperationRepository operationRepository;
    private final OTBillingIntegrationService billingIntegrationService;

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No user is logged in");
        }
        return ((CustomUserDetails) auth.getPrincipal()).getUser();
    }

    // ---------------------------------------- Add ---------------------------------------- //

    @Transactional
    @Override
    public ConsumableUsageResponse addConsumable(Long operationId, ConsumableUsageRequest request) {

        User currentUser = currentUser();

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        // Hospital check
        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        // Status check
        if (!operation.getStatus().equals(OperationStatus.IN_PROGRESS)) {
            throw new ValidationException("Consumables can only be added for IN_PROGRESS operations");
        }

        // Expiry check
        if (request.getExpiryDate() != null && request.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Consumable " + request.getConsumableName() + " is expired");
        }

        // Quantity validation
        if (request.getQuantityUsed() == null || request.getQuantityUsed() <= 0) {
            throw new ValidationException("Quantity used must be greater than 0");
        }

        if (request.getQuantityWasted() != null && request.getQuantityWasted() < 0) {
            throw new ValidationException("Quantity wasted cannot be negative");
        }

        ConsumableUsage consumable = ConsumableUsage.builder()
                .hospital(operation.getHospital())
                .scheduledOperation(operation)
                .consumableCode(request.getConsumableCode())
                .consumableName(request.getConsumableName())
                .category(request.getCategory())
                .quantityUsed(request.getQuantityUsed())
                .quantityWasted(request.getQuantityWasted() != null ? request.getQuantityWasted() : 0)
                .unitOfMeasure(request.getUnitOfMeasure())
                .batchNumber(request.getBatchNumber())
                .expiryDate(request.getExpiryDate())
                .issuedBy(currentUser.getUserName())
                .isSterile(request.getIsSterile())
                .sterilizationDate(request.getSterilizationDate())
                .build();

        consumableRepository.save(consumable);
        
        if (operation.getBillingMasterId() == null) {
            throw new ValidationException("Billing not initialized for this operation");
        }
        
        OTItemBillingRequest billingRequest = new OTItemBillingRequest();
        billingRequest.setOperationExternalId(operation.getId());
        billingRequest.setItemExternalId(null); // optional
        billingRequest.setItemName(request.getConsumableName());
        billingRequest.setItemCode(request.getConsumableCode());
        billingRequest.setItemType(CatalogItemType.CONSUMABLE);
        billingRequest.setQuantity(request.getQuantityUsed());
        billingRequest.setUnitPrice(request.getUnitPrice());
        billingRequest.setDiscountPercent(request.getDiscountPercent());
        billingRequest.setGstPercent(request.getGstPercent());

        OTItemBillingResponse billingResponse =
                billingIntegrationService.addItemToBilling(billingRequest);

        // 🔥 STORE BILLING ITEM ID
        if (billingResponse != null) {
            consumable.setBillingItemId(billingResponse.getId());
            consumableRepository.save(consumable); // update with billing id
        } else {
            log.warn("Item billing failed for operationId: {}", operation.getId());
        }

        return mapToResponse(consumable);
    }

    // ---------------------------------------- Get All ---------------------------------------- //

    @Override
    public List<ConsumableUsageResponse> getConsumables(Long operationId) {

        User currentUser = currentUser();

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        return consumableRepository
                .findByScheduledOperationIdOrderByCreatedAtAsc(operationId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ---------------------------------------- Update ---------------------------------------- //

    @Transactional
    @Override
    public ConsumableUsageResponse updateConsumable(Long operationId, Long consumableId,
                                                     ConsumableUsageRequest request) {

        User currentUser = currentUser();

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        ConsumableUsage consumable = consumableRepository.findById(consumableId)
                .orElseThrow(() -> new ResourceNotFoundException("Consumable not found"));

        // Same operation check
        if (!consumable.getScheduledOperation().getId().equals(operationId)) {
            throw new ValidationException("Consumable does not belong to this operation");
        }

        // Partial update
        if (request.getQuantityUsed() != null)      consumable.setQuantityUsed(request.getQuantityUsed());
        if (request.getQuantityWasted() != null)    consumable.setQuantityWasted(request.getQuantityWasted());
        if (request.getBatchNumber() != null)       consumable.setBatchNumber(request.getBatchNumber());
        if (request.getUnitOfMeasure() != null)     consumable.setUnitOfMeasure(request.getUnitOfMeasure());
        if (request.getIsSterile() != null)         consumable.setIsSterile(request.getIsSterile());
        if (request.getSterilizationDate() != null) consumable.setSterilizationDate(request.getSterilizationDate());

        consumableRepository.save(consumable);
        
     // 🔥 Update item in billing
        if (consumable.getBillingItemId() != null) {

            OTItemBillingUpdateRequest billingRequest = new OTItemBillingUpdateRequest();

            billingRequest.setQuantity(consumable.getQuantityUsed());
//            billingRequest.setUnitPrice(request.getUnitPrice());
//            billingRequest.setDiscountPercent(request.getDiscountPercent());
//            billingRequest.setGstPercent(request.getGstPercent());

            billingIntegrationService.updateItemInBilling(
                    consumable.getBillingItemId(),
                    billingRequest
            );

        } else {
            log.warn("Billing item not found for consumableId: {}", consumableId);
        }

        return mapToResponse(consumable);
    }

    // ---------------------------------------- Return ---------------------------------------- //

    @Transactional
    @Override
    public ConsumableUsageResponse returnConsumable(Long operationId, Long consumableId) {

        User currentUser = currentUser();

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        ConsumableUsage consumable = consumableRepository.findById(consumableId)
                .orElseThrow(() -> new ResourceNotFoundException("Consumable not found"));

        if (!consumable.getScheduledOperation().getId().equals(operationId)) {
            throw new ValidationException("Consumable does not belong to this operation");
        }

        if (consumable.getReturnedBy() != null) {
            throw new ValidationException("Consumable already returned");
        }

        consumable.setReturnedBy(currentUser.getUserName());

        consumableRepository.save(consumable);

        return mapToResponse(consumable);
    }

    // ---------------------------------------- Delete ---------------------------------------- //

    @Transactional
    @Override
    public void deleteConsumable(Long operationId, Long consumableId) {

        User currentUser = currentUser();

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        ConsumableUsage consumable = consumableRepository.findById(consumableId)
                .orElseThrow(() -> new ResourceNotFoundException("Consumable not found"));

        if (!consumable.getScheduledOperation().getId().equals(operationId)) {
            throw new ValidationException("Consumable does not belong to this operation");
        }
        
        // 🔥 Remove from billing first
        billingIntegrationService.removeItemFromBilling(consumable.getBillingItemId());

        consumableRepository.delete(consumable);
    }

    // ---------------------------------------- Summary ---------------------------------------- //

    @Override
    public ConsumableSummaryResponse getConsumableSummary(Long operationId) {

        User currentUser = currentUser();

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        List<ConsumableUsage> consumables = consumableRepository
                .findByScheduledOperationIdOrderByCreatedAtAsc(operationId);

        // Total used
        int totalUsed = consumables.stream()
                .mapToInt(ConsumableUsage::getQuantityUsed)
                .sum();

        // Total wasted
        int totalWasted = consumables.stream()
                .mapToInt(c -> c.getQuantityWasted() != null ? c.getQuantityWasted() : 0)
                .sum();

        // By category
        Map<String, Integer> byCategory = consumables.stream()
                .filter(c -> c.getCategory() != null)
                .collect(Collectors.groupingBy(
                        ConsumableUsage::getCategory,
                        Collectors.summingInt(ConsumableUsage::getQuantityUsed)
                ));

        return ConsumableSummaryResponse.builder()
                .operationId(operationId)
                .consumables(consumables.stream().map(this::mapToResponse).collect(Collectors.toList()))
                .totalItemsUsed(totalUsed)
                .totalItemsWasted(totalWasted)
                .byCategory(byCategory)
                .build();
    }

    // ---------------------------------------- Mapper ---------------------------------------- //

    private ConsumableUsageResponse mapToResponse(ConsumableUsage consumable) {
        return ConsumableUsageResponse.builder()
                .id(consumable.getId())
                .operationId(consumable.getScheduledOperation().getId())
                .consumableCode(consumable.getConsumableCode())
                .consumableName(consumable.getConsumableName())
                .category(consumable.getCategory())
                .quantityUsed(consumable.getQuantityUsed())
                .quantityWasted(consumable.getQuantityWasted())
                .unitOfMeasure(consumable.getUnitOfMeasure())
                .batchNumber(consumable.getBatchNumber())
                .expiryDate(consumable.getExpiryDate())
                .issuedBy(consumable.getIssuedBy())
                .returnedBy(consumable.getReturnedBy())
                .isSterile(consumable.getIsSterile())
                .sterilizationDate(consumable.getSterilizationDate())
                .createdAt(consumable.getCreatedAt())
                .build();
    }
}