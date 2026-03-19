package com.ot.dto.ward;

import java.time.LocalDateTime;
import com.ot.enums.WardType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WardResponse {
    private Long id;
    private String wardNumber;
    private String wardName;
    private WardType wardType;
    private Integer totalBeds;
    private Integer occupiedBeds;
    private Integer availableBeds;
    private Boolean isActive;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}