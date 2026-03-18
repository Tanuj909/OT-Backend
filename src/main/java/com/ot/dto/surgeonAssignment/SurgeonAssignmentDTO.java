package com.ot.dto.surgeonAssignment;

import com.ot.enums.SurgeonRole;

import lombok.Data;

@Data
public class SurgeonAssignmentDTO {
	
    private Long surgeonId;

    private String surgeonName;

    private SurgeonRole role;
    
    private boolean isPrimary;     // ✅ missing tha

}
