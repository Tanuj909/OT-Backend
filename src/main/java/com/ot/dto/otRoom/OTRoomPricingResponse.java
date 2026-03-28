package com.ot.dto.otRoom;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OTRoomPricingResponse {

    private Long id;
    private Long roomId;

    private Double basePrice;
    private Double hourlyRate;
    private Double emergencyCharge;
    private Double cleaningCharge;

    private LocalDateTime createdAt;
}
