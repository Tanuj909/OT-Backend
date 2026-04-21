package com.ot.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.ot.dto.request.CreateStaffAvailabilityRequest;
import com.ot.dto.response.StaffAvailabilityResponse;
import com.ot.dto.staffRequest.StaffRosterResponse;
import com.ot.entity.User;

public interface StaffAvailabilityService {

	StaffAvailabilityResponse createStaffAvailability(CreateStaffAvailabilityRequest request);

	List<StaffAvailabilityResponse> getStaffAvailabilityByStaff(Long staffId);

	void deleteStaffAvailability(Long availabilityId);

	boolean isStaffAvailable(Long staffId, LocalDate date, LocalTime start, LocalTime end);

	StaffRosterResponse getStaffAvailability(LocalDateTime startTime, LocalDateTime endTime);




}
