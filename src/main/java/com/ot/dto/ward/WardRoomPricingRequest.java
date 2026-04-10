package com.ot.dto.ward;

import lombok.Data;

@Data
public class WardRoomPricingRequest {
    private Long wardRoomId;

    private Double basePrice;
    private Double hourlyRate;
    private Double emergencyCharge;
    private Double cleaningCharge;
}
