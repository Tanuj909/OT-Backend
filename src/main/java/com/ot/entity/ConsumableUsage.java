package com.ot.entity;

import lombok.*;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "consumable_usage")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"hospital", "scheduledOperation"})
@ToString(exclude = {"hospital", "scheduledOperation"})
public class ConsumableUsage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;  // 👈 NEW FIELD
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_id")
    private ScheduledOperation scheduledOperation;
    
    private Long billingItemId; //-> Billing kai Item ki ID!
    
    private String consumableCode;
    private String consumableName;
    private String category;
    
    private Integer quantityUsed;
    private Integer quantityWasted;
    private String unitOfMeasure;
    
    private String batchNumber;
    private LocalDateTime expiryDate;
    
    private String issuedBy;
    private String returnedBy;
    
    private Boolean isSterile;
    private LocalDateTime sterilizationDate;
    
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}