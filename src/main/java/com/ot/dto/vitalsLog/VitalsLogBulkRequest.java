package com.ot.dto.vitalsLog;

import java.util.List;

import lombok.Data;

@Data
public class VitalsLogBulkRequest {
    private List<VitalsLogRequest> vitals;
}