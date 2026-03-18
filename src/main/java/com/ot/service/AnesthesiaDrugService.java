package com.ot.service;

import java.util.List;

import com.ot.dto.anesthesiaDrug.AnesthesiaDrugRequest;
import com.ot.dto.anesthesiaDrug.AnesthesiaDrugResponse;
import com.ot.dto.anesthesiaDrug.AnesthesiaDrugSummaryResponse;
import com.ot.dto.anesthesiaDrug.AnesthesiaDrugUpdateRequest;

public interface AnesthesiaDrugService {
	
    AnesthesiaDrugResponse addDrug(Long operationId, AnesthesiaDrugRequest request);
    
    List<AnesthesiaDrugResponse> getDrugs(Long operationId);
    
    AnesthesiaDrugResponse updateDrug(Long operationId, Long drugId, AnesthesiaDrugUpdateRequest request);
    
    void removeDrug(Long operationId, Long drugId);
    
    AnesthesiaDrugSummaryResponse getDrugSummary(Long operationId);
}
