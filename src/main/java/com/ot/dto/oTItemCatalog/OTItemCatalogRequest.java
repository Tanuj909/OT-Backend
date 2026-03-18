package com.ot.dto.oTItemCatalog;

import com.ot.enums.CatalogItemType;
import lombok.Data;

@Data
public class OTItemCatalogRequest {
    private String itemCode;
    private String itemName;
    private CatalogItemType itemType;
    private String category;
    private String subCategory;
    private String manufacturer;
    private String modelNumber;
    private String description;
    private String unit;
}