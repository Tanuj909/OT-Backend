package com.ot.dto.otRoom;

import com.ot.enums.RoomStatus;
import com.ot.enums.RoomType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OTRoomResponse {

    private Long id;

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

    private Long operationTheaterId;
}