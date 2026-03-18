package com.ot.dto.implantUsed;

import lombok.Data;

@Data
public class ImplantUsedUpdateRequest {
    private String serialNumber;
    private String batchNumber;
    private Integer quantity;
    private String bodyLocation;
    private String notes;
}
