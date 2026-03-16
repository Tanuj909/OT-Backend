package com.ot.dto.equipment;

import java.time.LocalDateTime;
import java.util.Set;
import com.ot.enums.EquipmentCategory;
import com.ot.enums.EquipmentStatus;
import lombok.Data;

@Data
public class EquipmentRequest {
    private String name;
    private String model;
    private String manufacturer;
    private String serialNumber;
    private String assetCode;
    private EquipmentStatus status;
    private EquipmentCategory category;
    private LocalDateTime purchaseDate;
    private LocalDateTime lastMaintenanceDate;
    private LocalDateTime nextMaintenanceDate;
    private Set<String> capabilities;
}
