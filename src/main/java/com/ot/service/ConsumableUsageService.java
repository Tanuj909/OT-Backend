package com.ot.service;

import java.util.List;

import com.ot.dto.consumableUsage.ConsumableSummaryResponse;
import com.ot.dto.consumableUsage.ConsumableUsageRequest;
import com.ot.dto.consumableUsage.ConsumableUsageResponse;

public interface ConsumableUsageService {

	ConsumableUsageResponse addConsumable(Long operationId, ConsumableUsageRequest request);

	List<ConsumableUsageResponse> getConsumables(Long operationId);

	ConsumableUsageResponse updateConsumable(Long operationId, Long consumableId, ConsumableUsageRequest request);

	ConsumableUsageResponse returnConsumable(Long operationId, Long consumableId);

	void deleteConsumable(Long operationId, Long consumableId);

	ConsumableSummaryResponse getConsumableSummary(Long operationId);

}
