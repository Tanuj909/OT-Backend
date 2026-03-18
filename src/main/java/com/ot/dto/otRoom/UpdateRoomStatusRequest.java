package com.ot.dto.otRoom;

import com.ot.enums.RoomStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateRoomStatusRequest {

    @NotNull
    private RoomStatus status;

    private String reason;
}