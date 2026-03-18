package com.ot.dto.operationtheater;

import com.ot.enums.TheaterType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OperationTheaterCreateRequest {

    @NotBlank
    private String theaterNumber;

    @NotBlank
    private String name;

    private String location;

    private String building;

    private Integer floor;

    private TheaterType type;
}