package com.ot.dto.billing;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OTDoctorVisitBillingUpdateRequest {

    // Sirf fees aur visitTime update ho sakti hai
    private Double fees;
    private LocalDateTime visitTime;
}