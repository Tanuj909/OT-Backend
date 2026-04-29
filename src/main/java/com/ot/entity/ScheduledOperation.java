package com.ot.entity;

import lombok.*;
import java.time.LocalDateTime;
import java.util.*;
import com.ot.embed.StaffAssignment;
import com.ot.embed.SurgeonAssignment;
import com.ot.enums.OperationStatus;
import com.ot.enums.ProcedureComplexity;
import jakarta.persistence.*;

@Entity
@Table(name = "scheduled_operations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"hospital", "operationTheater", "notes", "usedEquipment", "attributes", 
                              "vitalsLogs", "consumables", "preOp", "intraOp", "postOp"})
@ToString(exclude = {"hospital", "operationTheater", "notes", "usedEquipment", "attributes", 
                     "vitalsLogs", "consumables", "preOp", "intraOp", "postOp"})
public class ScheduledOperation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;  // 👈 NEW FIELD
    
    @Column(nullable = false)
    private String operationReference;  // Removed unique - now unique per hospital
    
//    private String patientId;
    private Long patientId;
    private String patientName;
    private String patientMrn;
    private String ipdAdmissionId;
    
    
    private Long billingMasterId;    ///-> Billing Master Table Id(Can fetch the Billing Details With this!)
    private Long billingDetailId;    ///-> Billing Master Table Id(Can fetch the Billing Details With this!)
    
    @Column(nullable = false)
    private String procedureName;
    
    private String procedureCode;
    
    @Enumerated(EnumType.STRING)
    private ProcedureComplexity complexity;
    
//    @Column(nullable = false)
    private LocalDateTime scheduledStartTime;
    
//    @Column(nullable = true)
    private LocalDateTime scheduledEndTime;
    
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    
    @Enumerated(EnumType.STRING)
    private OperationStatus status;
    
    private String primarySurgeonId;
    private String primarySurgeonName;
    
    private String anesthesiologistId;
    private String anesthesiologistName;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    
    @Column(name = "transfer_status")
    private String transferStatus; 
    // NONE / READY_FOR_IPD_TRANSFER / TRANSFERRED

    @Column(name = "transferred_to")
    private String transferredTo; 
    // IPD / ICU / DISCHARGED
    
    @Version
    private Long version;
    
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "operation_theater_id")
//    private OperationTheater operationTheater;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ot_room_id")
    private OTRoom room;
    
    @OneToMany(mappedBy = "scheduledOperation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<OperationNote> notes = new HashSet<>();
    
    @OneToMany(mappedBy = "scheduledOperation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UsedEquipment> usedEquipment = new HashSet<>();
    
    @OneToMany(mappedBy = "scheduledOperation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<OperationAttribute> attributes = new HashSet<>();
    
    @OneToMany(mappedBy = "scheduledOperation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<VitalsLog> vitalsLogs = new HashSet<>();
    
    @OneToMany(mappedBy = "scheduledOperation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ConsumableUsage> consumables = new HashSet<>();
    
    @OneToMany(mappedBy = "scheduledOperation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ImplantUsed> implants = new HashSet<>();
    
    @ElementCollection
    @CollectionTable(name = "operation_surgeons", 
                     joinColumns = @JoinColumn(name = "operation_id"))
    private Set<SurgeonAssignment> supportingSurgeons = new HashSet<>();
    
    @ElementCollection
    @CollectionTable(name = "operation_staff", 
                     joinColumns = @JoinColumn(name = "operation_id"))
    private Set<StaffAssignment> supportingStaff = new HashSet<>();
    
    @OneToOne(mappedBy = "scheduledOperation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PreOpAssessment preOp;
    
    @OneToOne(mappedBy = "scheduledOperation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private IntraOpRecord intraOp;
    
    @OneToOne(mappedBy = "scheduledOperation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PostOpRecord postOp;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}