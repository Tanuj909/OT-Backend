package com.ot.dto.otRoom;

import com.ot.enums.RoomStatus;
import com.ot.enums.RoomType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OTRoomCreateRequest {

    @NotNull
    private Long operationTheaterId;

    @NotBlank
    private String roomNumber;

    private String roomName;

    private String location;

    private Integer floor;

    private RoomType type;

    private RoomStatus status;

    private Boolean hasHvac;

    private Boolean hasGasSupply;

    private Boolean hasSuction;

    private Boolean hasEmergencyPower;

    private Integer capacity;

    private String specialFeatures;
}