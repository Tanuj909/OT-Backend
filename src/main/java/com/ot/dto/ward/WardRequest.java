package com.ot.dto.ward;

import com.ot.enums.WardType;

import lombok.Data;

@Data
public class WardRequest {
    private String wardNumber;
    private String wardName;
    private WardType wardType;
    private Integer totalBeds;
}