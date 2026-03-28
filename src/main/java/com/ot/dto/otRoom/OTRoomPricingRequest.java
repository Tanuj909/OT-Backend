package com.ot.dto.otRoom;

import lombok.Data;

@Data
public class OTRoomPricingRequest {

    private Long roomId;

    private Double basePrice;
    private Double hourlyRate;
    private Double emergencyCharge;
    private Double cleaningCharge;
}