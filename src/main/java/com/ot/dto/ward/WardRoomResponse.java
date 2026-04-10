package com.ot.dto.ward;

import java.time.LocalDateTime;

import com.ot.enums.WardRoomType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WardRoomResponse {
    private Long id;
    private Long wardId;
    private String wardNumber;
    private String wardName;
    private String roomNumber;
    private String roomName;
    private WardRoomType roomType;
    private Integer totalBeds;
    private Integer occupiedBeds;
    private Integer availableBeds;
    private Double ratePerHour;
    private Double discountPercent;
    private Double gstPercent;
    private String hsnCode;
    private Boolean isActive;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
