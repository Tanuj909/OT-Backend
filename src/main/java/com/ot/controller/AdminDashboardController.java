package com.ot.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ot.dto.dashboards.AdminDashboardResponse;
import com.ot.dto.response.ApiResponse;
import com.ot.service.AdminDashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success("Dashboard fetched successfully",
                adminDashboardService.getDashboard()));
    }

//    // Alag alag bhi fetch kar sako
//    @GetMapping("/stats")
//    public ResponseEntity<ApiResponse<AdminDashboardResponse.OverviewStats>> getStats() {
//        return ResponseEntity.ok(ApiResponse.success("Stats fetched successfully",
//                adminDashboardService.getOverviewStats()));
//    }
//
//    @GetMapping("/schedule/today")
//    public ResponseEntity<ApiResponse<List<AdminDashboardResponse.TodayScheduleDTO>>> getTodaySchedule() {
//        return ResponseEntity.ok(ApiResponse.success("Today schedule fetched successfully",
//                adminDashboardService.getTodaySchedule()));
//    }
//
//    @GetMapping("/rooms")
//    public ResponseEntity<ApiResponse<List<AdminDashboardResponse.OTRoomStatusDTO>>> getRoomStatus() {
//        return ResponseEntity.ok(ApiResponse.success("Room status fetched successfully",
//                adminDashboardService.getOTRoomStatus()));
//    }
//
//    @GetMapping("/overdue")
//    public ResponseEntity<ApiResponse<List<AdminDashboardResponse.OverdueOperationDTO>>> getOverdueOperations() {
//        return ResponseEntity.ok(ApiResponse.success("Overdue operations fetched successfully",
//                adminDashboardService.getOverdueOperations()));
//    }
//
//    @GetMapping("/pending-requests")
//    public ResponseEntity<ApiResponse<List<AdminDashboardResponse.PendingOTRequestDTO>>> getPendingRequests() {
//        return ResponseEntity.ok(ApiResponse.success("Pending requests fetched successfully",
//                adminDashboardService.getPendingRequests()));
//    }
}