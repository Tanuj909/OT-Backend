package com.ot.dto.oTItemCatalog;

import lombok.Data;

@Data
public class OTItemCatalogUpdateRequest {
    private String itemName;
    private String category;
    private String subCategory;
    private String manufacturer;
    private String modelNumber;
    private String description;
    private String unit;
    private Boolean isActive;
}