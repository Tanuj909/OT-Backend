package com.ot.entity;

import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

import com.ot.enums.RoomStatus;
import com.ot.enums.RoomType;

import jakarta.persistence.*;

@Entity
@Table(name = "ot_rooms")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OTRoom {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;  // 👈 NEW FIELD
    
    private String roomNumber;
    private String roomName;
    private String location;
    private Integer floor;
    
    private String statusReason;
    private Boolean isActive;
    
    @Enumerated(EnumType.STRING)
    private RoomType type;
    
    @Enumerated(EnumType.STRING)
    private RoomStatus status;
    
    private Boolean hasHvac;
    private Boolean hasGasSupply;
    private Boolean hasSuction;
    private Boolean hasEmergencyPower;
    
    private Integer capacity;
    private String specialFeatures;
    
//    @OneToOne(mappedBy = "otRoom", fetch = FetchType.LAZY)
//    private OperationTheater operationTheater;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_theater_id", nullable = false)
    private OperationTheater operationTheater;
    
    @ManyToMany
    @JoinTable(
        name = "ot_room_feature_mapping",
        joinColumns = @JoinColumn(name = "room_id"),
        inverseJoinColumns = @JoinColumn(name = "feature_id")
    )
    private Set<OTRoomFeature> features;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
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