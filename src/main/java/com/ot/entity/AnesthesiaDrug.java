package com.ot.entity;

import java.time.LocalDateTime;

import com.ot.enums.DrugType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "anesthesia_drugs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnesthesiaDrug {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intra_op_id", nullable = false)
    private IntraOpRecord intraOp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    private String drugName;        // Propofol, Ketamine, etc.
    private Double dose;            // 200
    private String doseUnit;        // mg, mcg, ml
    private String route;           // IV, IM, Inhalation

    @Enumerated(EnumType.STRING)
    private DrugType drugType;      // INDUCTION, MAINTENANCE, REVERSAL, ANALGESIC

    private LocalDateTime administeredAt;
    private LocalDateTime endTime;   
    private String administeredBy;

    private String notes;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}