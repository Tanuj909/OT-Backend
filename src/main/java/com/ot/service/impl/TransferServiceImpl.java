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
}
