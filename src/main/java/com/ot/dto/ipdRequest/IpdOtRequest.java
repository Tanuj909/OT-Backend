package com.ot.dto.ipdRequest;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class IpdOtRequest {

    private Long patientId;

    private String patientName;

    private String patientMrn;

    private Long ipdAdmissionId;
    
    private Long ipdHospitalId;

    private String procedureName;

    private String procedureCode;

    private String surgeonId;

    private String surgeonName;

    private LocalDateTime preferredDate;

    private String complexity;

}
