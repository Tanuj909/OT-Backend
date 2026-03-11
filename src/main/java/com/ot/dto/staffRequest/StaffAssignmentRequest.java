package com.ot.dto.staffRequest;

import java.util.Set;

import lombok.Data;

@Data
public class StaffAssignmentRequest {

    private Set<StaffAssignmentDTO> staff;

}
