package com.ot.dto.operationtheater;

import com.ot.enums.TheaterStatus;
import com.ot.enums.TheaterType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OperationTheaterResponse {

    private Long id;

    private String theaterNumber;

    private String name;

    private String location;

    private String building;

    private Integer floor;

    private TheaterType type;

    private TheaterStatus status;
}