//package com.ot.entity;
//
//import lombok.*;
//import java.time.LocalDateTime;
//import jakarta.persistence.*;
//
//@Entity
//@Table(name = "vitals_logs")
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//@EqualsAndHashCode(exclude = {"hospital", "scheduledOperation"})
//@ToString(exclude = {"hospital", "scheduledOperation"})
//public class VitalsLog {
//    
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//    
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "hospital_id", nullable = false)
//    private Hospital hospital;  // 👈 NEW FIELD
//    
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "operation_id")
//    private ScheduledOperation scheduledOperation;
//    
//    private LocalDateTime recordedTime;
//    private String recordedBy;
//    
//    private Integer heartRate;
//    private Integer systolicBp;
//    private Integer diastolicBp;
//    private Integer meanBp;
//    private Integer respiratoryRate;
//    private Double temperature;
//    private Integer oxygenSaturation;
//    private Integer etco2;
//    
//    private Integer painScale;
//    private String consciousness;
//    
//    private String sedationScore;
//    private String additionalNotes;
//    
//    private LocalDateTime createdAt;
//    
//    @PrePersist
//    protected void onCreate() {
//        createdAt = LocalDateTime.now();
//    }
//}

package com.ot.entity;

import lombok.*;
import java.time.LocalDateTime;

import com.ot.enums.VitalsPhase;

import jakarta.persistence.*;

@Entity
@Table(name = "vitals_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"hospital", "scheduledOperation", "ward"})
@ToString(exclude = {"hospital", "scheduledOperation", "ward"})
public class VitalsLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_id")
    private ScheduledOperation scheduledOperation;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "ward_id")                   // 👈 NEW
//    private Ward ward;
    
    private Long ward_room_id;
    private Long ward_bed_id;

    @Enumerated(EnumType.STRING)
    private VitalsPhase phase;                       // 👈 NEW — INTRA_OP ya POST_OP

    private LocalDateTime recordedTime;
    private String recordedBy;

    private Integer heartRate;
    private Integer systolicBp;
    private Integer diastolicBp;
    private Integer meanBp;
    private Integer respiratoryRate;
    private Double temperature;
    private Integer oxygenSaturation;
    private Integer etco2;

    private Integer painScale;
    private String consciousness;

    private String sedationScore;
    private Boolean isStable;                        // 👈 NEW — patient stable hai?
    private String additionalNotes;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}