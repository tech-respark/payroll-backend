package com.relfor.pcs.payroll.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardMetricsDTO {
    private long totalHeadcount;
    private double attendancePercentageToday;
    private long pendingLeaveRequests;
    private long staffOnLeaveToday;
    private List<String> staffOnLeaveNames;

    private List<DailyAttendanceStat> weeklyAttendance;
    private List<UnplannedAbsenceStat> unplannedAbsences;
    private List<ActivityLog> recentActivity;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnplannedAbsenceStat {
        private String staffName;
        private String designation;
        private long absentDays;
        private List<String> awolDates;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyAttendanceStat {
        private String date; // "Mon", "Tue"
        private long present;
        private long absent;
        private long late;
        private long onLeave;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentStat {
        private String designation;
        private long total;
        private long present;
        private long onLeave;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityLog {
        private String staffName;
        private String action;
        private String time;
        private String type; // "CheckIn", "LeaveRequest"
    }
}
