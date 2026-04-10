package com.ot.dto.ward;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WardRoomPricingResponse {
	
    private Long id;
    private Long wardRoomId;

    private Double basePrice;
    private Double hourlyRate;
    private Double emergencyCharge;
    private Double cleaningCharge;

    private LocalDateTime createdAt;

}
