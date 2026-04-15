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
public class OTRecoveryRoomBillingRequest {

    private Long operationExternalId;

    private Long wardRoomId;
    private Long wardRoomBedId;
    private String wardRoomName;

    private LocalDateTime startTime;
    private Double ratePerHour;
    private Double discountPercent;
    private Double gstPercent;
}