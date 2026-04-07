package com.ot.dto.billing;


import lombok.Data;

@Data
public class OTItemBillingUpdateRequest {
    private Integer quantity;
    private Double unitPrice;
    private Double discountPercent;
    private Double gstPercent;
}
