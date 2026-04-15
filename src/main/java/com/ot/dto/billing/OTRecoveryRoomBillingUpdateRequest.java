package com.ot.dto.billing;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OTRecoveryRoomBillingUpdateRequest {

    private LocalDateTime startTime;
    private Double ratePerHour;
    private Double discountPercent;
    private Double gstPercent;
}