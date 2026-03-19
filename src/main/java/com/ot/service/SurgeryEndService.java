package com.ot.service;

import com.ot.dto.surgeryEnd.SurgeryEndRequest;
import com.ot.dto.surgeryEnd.SurgeryEndResponse;

public interface SurgeryEndService {
    SurgeryEndResponse endSurgery(Long operationId, SurgeryEndRequest request);
}
