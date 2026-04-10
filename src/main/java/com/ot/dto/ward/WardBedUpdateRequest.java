package com.ot.dto.ward;

import lombok.Data;

@Data
public class WardBedUpdateRequest {

    private String bedNumber;
    private Boolean isActive;
}