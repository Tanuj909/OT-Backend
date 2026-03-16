package com.ot.service;

import java.util.List;

import com.ot.dto.operationNotes.OperationAttributeRequest;
import com.ot.dto.operationNotes.OperationAttributeResponse;

public interface OperationAttributeService {
    OperationAttributeResponse addAttribute(Long operationId, OperationAttributeRequest request);
    List<OperationAttributeResponse> getAttributes(Long operationId);
    OperationAttributeResponse updateAttribute(Long operationId, Long attributeId, OperationAttributeRequest request);
    void deleteAttribute(Long operationId, Long attributeId);
}