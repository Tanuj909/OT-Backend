package com.ot.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.ot.enums.WardRoomType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "ward_rooms")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"ward", "hospital", "beds"})
@ToString(exclude = {"ward", "hospital", "beds"})
public class WardRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id", nullable = false)
    private Ward ward;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    private String roomNumber;          // "R-101"
    private String roomName;            // "Recovery Room 1"

    @Enumerated(EnumType.STRING)
    private WardRoomType roomType;      // GENERAL, PRIVATE, ICU, RECOVERY etc.

    private Integer totalBeds;
    private Integer occupiedBeds;
    private Integer availableBeds;

    // Pricing — per room alag hoga
    private Double ratePerHour;
    private Double discountPercent;
    private Double gstPercent;
    private String hsnCode;

    private Boolean isActive;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "wardRoom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WardBed> beds = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        isActive = true;
        occupiedBeds = 0;
        availableBeds = totalBeds != null ? totalBeds : 0; // 👈 create pe bhi set karo
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        availableBeds = totalBeds - occupiedBeds;
    }
}