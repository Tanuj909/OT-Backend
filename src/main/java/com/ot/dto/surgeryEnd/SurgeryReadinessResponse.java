package com.ot.dto.surgeryEnd;

import com.ot.enums.OperationStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SurgeryReadinessResponse {

    private Long operationId;
    private OperationStatus currentStatus;

    private boolean canStart;
    private boolean canEnd;

    private SurgeryReadinessChecks checks;

    @Data
    @Builder
    public static class SurgeryReadinessChecks {

        // Surgery Start checks
        private boolean preOpCompleted;
        private boolean primarySurgeonAssigned;
        private boolean anesthesiologistAssigned;
        private boolean roomAssigned;

        // Surgery End checks
        private boolean intraOpExists;
        private boolean intraOpCompleted;
        private boolean woundClosureFilled;
        private boolean procedurePerformedFilled;
        private boolean bloodLossRecorded;
        private boolean allDrugsEndTimeSet;     // 👈 updated
        private boolean allEquipmentEndTimeSet;     // 👈 NEW
        private boolean allIVFluidsEndTimeSet;      // 👈 NEW
        private boolean vitalsRecorded;
    }
}