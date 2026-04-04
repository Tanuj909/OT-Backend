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
import com.ot.dto.iVFluid.IVFluidRequest;
import com.ot.dto.iVFluid.IVFluidResponse;
import com.ot.dto.iVFluid.IVFluidSummaryResponse;
import com.ot.dto.iVFluid.IVFluidUpdateRequest;
import com.ot.entity.IVFluidRecord;
import com.ot.entity.IntraOpRecord;
import com.ot.entity.OTItemCatalog;
import com.ot.entity.PriceCatalog;
import com.ot.entity.ScheduledOperation;
import com.ot.entity.User;
import com.ot.enums.CatalogItemType;
import com.ot.enums.OperationStatus;
import com.ot.enums.VolumeUnit;
import com.ot.exception.ResourceNotFoundException;
import com.ot.exception.UnauthorizedException;
import com.ot.mapper.IVFluidMapper;
import com.ot.repository.IvFluidRepository;
import com.ot.repository.OTItemCatalogRepository;
import com.ot.repository.PriceCatalogRepository;
import com.ot.repository.ScheduledOperationRepository;
import com.ot.security.CustomUserDetails;
import com.ot.service.IvFluidService;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IvFluidServiceImpl implements IvFluidService{
	
	private final ScheduledOperationRepository operationRepository;
	private final IvFluidRepository ivFluidRepository;
	private final OTBillingIntegrationService billingIntegrationService;
	private final OTItemCatalogRepository catalogRepository;
    private final PriceCatalogRepository priceCatalogRepository;
	
	public User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No user is logged in");
        }
        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal(); 
        return cud.getUser(); 
    }
	
	
	@Transactional
	@Override
	public IVFluidResponse addIVFluid(Long operationId, IVFluidRequest request) {

	    User currentUser = currentUser();

	    // 1. Operation fetch
	    ScheduledOperation operation = operationRepository.findById(operationId)
	            .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

	    // 2. Same hospital check
	    if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
	        throw new UnauthorizedException("You are not authorized to access this operation");
	    }

	    // 3. Operation IN_PROGRESS check
	    if (!operation.getStatus().equals(OperationStatus.IN_PROGRESS)) {
	        throw new ValidationException("IV fluids can only be added to IN_PROGRESS operations");
	    }

	    
        // Catalog item fetch
        OTItemCatalog catalogItem = catalogRepository.findById(request.getCatalogItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Catalog item not found"));
        
        // Catalog item type check
        if (!catalogItem.getItemType().equals(CatalogItemType.IV_FLUID)) {
            throw new ValidationException("Selected catalog item is not an implant");
        }

        // Catalog item active check
        if (!catalogItem.getIsActive()) {
            throw new ValidationException("Selected catalog item is inactive");
        }
        
	    // 4. IntraOp exists check
	    IntraOpRecord intraOp = operation.getIntraOp();
	    if (intraOp == null) {
	        throw new ResourceNotFoundException("IntraOp record not found — create it first");
	    }

	    // 5. Validation
	    if (request.getFluidType() == null || request.getFluidType().isBlank()) {
	        throw new ValidationException("Fluid type cannot be empty");
	    }
	    if (request.getVolume() == null || request.getVolume() <= 0) {
	        throw new ValidationException("Volume must be greater than 0");
	    }
	    if (request.getUnit() == null) {
	        throw new ValidationException("Unit cannot be null");
	    }
	    
        //Catalog Price
        PriceCatalog price = priceCatalogRepository
                .findByCatalogItem(catalogItem)
                .orElse(null);

	    // 6. Build and save
	    IVFluidRecord ivFluid = IVFluidRecord.builder()
	            .hospital(operation.getHospital())
	            .intraOpRecord(intraOp)
	            .fluidType(request.getFluidType())
	            .volume(request.getVolume())
	            .unit(request.getUnit())
	            .startTime(request.getStartTime() != null ? request.getStartTime() : LocalDateTime.now())
	            .endTime(request.getEndTime())
	            .administeredBy(currentUser.getUserName())
	            .build();

	    ivFluidRepository.save(ivFluid);
	    
        if (operation.getBillingMasterId() == null) {
            throw new ValidationException("Billing not initialized for this operation");
        }
        
        OTItemBillingRequest billingRequest = new OTItemBillingRequest();
        billingRequest.setOperationExternalId(operation.getId());
        billingRequest.setItemExternalId(null); // optional
        billingRequest.setItemName(request.getIvFluidName());
        billingRequest.setItemType(CatalogItemType.IMPLANT);
//        billingRequest.setQuantity(request.getQuantityUsed());
        billingRequest.setUnitPrice(price.getBasePrice());
        billingRequest.setDiscountPercent(price.getDiscountPercent());
        billingRequest.setGstPercent(price.getGstPercent());

        billingIntegrationService.addItemToBilling(billingRequest);

	    return IVFluidMapper.toResponse(ivFluid);
	}

	@Override
	public List<IVFluidResponse> getIVFluids(Long operationId) {

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

	    return intraOp.getIvFluids().stream()
	            .map(IVFluidMapper::toResponse)
	            .collect(Collectors.toList());
	}

	@Transactional
	@Override
	public void removeIVFluid(Long operationId, Long fluidId) {

	    User currentUser = currentUser();

	    ScheduledOperation operation = operationRepository.findById(operationId)
	            .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

	    if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
	        throw new UnauthorizedException("You are not authorized to access this operation");
	    }

	    // Operation IN_PROGRESS check — complete hone ke baad remove nahi kar sakte
	    if (!operation.getStatus().equals(OperationStatus.IN_PROGRESS)) {
	        throw new ValidationException("IV fluids can only be removed from IN_PROGRESS operations");
	    }

	    IVFluidRecord ivFluid = ivFluidRepository.findById(fluidId)
	            .orElseThrow(() -> new ResourceNotFoundException("IV fluid record not found"));

	    // Belong to same operation check
	    if (!ivFluid.getIntraOpRecord().getScheduledOperation().getId().equals(operationId)) {
	        throw new ValidationException("IV fluid does not belong to this operation");
	    }

	    ivFluidRepository.delete(ivFluid);
	}
	
	@Transactional
	@Override
	public IVFluidResponse updateIVFluid(Long operationId, Long fluidId, IVFluidUpdateRequest request) {

	    User currentUser = currentUser();

	    ScheduledOperation operation = operationRepository.findById(operationId)
	            .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

	    if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
	        throw new UnauthorizedException("You are not authorized to access this operation");
	    }

	    if (!operation.getStatus().equals(OperationStatus.IN_PROGRESS)) {
	        throw new ValidationException("IV fluids can only be updated for IN_PROGRESS operations");
	    }

	    IVFluidRecord ivFluid = ivFluidRepository.findById(fluidId)
	            .orElseThrow(() -> new ResourceNotFoundException("IV fluid record not found"));

	    // Belong to same operation check
	    if (!ivFluid.getIntraOpRecord().getScheduledOperation().getId().equals(operationId)) {
	        throw new ValidationException("IV fluid does not belong to this operation");
	    }

	    // Already completed check
	    if (ivFluid.getEndTime() != null && ivFluid.getEndTime().isBefore(LocalDateTime.now())) {
	        throw new ValidationException("Cannot update — IV fluid administration already completed");
	    }

	    // endTime before startTime check
	    if (request.getEndTime() != null && request.getEndTime().isBefore(ivFluid.getStartTime())) {
	        throw new ValidationException("End time cannot be before start time");
	    }

	    // Volume must be positive
	    if (request.getVolume() != null && request.getVolume() <= 0) {
	        throw new ValidationException("Volume must be greater than 0");
	    }

	    if (request.getVolume() != null)  ivFluid.setVolume(request.getVolume());
	    if (request.getUnit() != null)    ivFluid.setUnit(request.getUnit());
	    if (request.getEndTime() != null) ivFluid.setEndTime(request.getEndTime());

	    ivFluidRepository.save(ivFluid);

	    return IVFluidMapper.toResponse(ivFluid);
	}
	
	@Transactional
	@Override
	public IVFluidResponse completeIVFluid(Long operationId, Long fluidId) {

	    User currentUser = currentUser();

	    ScheduledOperation operation = operationRepository.findById(operationId)
	            .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

	    if (!operation.getHospital().getId().equals(currentUser.getHospital().getId())) {
	        throw new UnauthorizedException("You are not authorized to access this operation");
	    }

	    IVFluidRecord ivFluid = ivFluidRepository.findById(fluidId)
	            .orElseThrow(() -> new ResourceNotFoundException("IV fluid record not found"));

	    if (!ivFluid.getIntraOpRecord().getScheduledOperation().getId().equals(operationId)) {
	        throw new ValidationException("IV fluid does not belong to this operation");
	    }

	    // Already completed check
	    if (ivFluid.getEndTime() != null) {
	        throw new ValidationException("IV fluid administration already completed at: " + ivFluid.getEndTime());
	    }

	    ivFluid.setEndTime(LocalDateTime.now());

	    ivFluidRepository.save(ivFluid);

	    return IVFluidMapper.toResponse(ivFluid);
	}
	
	@Override
	public IVFluidSummaryResponse getIVFluidSummary(Long operationId) {

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

	    List<IVFluidRecord> fluids = intraOp.getIvFluids();

	    // Total volume in ML
	    int totalVolumeML = fluids.stream()
	            .mapToInt(f -> f.getUnit().equals(VolumeUnit.LITERS) ? f.getVolume() * 1000 : f.getVolume())
	            .sum();

	    // Group by fluid type
	    Map<String, Integer> byFluidType = fluids.stream()
	            .collect(Collectors.groupingBy(
	                IVFluidRecord::getFluidType,
	                Collectors.summingInt(f -> f.getUnit().equals(VolumeUnit.LITERS) ? f.getVolume() * 1000 : f.getVolume())
	            ));

	    // Ongoing vs completed
	    int ongoingCount   = (int) fluids.stream().filter(f -> f.getEndTime() == null).count();
	    int completedCount = (int) fluids.stream().filter(f -> f.getEndTime() != null).count();

	    return IVFluidSummaryResponse.builder()
	            .operationId(operationId)
	            .fluids(fluids.stream().map(IVFluidMapper::toResponse).collect(Collectors.toList()))
	            .totalVolumeML(totalVolumeML)
	            .byFluidType(byFluidType)
	            .ongoingFluidsCount(ongoingCount)
	            .completedFluidsCount(completedCount)
	            .build();
	}
}
