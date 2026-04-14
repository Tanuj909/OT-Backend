package com.ot.entity;

import lombok.*;
import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "medication_usage")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"hospital", "scheduledOperation"})
@ToString(exclude = {"hospital", "scheduledOperation"})
public class MedicationUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_id")
    private ScheduledOperation scheduledOperation;  // Optional

    private Long billingItemId;
    private Long wardRoomId;
    private Long wardBedId;

    private String itemCode;
    private String name;
    private String batchNumber;
    private String category;
    private String type;

    private Integer quantity;
    private String givenBy;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}