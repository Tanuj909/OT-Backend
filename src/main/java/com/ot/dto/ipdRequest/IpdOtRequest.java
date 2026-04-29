package com.ot.dto.ipdRequest;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class IpdOtRequest {

    private Long patientId;
    private Long admissionId;
    private String patientName;
    private LocalDateTime operationDate;
    private String procedureName;
    private Long hospitalId;
    private String complexity;

}
