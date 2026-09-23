package com.relfor.pcs.payroll.service;

import com.relfor.pcs.payroll.dto.DashboardMetricsDTO;
import com.relfor.pcs.payroll.entity.DayWiseAttendanceSummary;
import com.relfor.pcs.payroll.entity.LeaveApplication;
import com.relfor.pcs.payroll.entity.PersonnelDetails;
import com.relfor.pcs.payroll.repository.DayWiseAttendanceSummaryRepository;
import com.relfor.pcs.payroll.repository.LeaveApplicationRepository;
import com.relfor.pcs.payroll.repository.PersonnelDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HRDashboardService {

    private final PersonnelDetailsRepository personnelDetailsRepository;
    private final LeaveApplicationRepository leaveApplicationRepository;
    private final DayWiseAttendanceSummaryRepository attendanceRepository;

    public DashboardMetricsDTO getDashboardMetrics(Long tenantId, Long storeId) {
        LocalDate today = LocalDate.now(ZoneId.of("UTC")); // Adjust timezone as needed
        LocalDate startOfWeek = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        List<PersonnelDetails> allStaff = personnelDetailsRepository.findByApplicationTenantIdAndStoreId(tenantId, storeId);

        // 1. Headcount
        long totalHeadcount = allStaff.size();

        // 2. Pending Leave Requests
        long pendingRequests = leaveApplicationRepository.countByStatus(tenantId, storeId, LeaveApplication.ApplicationStatus.PENDING);

        // 3. Staff on Leave Today
        List<LeaveApplication> leavesToday = leaveApplicationRepository.findApprovedLeavesForDate(tenantId, storeId, today);
        long staffOnLeaveToday = leavesToday.size();
        List<String> staffOnLeaveNames = leavesToday.stream()
                .map(l -> allStaff.stream().filter(s -> s.getId().equals(l.getStaffId())).findFirst()
                        .map(s -> s.getFirstName() + " " + s.getLastName()).orElse("Unknown Staff"))
                .distinct()
                .collect(Collectors.toList());

        // 4. Attendance Today
        List<DayWiseAttendanceSummary> weeklyAttendanceData = attendanceRepository.findByTenantAndStoreAndDateBetween(tenantId, storeId, startOfWeek, endOfWeek);
        
        List<DayWiseAttendanceSummary> todayAttendance = weeklyAttendanceData.stream()
                .filter(a -> a.getAttendanceDate().equals(today))
                .collect(Collectors.toList());
        
        long totalPresentToday = todayAttendance.stream().filter(a -> Boolean.TRUE.equals(a.getIsPresent())).count();
        
        double attendancePercentage = totalHeadcount > 0 ? ((double) totalPresentToday / totalHeadcount) * 100 : 0.0;
        
        // 5. Weekly Attendance Trend
        Map<LocalDate, List<DayWiseAttendanceSummary>> attendanceByDate = weeklyAttendanceData.stream()
                .collect(Collectors.groupingBy(DayWiseAttendanceSummary::getAttendanceDate));

        List<DashboardMetricsDTO.DailyAttendanceStat> weeklyStats = new ArrayList<>();
        for (int i = 0; i <= 6; i++) {
            LocalDate d = startOfWeek.plusDays(i);
            List<DayWiseAttendanceSummary> dailyData = attendanceByDate.getOrDefault(d, Collections.emptyList());
            
            long p = dailyData.stream().filter(a -> Boolean.TRUE.equals(a.getIsPresent())).count();
            long a = dailyData.stream().filter(x -> Boolean.TRUE.equals(x.getIsAbsent())).count();
            long l = dailyData.stream().filter(x -> Boolean.TRUE.equals(x.getIsLateArrival())).count();
            long ol = dailyData.stream().filter(x -> Boolean.TRUE.equals(x.getIsOnLeave())).count();
            
            String label = d.getDayOfWeek().toString().substring(0,3) + "|" + d.toString();
            weeklyStats.add(new DashboardMetricsDTO.DailyAttendanceStat(label, p, a, l, ol));
        }

        // 6. Action Required: Unplanned Absences (Past 30 Days)
        LocalDate startOfRange = today.minusDays(30);
        List<DayWiseAttendanceSummary> monthAttendance = attendanceRepository.findByTenantAndStoreAndDateBetween(tenantId, storeId, startOfRange, today);
        
        Set<Long> activeStaffIds = allStaff.stream().map(PersonnelDetails::getId).collect(Collectors.toSet());

        LocalDate yesterday = today.minusDays(1);
        List<LocalDate> allDaysThisMonth = new ArrayList<>();
        for (LocalDate d = startOfRange; !d.isAfter(yesterday); d = d.plusDays(1)) {
            allDaysThisMonth.add(d);
        }

        List<LeaveApplication> monthLeaves = leaveApplicationRepository.findByTenantIdAndStoreIdOrderByCreatedAtDesc(tenantId, storeId)
                .stream().filter(l -> l.getStatus() == LeaveApplication.ApplicationStatus.APPROVED
                && !l.getStartDate().isAfter(yesterday) && !l.getEndDate().isBefore(startOfRange))
                .collect(Collectors.toList());

        Map<Long, Set<LocalDate>> staffLeaveDates = new HashMap<>();
        for (LeaveApplication l : monthLeaves) {
            staffLeaveDates.computeIfAbsent(l.getStaffId(), k -> new HashSet<>());
            for (LocalDate d = l.getStartDate(); !d.isAfter(l.getEndDate()); d = d.plusDays(1)) {
                staffLeaveDates.get(l.getStaffId()).add(d);
            }
        }

        Map<Long, Set<LocalDate>> staffAttendanceDates = monthAttendance.stream()
                .filter(a -> a.getStaffId() != null)
                .collect(Collectors.groupingBy(
                        DayWiseAttendanceSummary::getStaffId,
                        Collectors.mapping(DayWiseAttendanceSummary::getAttendanceDate, Collectors.toSet())
                ));

        Map<Long, List<String>> absenceDetails = new HashMap<>();
        for (PersonnelDetails staff : allStaff) {
            List<String> awolDates = new ArrayList<>();
            Set<LocalDate> attDates = staffAttendanceDates.getOrDefault(staff.getId(), Collections.emptySet());
            Set<LocalDate> leaveDates = staffLeaveDates.getOrDefault(staff.getId(), Collections.emptySet());
            
            LocalDate joining = null;
            if (staff.getJoiningDate() != null && staff.getJoiningDate().length() >= 10) {
                try {
                    joining = LocalDate.parse(staff.getJoiningDate().substring(0, 10));
                } catch (Exception e) {}
            }
            
            String staffWeeklyOffs = staff.getWeeklyOff() != null ? staff.getWeeklyOff().toUpperCase() : "";
            
            for (LocalDate wd : allDaysThisMonth) {
                if (joining != null && wd.isBefore(joining)) continue;
                
                // Skip if this day of the week is in their weeklyOff string
                if (staffWeeklyOffs.contains(wd.getDayOfWeek().toString())) continue;
                
                if (!attDates.contains(wd) && !leaveDates.contains(wd)) {
                    awolDates.add(wd.toString());
                }
            }
            
            monthAttendance.stream()
                .filter(a -> a.getStaffId() != null && a.getStaffId().equals(staff.getId()) && Boolean.TRUE.equals(a.getIsAbsent()) && !Boolean.TRUE.equals(a.getIsOnLeave()))
                .map(a -> a.getAttendanceDate().toString())
                .forEach(dateStr -> {
                    if (!awolDates.contains(dateStr)) {
                        awolDates.add(dateStr);
                    }
                });
                
            if (!awolDates.isEmpty()) {
                // sort dates descending
                awolDates.sort(Collections.reverseOrder());
                absenceDetails.put(staff.getId(), awolDates);
            }
        }
        
        List<DashboardMetricsDTO.UnplannedAbsenceStat> unplannedAbsences = absenceDetails.entrySet().stream()
                .map(entry -> {
                    PersonnelDetails p = allStaff.stream().filter(s -> s.getId().equals(entry.getKey())).findFirst().orElse(null);
                    String name = p != null ? p.getFirstName() + " " + p.getLastName() : "Unknown";
                    String desig = p != null && p.getDesignation() != null ? p.getDesignation() : "Staff";
                    return new DashboardMetricsDTO.UnplannedAbsenceStat(name, desig, entry.getValue().size(), entry.getValue());
                })
                .sorted((a,b) -> Long.compare(b.getAbsentDays(), a.getAbsentDays()))
                .limit(5)
                .collect(Collectors.toList());

        // 7. Recent Activity (Latest leaves)
        List<LeaveApplication> recentLeaves = leaveApplicationRepository.findByTenantIdAndStoreIdOrderByCreatedAtDesc(tenantId, storeId);
        List<DashboardMetricsDTO.ActivityLog> activityLogs = recentLeaves.stream().limit(5)
                .map(l -> {
                    String name = allStaff.stream().filter(s -> s.getId().equals(l.getStaffId())).findFirst()
                            .map(s -> s.getFirstName() + " " + s.getLastName()).orElse("Unknown Staff");
                    return new DashboardMetricsDTO.ActivityLog(name, "Submitted Leave Request", l.getCreatedAt().toString().substring(0,10), "LeaveRequest");
                }).collect(Collectors.toList());


        return DashboardMetricsDTO.builder()
                .totalHeadcount(totalHeadcount)
                .attendancePercentageToday(Math.round(attendancePercentage * 10.0) / 10.0)
                .pendingLeaveRequests(pendingRequests)
                .staffOnLeaveToday(staffOnLeaveToday)
                .staffOnLeaveNames(staffOnLeaveNames)
                .weeklyAttendance(weeklyStats)
                .unplannedAbsences(unplannedAbsences)
                .recentActivity(activityLogs)
                .build();
    }
}
