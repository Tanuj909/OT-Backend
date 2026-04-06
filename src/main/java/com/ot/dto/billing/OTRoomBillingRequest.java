package com.ot.dto.billing;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OTRoomBillingRequest {
	
    private Long operationExternalId;
    private String roomNumber;
    private String roomName;
    private LocalDateTime startTime;
    private Double ratePerHour;
    private Double discountPercent;
    private Double gstPercent;

}
