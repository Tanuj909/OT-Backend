package com.ot.service.impl;

import org.springframework.stereotype.Service;

import com.ot.billing.service.OTIpdIntegrationService;
import com.ot.entity.ScheduledOperation;
import com.ot.enums.OperationStatus;
import com.ot.repository.ScheduledOperationRepository;
import com.ot.service.TransferService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {
	
	private final ScheduledOperationRepository operationRepository;
	private final OTIpdIntegrationService ipdIntegrationService;
	
	@Override
	@Transactional
	public void markReadyForIpdTransfer(Long operationId) {

	    ScheduledOperation operation = operationRepository.findById(operationId)
	            .orElseThrow(() -> new RuntimeException("Operation not found"));

	    /* ================= VALIDATIONS ================= */

	    if (operation.getStatus() != OperationStatus.COMPLETED) {
	        throw new IllegalStateException("Operation not completed yet");
	    }

	    if ("READY_FOR_IPD_TRANSFER".equals(operation.getTransferStatus())) {
	        return; // idempotent
	    }

	    /* ================= UPDATE OT STATE ================= */

	    operation.setTransferStatus("READY_FOR_IPD_TRANSFER");
	    operation.setTransferredTo("IPD");

	    operationRepository.save(operation);

	    /* ================= CALL IPD ================= */
	 // 🔥 FEIGN CALL
	    ipdIntegrationService.notifyIpdForReturn(
	        operation.getPatientId(),
	        Long.valueOf(operation.getIpdAdmissionId())
	    );
	}
	
	
	@Override
	@Transactional
	public void markAcceptedByIpd(Long admissionId) {

	    /* ================= FETCH ================= */

	    ScheduledOperation operation = operationRepository
	            .findByIpdAdmissionId(String.valueOf(admissionId))
	            .orElseThrow(() -> new RuntimeException("Operation not found for this admission"));

	    /* ================= VALIDATIONS ================= */

	    if (!"READY_FOR_IPD_TRANSFER".equalsIgnoreCase(operation.getTransferStatus())) {
	        throw new IllegalStateException("Operation is not ready for transfer");
	    }

	    /* ================= IDEMPOTENT ================= */

	    if ("ACCEPTED_BY_IPD".equalsIgnoreCase(operation.getTransferStatus())) {
	        return;
	    }

	    /* ================= UPDATE ================= */

	    operation.setTransferStatus("ACCEPTED_BY_IPD");
	    operation.setTransferredTo("IPD");

	    operationRepository.save(operation);
	}
}
