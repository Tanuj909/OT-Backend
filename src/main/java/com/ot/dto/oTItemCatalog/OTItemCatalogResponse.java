package com.ot.dto.oTItemCatalog;

import java.time.LocalDateTime;
import com.ot.enums.CatalogItemType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OTItemCatalogResponse {
    private Long id;
    private String itemCode;
    private String itemName;
    private CatalogItemType itemType;
    private String category;
    private String subCategory;
    private String manufacturer;
    private String modelNumber;
    private String description;
    private String unit;
    private Boolean isActive;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}