package com.ot.dto.staffRequest;


import lombok.Data;

@Data
public class StaffFeeUpdateRequest {

    private Double consultationFee;
    private Double otFee;
    private Double visitFee;
    private Double emergencyFee;
    private Boolean isActive;
}