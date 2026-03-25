package com.ot.mapper;

import com.ot.dto.otRoom.OTRoomResponse;
import com.ot.entity.OTRoom;

public class OtRoomMapper {
	
	public static OTRoomResponse mapRoomToResponse(OTRoom room) {
	    return OTRoomResponse.builder()
	            .id(room.getId())
	            .roomNumber(room.getRoomNumber())
	            .roomName(room.getRoomName())
	            .location(room.getLocation())
	            .floor(room.getFloor())
	            .type(room.getType())
	            .status(room.getStatus())
	            .isActive(room.getIsActive())
	            .hasHvac(room.getHasHvac())
	            .hasGasSupply(room.getHasGasSupply())
	            .hasSuction(room.getHasSuction())
	            .hasEmergencyPower(room.getHasEmergencyPower())
	            .capacity(room.getCapacity())
	            .specialFeatures(room.getSpecialFeatures())
	            .build();
	}

}
