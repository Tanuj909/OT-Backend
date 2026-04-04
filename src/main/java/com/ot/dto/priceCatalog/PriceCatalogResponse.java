package com.ot.dto.priceCatalog;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ot.enums.CatalogItemType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PriceCatalogResponse {
    private Long id;
    private Long catalogItemId;
    private String itemCode;
    private String itemName;
    private CatalogItemType itemType;
    private String hsnCode;
    private Double basePrice;
    private Double discountPercent;
    private BigDecimal discountAmount;
    private BigDecimal priceAfterDiscount;
    private Double gstPercent;
    private BigDecimal gstAmount;
    private BigDecimal totalPrice;
    private Boolean isActive;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
