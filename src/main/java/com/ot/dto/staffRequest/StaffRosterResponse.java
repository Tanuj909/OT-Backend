package com.ot.dto.staffRequest;

import java.time.LocalDateTime;
import java.util.List;

import com.ot.enums.RoleType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StaffRosterResponse  {

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private List<StaffMemberAvailability> surgeons;
    private List<StaffMemberAvailability> anesthesiologists;
    private List<StaffMemberAvailability> scrubNurses;
    private List<StaffMemberAvailability> circulatingNurses;
    private List<StaffMemberAvailability> anesthesiaNurses;
    private List<StaffMemberAvailability> otTechnicians;
    private List<StaffMemberAvailability> surgicalTechs;
    private List<StaffMemberAvailability> anesthesiaTechnicians;
    private List<StaffMemberAvailability> orderlies;
    private List<StaffMemberAvailability> otAssistants;

    // Summary
    private Integer totalAvailable;
    private Integer totalBusy;

    @Data
    @Builder
    public static class StaffMemberAvailability {
        private Long id;
        private String userName;
        private String email;
        private RoleType role;
        private Boolean isAvailable;
        private Long assignedOperationId;       // busy hai toh kis operation mein
        private String assignedOperationRef;    // OT-2024-001
    }
}