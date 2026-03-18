package com.ot.entity;

import lombok.*;
import java.time.LocalDateTime;
import com.ot.enums.ScheduleType;
import jakarta.persistence.*;

@Entity
@Table(name = "ot_schedules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"hospital", "operationTheater"})
@ToString(exclude = {"hospital", "operationTheater"})
public class OTSchedule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;  // 👈 NEW FIELD
    
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "theater_id")
//    private OperationTheater operationTheater;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ot_room_id")
    private OTRoom room;
    
    private LocalDateTime scheduleDate;
    private String dayOfWeek;
    private String timeSlot;
    
    @Enumerated(EnumType.STRING)
    private ScheduleType type;
    
    private String blockedBy;
    private String blockReason;
    private Boolean isRecurring;
    private String recurringPattern;
    
    private LocalDateTime startTime;

    private LocalDateTime endTime;
    
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