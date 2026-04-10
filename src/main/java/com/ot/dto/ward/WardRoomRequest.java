package com.ot.dto.ward;

import com.ot.enums.WardRoomType;

import lombok.Data;

@Data
public class WardRoomRequest {
    private Long wardId;
    private String roomNumber;
    private String roomName;
    private WardRoomType roomType;
    private Integer totalBeds;
    private Double ratePerHour;
    private Double discountPercent;
    private Double gstPercent;
    private String hsnCode;
}