package com.ot.entity;

import lombok.*;
import java.time.LocalDateTime;
import com.ot.enums.AldreteScore;
import com.ot.enums.RecoveryStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "post_op_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"hospital", "scheduledOperation"})
@ToString(exclude = {"hospital", "scheduledOperation"})
public class PostOpRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;  // 👈 NEW FIELD
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_id")
    private ScheduledOperation scheduledOperation;
    
    private LocalDateTime surgeryEndTime;
    private LocalDateTime recoveryStartTime;
    private LocalDateTime recoveryEndTime;
    private String recoveryLocation;
    
    @Enumerated(EnumType.STRING)
    private AldreteScore aldreteScore;
    
    private String immediatePostOpCondition;
    private String painManagement;
    private String medicationsGiven;
    
    private String drainDetails;
    private String dressingDetails;
    
    @Column(length = 1000)
    private String postOpInstructions;
    
    private String followUpPlan;
    
    private String transferredTo;
    private String transferredBy;
    private String receivedBy;
    
    @Enumerated(EnumType.STRING)
    private RecoveryStatus status;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id")
    private Ward ward;
    
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