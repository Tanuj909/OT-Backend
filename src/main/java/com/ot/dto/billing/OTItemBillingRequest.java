package com.ot.dto.billing;


import com.ot.enums.CatalogItemType;

import lombok.Data;

@Data
public class OTItemBillingRequest {
    private Long operationExternalId;
    private Long itemExternalId;
    private CatalogItemType itemType;
    private String itemName;
    private String itemCode;
    private String hsnCode;
    private Integer quantity;
    private Double unitPrice;
    private Double discountPercent;
    private Double gstPercent;
}