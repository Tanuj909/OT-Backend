package com.ot.dto.otRoom;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OTRoomFeatureResponse {
    private Long id;
    private String name;
    private Boolean isActive;
}