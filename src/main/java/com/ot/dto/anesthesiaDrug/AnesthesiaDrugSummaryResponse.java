package com.ot.dto.anesthesiaDrug;

import java.util.List;
import java.util.Map;

import com.ot.enums.DrugType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnesthesiaDrugSummaryResponse {
    private Long operationId;
    private List<AnesthesiaDrugResponse> drugs;
    private Map<DrugType, List<AnesthesiaDrugResponse>> byDrugType;
    private Integer totalDrugsAdministered;
}