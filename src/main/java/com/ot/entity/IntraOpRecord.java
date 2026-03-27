package com.ot.entity;

import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.ot.enums.SurgeryStatus;
import com.ot.enums.VolumeUnit;
import jakarta.persistence.*;

@Entity
@Table(name = "intra_op_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"hospital", "scheduledOperation"})
@ToString(exclude = {"hospital", "scheduledOperation"})
public class IntraOpRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;  // 👈 NEW FIELD
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_id")
    private ScheduledOperation scheduledOperation;
    
    private LocalDateTime surgeryStartTime;
    private LocalDateTime surgeryEndTime;
    private LocalDateTime anesthesiaStartTime;
    private LocalDateTime anesthesiaEndTime;
    
    private String procedurePerformed;
    private String incisionType;
    private String woundClosure;
    
    private Integer bloodLoss;
    @Enumerated(EnumType.STRING)
    private VolumeUnit bloodLossUnit;
    
//    private String ivFluids;
//    private Integer ivFluidsVolume;
    
    @OneToMany(mappedBy = "intraOpRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IVFluidRecord> ivFluids = new ArrayList<>();
    
    @OneToMany(mappedBy = "intraOp", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AnesthesiaDrug> anesthesiaDrugs = new ArrayList<>();
    
    private String urineOutput;
    private String drainOutput;
    
    @Column(length = 1000)
    private String intraOpFindings;
    
    private String specimensCollected;
    private String implantsUsed;
    
    private String complications;
    private String interventions;
    
    @Enumerated(EnumType.STRING)
    private SurgeryStatus status;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    
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