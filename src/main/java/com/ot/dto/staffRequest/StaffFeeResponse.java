package com.ot.dto.staffRequest;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StaffFeeResponse {

    private Long id;

    private Long staffId;
    private String staffName;
    private String staffUserName;

    private Double consultationFee;
    private Double otFee;
    private Double visitFee;
    private Double emergencyFee;

    private Boolean isActive;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}