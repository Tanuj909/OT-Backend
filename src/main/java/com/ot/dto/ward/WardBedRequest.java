package com.ot.dto.ward;

import lombok.Data;

@Data
public class WardBedRequest {

    private Long wardRoomId;
    private String bedNumber;   // e.g. "B-101-1"
}