package com.ot.dto.dashboards;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminDashboardResponse {

    private OverviewStats overviewStats;
    private List<OTRoomStatusDTO> otRoomStatus;
    private List<TodayScheduleDTO> todaySchedule;
    private StaffAvailabilitySummary staffAvailability;
    private List<RecentOperationDTO> recentOperations;
    private List<PendingOTRequestDTO> pendingRequests;
    private List<OverdueOperationDTO> overdueOperations;  // exceeded end time

    // ==================== Nested Classes ==================== //

    @Data
    @Builder
    public static class OverviewStats {
        private Long totalOperationsToday;
        private Long scheduledOperations;
        private Long inProgressOperations;
        private Long completedOperationsToday;
        private Long cancelledOperationsToday;
        private Long pendingOTRequests;
        private Long availableOTRooms;
        private Long occupiedOTRooms;
        private Long totalWardBeds;
        private Long occupiedWardBeds;
        private Long availableWardBeds;
        private Long overdueOperations;     // exceeded end time
    }

    @Data
    @Builder
    public static class OTRoomStatusDTO {
        private Long roomId;
        private String roomNumber;
        private String roomName;
        private String status;              // AVAILABLE, OCCUPIED, UNDER_CLEANING
        private String currentOperationRef; // agar occupied hai
        private String currentPatientName;
        private LocalDateTime operationStartTime;
        private LocalDateTime scheduledEndTime;
        private Boolean isOverdue;          // end time exceed hua?
    }

    @Data
    @Builder
    public static class TodayScheduleDTO {
        private Long operationId;
        private String operationReference;
        private String patientName;
        private String patientMrn;
        private String procedureName;
        private String roomNumber;
        private String primarySurgeon;
        private String anesthesiologist;
        private LocalDateTime scheduledStartTime;
        private LocalDateTime scheduledEndTime;
        private LocalDateTime actualStartTime;
        private String status;
        private Boolean isOverdue;
    }

    @Data
    @Builder
    public static class StaffAvailabilitySummary {
        private Integer totalSurgeons;
        private Integer availableSurgeons;
        private Integer busySurgeons;
        private Integer totalAnesthesiologists;
        private Integer availableAnesthesiologists;
        private Integer busyAnesthesiologists;
        private Integer totalNurses;
        private Integer availableNurses;
        private Integer busyNurses;
        private Integer totalTechnicians;
        private Integer availableTechnicians;
        private Integer busyTechnicians;
    }

    @Data
    @Builder
    public static class RecentOperationDTO {
        private Long operationId;
        private String operationReference;
        private String patientName;
        private String procedureName;
        private String roomNumber;
        private String primarySurgeon;
        private String status;
        private LocalDateTime scheduledStartTime;
        private LocalDateTime actualStartTime;
        private LocalDateTime actualEndTime;
        private Long durationMinutes;
    }

    @Data
    @Builder
    public static class PendingOTRequestDTO {
        private Long operationId;
        private String operationReference;
        private String patientName;
        private String patientMrn;
        private String procedureName;
        private String complexity;
        private LocalDateTime requestedAt;
        private Long pendingSinceHours;     // kitne ghante se pending hai
    }

    @Data
    @Builder
    public static class OverdueOperationDTO {
        private Long operationId;
        private String operationReference;
        private String patientName;
        private String procedureName;
        private String roomNumber;
        private String primarySurgeon;
        private LocalDateTime scheduledEndTime;
        private LocalDateTime actualStartTime;
        private Long exceededByMinutes;     // kitne minutes exceed hua
        private String status;
    }
}