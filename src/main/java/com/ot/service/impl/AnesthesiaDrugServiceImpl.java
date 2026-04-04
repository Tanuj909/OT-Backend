package com.ot.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ot.billing.service.OTBillingIntegrationService;
import com.ot.dto.anesthesiaDrug.AnesthesiaDrugRequest;
import com.ot.dto.anesthesiaDrug.AnesthesiaDrugResponse;
import com.ot.dto.anesthesiaDrug.AnesthesiaDrugSummaryResponse;
import com.ot.dto.anesthesiaDrug.AnesthesiaDrugUpdateRequest;
import com.ot.dto.billing.OTItemBillingRequest;
import com.ot.entity.AnesthesiaDrug;
import com.ot.entity.IntraOpRecord;
import com.ot.entity.OTItemCatalog;
import com.ot.entity.PriceCatalog;
import com.ot.entity.ScheduledOperation;
import com.ot.entity.User;
import com.ot.enums.CatalogItemType;
import com.ot.enums.DrugType;
import com.ot.enums.OperationStatus;
import com.ot.enums.RoleType;
import com.ot.exception.ResourceNotFoundException;
import com.ot.exception.UnauthorizedException;
import com.ot.exception.ValidationException;
import com.ot.mapper.AnesthesiaDrugMapper;
import com.ot.repository.AnesthesiaDrugRepository;
import com.ot.repository.IntraOpRepository;
import com.ot.repository.OTItemCatalogRepository;
import com.ot.repository.PriceCatalogRepository;
import com.ot.repository.ScheduledOperationRepository;
import com.ot.security.CustomUserDetails;
import com.ot.service.AnesthesiaDrugService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnesthesiaDrugServiceImpl implements AnesthesiaDrugService {

    private final AnesthesiaDrugRepository drugRepository;
    private final ScheduledOperationRepository operationRepository;
    private final IntraOpRepository intraOpRepository;
	private final OTItemCatalogRepository catalogRepository;
    private final PriceCatalogRepository priceCatalogRepository;
	private final OTBillingIntegrationService billingIntegrationService;

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No user is logged in");
        }
        return ((CustomUserDetails) auth.getPrincipal()).getUser();
    }

    @Transactional
    @Override
    public AnesthesiaDrugResponse addDrug(Long operationId, AnesthesiaDrugRequest request) {

        User currentUser = currentUser();

        // 1. Role check — sirf ANESTHESIOLOGIST add kar sakta hai
        if (!currentUser.getRole().equals(RoleType.ANESTHESIOLOGIST)) {
            throw new ValidationException("Only anesthesiologist can add anesthesia drugs");
        }

        // 2. Operation fetch
        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        // 3. Hospital check
        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        // 4. Operation IN_PROGRESS check
        if (!operation.getStatus().equals(OperationStatus.IN_PROGRESS)) {
            throw new ValidationException("Drugs can only be added to IN_PROGRESS operations");
        }
        
        // Catalog item fetch
        OTItemCatalog catalogItem = catalogRepository.findById(request.getCatalogItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Catalog item not found"));
        
        // Catalog item type check
        if (!catalogItem.getItemType().equals(CatalogItemType.ANESTHESIA_DRUG)) {
            throw new ValidationException("Selected catalog item is not an implant");
        }

        // Catalog item active check
        if (!catalogItem.getIsActive()) {
            throw new ValidationException("Selected catalog item is inactive");
        }

        // 5. IntraOp exists check
        IntraOpRecord intraOp = operation.getIntraOp();
        if (intraOp == null) {
            throw new ResourceNotFoundException("IntraOp record not found for this operation");
        }
        
        //Catalog Price
        PriceCatalog price = priceCatalogRepository
                .findByCatalogItem(catalogItem)
                .orElse(null);

        // 6. Build and save
        AnesthesiaDrug drug = AnesthesiaDrug.builder()
                .intraOp(intraOp)
                .hospital(operation.getHospital())
                .drugName(request.getDrugName())
                .dose(request.getDose())
                .doseUnit(request.getDoseUnit())
                .route(request.getRoute())
                .drugType(request.getDrugType())
                .administeredAt(request.getAdministeredAt() != null
                        ? request.getAdministeredAt()
                        : LocalDateTime.now())
                .endTime(request.getEndTime())          // 👈 NEW
                .administeredBy(currentUser.getUserName())
                .notes(request.getNotes())
                .build();

        drugRepository.save(drug);
        
        if (operation.getBillingMasterId() == null) {
            throw new ValidationException("Billing not initialized for this operation");
        }
        
        OTItemBillingRequest billingRequest = new OTItemBillingRequest();
        billingRequest.setOperationExternalId(operation.getId());
        billingRequest.setItemExternalId(null); // optional
        billingRequest.setItemName(catalogItem.getItemName());
        billingRequest.setItemType(CatalogItemType.ANESTHESIA_DRUG);
//        billingRequest.setQuantity(request.getQuantityUsed());
        billingRequest.setUnitPrice(price.getBasePrice());
        billingRequest.setDiscountPercent(price.getDiscountPercent());
        billingRequest.setGstPercent(price.getGstPercent());

        billingIntegrationService.addItemToBilling(billingRequest);

        return AnesthesiaDrugMapper.mapToResponse(drug);
    }

    @Override
    public List<AnesthesiaDrugResponse> getDrugs(Long operationId) {

        User currentUser = currentUser();

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        IntraOpRecord intraOp = operation.getIntraOp();
        if (intraOp == null) {
            throw new ResourceNotFoundException("IntraOp record not found");
        }

        return drugRepository.findByIntraOp(intraOp)
                .stream()
                .map(AnesthesiaDrugMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public AnesthesiaDrugResponse updateDrug(Long operationId, Long drugId, AnesthesiaDrugUpdateRequest request) {

        User currentUser = currentUser();

        if (!currentUser.getRole().equals(RoleType.ANESTHESIOLOGIST) && !currentUser.getRole().equals(RoleType.ADMIN)) {
            throw new ValidationException("Only anesthesiologist can update anesthesia drugs");
        }

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        AnesthesiaDrug drug = drugRepository.findById(drugId)
                .orElseThrow(() -> new ResourceNotFoundException("Drug not found"));

        // Partial update
        if (request.getDose() != null)             drug.setDose(request.getDose());
        if (request.getDoseUnit() != null)         drug.setDoseUnit(request.getDoseUnit());
        if (request.getRoute() != null)            drug.setRoute(request.getRoute());
        if (request.getAdministeredAt() != null)   drug.setAdministeredAt(request.getAdministeredAt());
        if (request.getNotes() != null)            drug.setNotes(request.getNotes());
     // updateDrug — partial update mein
        if (request.getEndTime() != null) drug.setEndTime(request.getEndTime()); // 👈 NEW

        drugRepository.save(drug);

        return AnesthesiaDrugMapper.mapToResponse(drug);
    }

    @Transactional
    @Override
    public void removeDrug(Long operationId, Long drugId) {

        User currentUser = currentUser();

        if (!currentUser.getRole().equals(RoleType.ANESTHESIOLOGIST)) {
            throw new ValidationException("Only anesthesiologist can remove anesthesia drugs");
        }

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        if (!operation.getStatus().equals(OperationStatus.IN_PROGRESS)) {
            throw new ValidationException("Drugs can only be removed from IN_PROGRESS operations");
        }

        AnesthesiaDrug drug = drugRepository.findById(drugId)
                .orElseThrow(() -> new ResourceNotFoundException("Drug not found"));

        drugRepository.delete(drug);
    }

    @Override
    public AnesthesiaDrugSummaryResponse getDrugSummary(Long operationId) {

        User currentUser = currentUser();

        ScheduledOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
            throw new UnauthorizedException("You are not authorized to access this operation");
        }

        IntraOpRecord intraOp = operation.getIntraOp();
        if (intraOp == null) {
            throw new ResourceNotFoundException("IntraOp record not found");
        }

        List<AnesthesiaDrugResponse> drugs = drugRepository.findByIntraOp(intraOp)
                .stream()
                .map(AnesthesiaDrugMapper::mapToResponse)
                .collect(Collectors.toList());

        // Group by DrugType
        Map<DrugType, List<AnesthesiaDrugResponse>> byDrugType = drugs.stream()
                .collect(Collectors.groupingBy(AnesthesiaDrugResponse::getDrugType));

        return AnesthesiaDrugSummaryResponse.builder()
                .operationId(operationId)
                .drugs(drugs)
                .byDrugType(byDrugType)
                .totalDrugsAdministered(drugs.size())
                .build();
    }


}
