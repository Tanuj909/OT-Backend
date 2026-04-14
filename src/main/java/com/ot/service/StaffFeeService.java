package com.ot.service;

import java.util.List;

import com.ot.dto.staffRequest.StaffFeeRequest;
import com.ot.dto.staffRequest.StaffFeeResponse;
import com.ot.dto.staffRequest.StaffFeeUpdateRequest;

public interface StaffFeeService {

    StaffFeeResponse createStaffFee(StaffFeeRequest request);

    StaffFeeResponse getByStaffId(Long staffId);

    List<StaffFeeResponse> getAllStaffFees(Boolean isActive);

    StaffFeeResponse updateStaffFee(Long staffId, StaffFeeUpdateRequest request);
}