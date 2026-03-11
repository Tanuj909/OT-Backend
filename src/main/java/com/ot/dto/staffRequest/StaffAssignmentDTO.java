package com.ot.dto.staffRequest;

import com.ot.enums.StaffRole;

import lombok.Data;

@Data
public class StaffAssignmentDTO {

    private Long staffId;

    private String staffName;

    private StaffRole role;
}