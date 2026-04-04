package com.ot.dto.priceCatalog;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class PriceCatalogRequest {
    private Long catalogItemId;
    private String hsnCode;
    private Double basePrice;
    private Double discountPercent;
    private Double gstPercent;
}
