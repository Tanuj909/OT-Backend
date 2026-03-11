package com.ot.service;

import java.util.Set;

import com.ot.dto.staffRequest.StaffAssignmentRequest;
import com.ot.dto.staffRequest.StaffUnAssignRequest;
import com.ot.dto.surgeonRequest.SurgeonAssignmentRequest;
import com.ot.dto.surgeonRequest.UnAssignSurgeonRequest;

public interface AssignService {

	void assignStaff(Long operationId, StaffAssignmentRequest request);

	void unAssignStaff(Long operationId, Set<Long> staffIds);

	void assignSurgeon(Long operationId, SurgeonAssignmentRequest request);

	void unAssignSurgeon(Long operationId, UnAssignSurgeonRequest request);

}
