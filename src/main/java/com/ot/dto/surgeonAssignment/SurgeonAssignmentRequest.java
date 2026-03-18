package com.ot.dto.surgeonAssignment;

import java.util.Set;

import lombok.Data;

@Data
public class SurgeonAssignmentRequest {
	
	private Set<SurgeonAssignmentDTO> surgeon;

}
