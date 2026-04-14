package com.ot.dto.staffRequest;

import lombok.Data;

@Data
public class StaffFeeRequest {

    private Long staffId;
    private Double consultationFee;
    private Double otFee;
    private Double visitFee;
    private Double emergencyFee;
    
}