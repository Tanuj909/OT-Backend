package com.ot.dto.priceCatalog;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class PriceCatalogRequest {
    private Long catalogItemId;
    private String hsnCode;
    private BigDecimal basePrice;
    private BigDecimal discountPercent;
    private BigDecimal gstPercent;
}
