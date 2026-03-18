package com.ot.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "price_catalog")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalog_item_id", nullable = false, unique = true)
    private OTItemCatalog catalogItem;  // OneToOne link

    private String hsnCode;             // GST ke liye

    private BigDecimal basePrice;
    private BigDecimal discountPercent;
    private BigDecimal discountAmount;      // auto calculated
    private BigDecimal priceAfterDiscount;  // auto calculated
    private BigDecimal gstPercent;
    private BigDecimal gstAmount;           // auto calculated
    private BigDecimal totalPrice;          // final — auto calculated

    private Boolean isActive;

    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        isActive = true;
        calculatePrices();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculatePrices();
    }

    private void calculatePrices() {
        if (basePrice == null) return;

        // Step 1 — Discount
        BigDecimal effectiveDiscount = discountPercent != null ? discountPercent : BigDecimal.ZERO;
        this.discountAmount = basePrice.multiply(effectiveDiscount)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        this.priceAfterDiscount = basePrice.subtract(discountAmount);

        // Step 2 — GST on discounted price
        BigDecimal effectiveGst = gstPercent != null ? gstPercent : BigDecimal.ZERO;
        this.gstAmount = priceAfterDiscount.multiply(effectiveGst)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Step 3 — Final total
        this.totalPrice = priceAfterDiscount.add(gstAmount);
    }
}