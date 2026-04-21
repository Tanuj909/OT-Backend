package com.ot.entity;

import com.ot.enums.TaskStatus;
import com.ot.enums.TaskType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ward_tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"scheduledOperation", "wardAdmission"})
@ToString(exclude = {"scheduledOperation", "wardAdmission"})
public class WardTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_id", nullable = false)
    private ScheduledOperation scheduledOperation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_admission_id", nullable = false)
    private WardAdmission wardAdmission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    // Patient snapshot
    private String patientId;
    private String patientName;
    private String patientMrn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskType taskType;

    @Column(nullable = false)
    private String taskDescription;

    @Column(length = 1000)
    private String taskNotes;

    private LocalDateTime scheduledTime;

    @Builder.Default
    private Boolean isRecurring = false;

    private Integer intervalHours;

    private LocalDateTime recurringEndTime;  //Jo task Recurring hai, ye uska end time hai, ki kab end hoga!

    // Long — WardAdmission mai assignedStaffId ki tarah
    @Column(nullable = false)
    private Long assignedById;

    private String assignedByName;

    private LocalDateTime assignedAt;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;

    // Koi bhi complete kar sakta hai — no restriction
    private Long completedById;
    private String completedByName;
    private LocalDateTime completedAt;

    @Column(length = 1000)
    private String completionNotes;

    private String readingValue;
    private String readingUnit;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (assignedAt == null) assignedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}