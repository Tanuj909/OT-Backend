package com.ot.dto.consumableUsage;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConsumableSummaryResponse {
    private Long operationId;
    private List<ConsumableUsageResponse> consumables;
    private Integer totalItemsUsed;
    private Integer totalItemsWasted;
    private Map<String, Integer> byCategory; // "SUTURES" -> 5
}