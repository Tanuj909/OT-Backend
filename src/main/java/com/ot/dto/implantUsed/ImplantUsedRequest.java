package com.ot.dto.implantUsed;

import lombok.Data;

@Data
public class ImplantUsedRequest {
    private Long catalogItemId;     // OTItemCatalog ka id
    private String serialNumber;
    private String batchNumber;
    private Integer quantity;
    private String bodyLocation;
    private String notes;
}