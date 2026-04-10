package com.ot.dto.ward;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WardAdmissionResponse {

    private Long id;
    private Long operationId;

    private Long wardRoomId;
    private String roomNumber;
    private String roomName;

    private Long wardBedId;
    private String bedNumber;

    private String patientId;
    private String patientName;
    private String patientMrn;

    private LocalDateTime admissionTime;
    private String admittedBy;

    private LocalDateTime dischargedWhen;
    private String dischargedBy;

    private LocalDateTime createdAt;
}