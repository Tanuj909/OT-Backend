package com.ot.dto.opertaionReport;

import java.time.LocalDateTime;
import java.util.List;
import com.ot.enums.AldreteScore;
import com.ot.enums.AsaGrade;
import com.ot.enums.AssessmentStatus;
import com.ot.enums.DrugType;
import com.ot.enums.NpoStatus;
import com.ot.enums.OperationStatus;
import com.ot.enums.ProcedureComplexity;
import com.ot.enums.RecoveryStatus;
import com.ot.enums.StaffRole;
import com.ot.enums.SurgeonRole;
import com.ot.enums.SurgeryStatus;
import com.ot.enums.VitalsPhase;
import com.ot.enums.VolumeUnit;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OperationReportResponse {

    // ==================== Patient Info ==================== //
    private String patientId;
    private String patientName;
    private String patientMrn;
    private String ipdAdmissionId;

    // ==================== Operation Info ==================== //
    private Long operationId;
    private String operationReference;
    private String procedureName;
    private String procedureCode;
    private ProcedureComplexity complexity;
    private OperationStatus operationStatus;
    private LocalDateTime scheduledStartTime;
    private LocalDateTime scheduledEndTime;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private Long surgeryDurationMinutes;        // calculated
    private String roomNumber;
    private String roomName;

    // ==================== Team ==================== //
    private String primarySurgeon;
    private String anesthesiologist;
    private List<SurgeonTeamMember> supportingSurgeons;
    private List<StaffTeamMember> supportingStaff;

    // ==================== PreOp Summary ==================== //
    private PreOpSummary preOp;

    // ==================== IntraOp Summary ==================== //
    private IntraOpSummary intraOp;

    // ==================== PostOp Summary ==================== //
    private PostOpSummary postOp;

    // ==================== Nested Classes ==================== //

    @Data
    @Builder
    public static class SurgeonTeamMember {
        private Long surgeonId;
        private String surgeonName;
        private SurgeonRole role;
        private boolean isPrimary;
    }

    @Data
    @Builder
    public static class StaffTeamMember {
        private Long staffId;
        private String staffName;
        private StaffRole role;
    }

    @Data
    @Builder
    public static class PreOpSummary {
        private LocalDateTime assessmentDate;
        private String assessedBy;
        private Double height;
        private Double weight;
        private Double bmi;
        private String bloodGroup;
        private String allergies;
        private String currentMedications;
        private String pastMedicalHistory;
        private String pastSurgicalHistory;
        private String physicalExamination;
        private String ecgFindings;
        private String labResults;
        private String radiologyFindings;
        private AsaGrade asaGrade;
        private NpoStatus npoStatus;
        private String anesthesiaPlan;
        private String specialInstructions;
        private AssessmentStatus status;
    }

    @Data
    @Builder
    public static class IntraOpSummary {
        private String procedurePerformed;
        private String incisionType;
        private String woundClosure;
        private Integer bloodLoss;
        private VolumeUnit bloodLossUnit;
        private String urineOutput;
        private String drainOutput;
        private String intraOpFindings;
        private String specimensCollected;
        private String complications;
        private String interventions;
        private SurgeryStatus status;

        // IV Fluids
        private List<IVFluidSummary> ivFluids;
        private Integer totalIVFluidsML;

        // Anesthesia Drugs
        private List<AnesthesiaDrugSummary> anesthesiaDrugs;

        // Vitals
        private List<VitalsSummary> vitals;

        // Equipment
        private List<EquipmentSummary> equipment;

        // Consumables
        private List<ConsumableSummary> consumables;

        // Implants
        private List<ImplantSummary> implants;
    }

    @Data
    @Builder
    public static class PostOpSummary {
        private LocalDateTime surgeryEndTime;
        private LocalDateTime recoveryStartTime;
        private LocalDateTime recoveryEndTime;
        private String recoveryLocation;
        private AldreteScore aldreteScore;
        private String immediatePostOpCondition;
        private String painManagement;
        private String medicationsGiven;
        private String drainDetails;
        private String dressingDetails;
        private String postOpInstructions;
        private String followUpPlan;
        private String transferredTo;
        private String transferredBy;
        private String receivedBy;
        private RecoveryStatus status;
    }

    @Data
    @Builder
    public static class IVFluidSummary {
        private String fluidType;
        private Integer volume;
        private VolumeUnit unit;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String administeredBy;
    }

    @Data
    @Builder
    public static class AnesthesiaDrugSummary {
        private String drugName;
        private Double dose;
        private String doseUnit;
        private String route;
        private DrugType drugType;
        private LocalDateTime administeredAt;
        private LocalDateTime endTime;
        private String administeredBy;
    }

    @Data
    @Builder
    public static class VitalsSummary {
        private LocalDateTime recordedTime;
        private String recordedBy;
        private Integer heartRate;
        private Integer systolicBp;
        private Integer diastolicBp;
        private Integer respiratoryRate;
        private Double temperature;
        private Integer oxygenSaturation;
        private Integer painScale;
        private VitalsPhase phase;
    }

    @Data
    @Builder
    public static class EquipmentSummary {
        private String equipmentName;
        private String equipmentCode;
        private String manufacturer;        // 👈 add karo — useful hai
        private String serialNumber;        // 👈 add karo — traceability
        private Integer quantityUsed;       // 👈 add karo
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String usedBy;
    }

    @Data
    @Builder
    public static class ConsumableSummary {
        private String itemName;
        private String category;            // 👈 add karo
        private String consumableCode;      // 👈 add karo
        private Integer quantity;
        private Integer quantityWasted;     // 👈 add karo
        private String unit;
        private String batchNumber;         // 👈 add karo — traceability
        private String usedBy;
    }

    @Data
    @Builder
    public static class ImplantSummary {
        private String itemName;
        private String manufacturer;
        private String serialNumber;
        private String batchNumber;
        private Integer quantity;
        private String bodyLocation;
        private String usedBy;
    }
}
