package com.ot.dto.ipdRequest;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class IpdOtRequest {

    private Long patientId;

    private String patientName;

    private Long ipdAdmissionId;

    private String procedureName;

    private String surgeonId;

    private String surgeonName;

    private LocalDateTime preferredDate;

    private String complexity;

}
