package com.ot.dto.ward;

import java.time.LocalDateTime;

import com.ot.enums.BedStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WardBedResponse {

    private Long id;

    private Long wardRoomId;
    private String roomNumber;
    private String roomName;

    private Long wardId;
    private String wardNumber;
    private String wardName;

    private String bedNumber;
    private BedStatus status;

    private Boolean isActive;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}