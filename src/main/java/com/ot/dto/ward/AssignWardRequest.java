package com.ot.dto.ward;

import lombok.Data;

@Data
public class AssignWardRequest {

    private Long operationId;
    private Long wardRoomId;
    private Long wardBedId;
}