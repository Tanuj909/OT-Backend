package com.ot.service;

import java.util.List;

import com.ot.dto.implantUsed.ImplantUsedRequest;
import com.ot.dto.implantUsed.ImplantUsedResponse;
import com.ot.dto.implantUsed.ImplantUsedUpdateRequest;

public interface ImplantUsedService {
    ImplantUsedResponse addImplant(Long operationId, ImplantUsedRequest request);
    List<ImplantUsedResponse> getImplants(Long operationId);
    ImplantUsedResponse updateImplant(Long operationId, Long implantId, ImplantUsedUpdateRequest request);
    void removeImplant(Long operationId, Long implantId);
}
