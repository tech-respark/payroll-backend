package com.relfor.pcs.payroll.service;

import com.relfor.pcs.payroll.dto.DashboardMetricsDTO;
import com.relfor.pcs.payroll.dto.OperationsHomeDTO;
import com.relfor.pcs.payroll.dto.StaffHomeDTO;
import com.relfor.pcs.payroll.entity.LeaveApplication;
import com.relfor.pcs.payroll.entity.LeavePlanRule;
import com.relfor.pcs.payroll.entity.LeaveType;
import com.relfor.pcs.payroll.entity.PersonnelAttendance;
import com.relfor.pcs.payroll.entity.PersonnelDetails;
import com.relfor.pcs.payroll.entity.SStaffShifts;
import com.relfor.pcs.payroll.entity.StoreDetails;
import com.relfor.pcs.payroll.repository.LeaveApplicationRepository;
import com.relfor.pcs.payroll.repository.PersonnelAttendanceRepository;
import com.relfor.pcs.payroll.repository.PersonnelDetailsRepository;
import com.relfor.pcs.payroll.repository.SStaffShiftsRepository;
import com.relfor.pcs.payroll.repository.StoreDetailsRepository;
import com.relfor.pcs.payroll.security.Permissions;
import com.relfor.pcs.payroll.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class HomeDashboardService {

    private static final ZoneId STORE_ZONE = ZoneId.of("Asia/Kolkata");
    private static final List<LeaveApplication.ApplicationStatus> OPEN_LEAVE = List.of(
            LeaveApplication.ApplicationStatus.PENDING,
            LeaveApplication.ApplicationStatus.CANCELLATION_REQUESTED
    );

    private final PersonnelDetailsRepository personnelDetailsRepository;
    private final PersonnelAttendanceRepository attendanceRepository;
    private final SStaffShiftsRepository staffShiftsRepository;
    private final StaffIdentityService staffIdentityService;
    private final LeaveApplicationRepository leaveApplicationRepository;
    private final LeaveManagementService leaveManagementService;
    private final LeaveLedgerService leaveLedgerService;
    private final StoreDetailsRepository storeDetailsRepository;
    private final HRDashboardService hrDashboardService;
    private final com.relfor.pcs.payroll.repository.PersonnelPayslipHistoryRepository personnelPayslipHistoryRepository;

    @Transactional(readOnly = true)
    public StaffHomeDTO staffHome(Long tenantId, Long storeId) {
        Long staffId = SecurityUtils.getStaffId();
        if (staffId == null) {
            throw new AccessDeniedException("Authenticated staff id is required");
        }
        LocalDate today = LocalDate.now(STORE_ZONE);
        PersonnelDetails person = personnelDetailsRepository.findById(staffId).orElse(null);
        List<PersonnelAttendance> punches = attendanceRepository
                .findByTenantIdAndStoreIdAndStaffIdAndAttendanceDateOrderByPunchTimestampAsc(tenantId, storeId, staffId, today);

        StaffHomeDTO.Pay pay = SecurityUtils.hasAnyAuthority(Permissions.VIEW_SALARY, Permissions.MANAGE_SALARY)
                ? payMonth(tenantId, storeId)
                : null;

        return StaffHomeDTO.builder()
                .firstName(person != null ? person.getFirstName() : null)
                .today(StaffHomeDTO.Today.builder()
                        .date(today.toString())
                        .clockedIn(isClockedIn(punches))
                        .lastInAt(lastInAt(punches))
                        .shift(todayShift(tenantId, storeId, staffId, today))
                        .build())
                .leave(StaffHomeDTO.Leave.builder()
                        .pendingCount(leaveApplicationRepository.countByStaffIdAndStatusIn(tenantId, storeId, staffId, OPEN_LEAVE))
                        .balances(leaveBalances(tenantId, storeId, staffId, today))
                        .build())
                .pay(pay)
                .build();
    }

    @Transactional(readOnly = true)
    public OperationsHomeDTO operationsHome(Long tenantId, Long storeId, boolean includeSnapshot) {
        if (!SecurityUtils.isOperationsUser()) {
            throw new AccessDeniedException("Operations home is limited to managers and administrators");
        }
        boolean canLeave = SecurityUtils.hasAnyAuthority(Permissions.MANAGE_LEAVES, Permissions.VIEW_HR_DASHBOARD);
        boolean canReg = SecurityUtils.hasAuthority(Permissions.MANAGE_ATTENDANCE);
        boolean canHr = SecurityUtils.hasAuthority(Permissions.VIEW_HR_DASHBOARD);
        boolean canPay = SecurityUtils.hasAnyAuthority(Permissions.VIEW_SALARY, Permissions.MANAGE_SALARY);

        Long pendingLeave = canLeave
                ? leaveApplicationRepository.countByStatusIn(tenantId, storeId, OPEN_LEAVE)
                : null;
        Long pendingReg = canReg
                ? attendanceRepository.countPendingRegularizations(tenantId, storeId, LocalDate.now(STORE_ZONE).minusDays(45))
                : null;
        Long onLeaveToday = canHr
                ? leaveApplicationRepository.findApprovedLeavesForDate(tenantId, storeId, LocalDate.now(STORE_ZONE))
                        .stream()
                        .map(LeaveApplication::getStaffId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .count()
                : null;

        DashboardMetricsDTO snapshot = includeSnapshot && canHr
                ? hrDashboardService.getDashboardMetrics(tenantId, storeId)
                : null;

        return OperationsHomeDTO.builder()
                .queues(OperationsHomeDTO.Queues.builder()
                        .pendingLeave(pendingLeave)
                        .pendingRegularization(pendingReg)
                        .onLeaveToday(onLeaveToday)
                        .build())
                .pay(canPay ? payMonth(tenantId, storeId) : null)
                .snapshot(snapshot)
                .build();
    }

    private StaffHomeDTO.Shift todayShift(Long tenantId, Long storeId, Long staffId, LocalDate today) {
        List<Long> keys = staffIdentityService.rosterKeysForStaffId(staffId);
        if (keys.isEmpty()) {
            return null;
        }
        List<SStaffShifts> roster = staffShiftsRepository.findByTenantIdAndStoreIdAndShiftDateAndStaffIdIn(
                tenantId, storeId, today, keys);
        if (roster.isEmpty()) {
            return null;
        }
        SStaffShifts shift = roster.get(0);
        return StaffHomeDTO.Shift.builder()
                .slot(shift.getSlot())
                .weeklyOff(shift.getWeeklyOff())
                .onLeave(shift.getOnLeave())
                .build();
    }

    private List<StaffHomeDTO.Balance> leaveBalances(Long tenantId, Long storeId, Long staffId, LocalDate today) {
        try {
            return leaveManagementService.getEligibleLeaveRules(staffId).stream()
                    .limit(4)
                    .map(rule -> toBalance(tenantId, storeId, staffId, today, rule))
                    .toList();
        } catch (Exception ex) {
            return List.of();
        }
    }

    private StaffHomeDTO.Balance toBalance(Long tenantId, Long storeId, Long staffId, LocalDate today, LeavePlanRule rule) {
        LeaveType type = rule.getLeaveType();
        return StaffHomeDTO.Balance.builder()
                .id(type.getId())
                .type(type.getLeaveName())
                .available(leaveLedgerService.getAvailableBalance(staffId, type.getId(), today))
                .build();
    }

    private StaffHomeDTO.Pay payMonth(Long tenantId, Long storeId) {
        List<Object[]> rows = personnelPayslipHistoryRepository.findLatestMonth(tenantId, storeId);
        if (rows == null || rows.isEmpty() || rows.get(0) == null) {
            return null;
        }
        Object[] row = rows.get(0);
        String monthName = row.length > 0 && row[0] != null ? String.valueOf(row[0]) : null;
        Integer year = row.length > 1 && row[1] != null ? ((Number) row[1]).intValue() : null;
        Integer monthNumber = null;
        String display = null;
        if (monthName != null) {
            try {
                Month parsed = Month.valueOf(monthName.trim().toUpperCase(Locale.ROOT));
                monthNumber = parsed.getValue();
                display = parsed.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            } catch (Exception ignored) {
                display = monthName;
            }
        }
        if (display == null) {
            return null;
        }
        if (year != null) {
            display = display + " " + year;
        }
        return StaffHomeDTO.Pay.builder()
                .lastCalculatedMonthNumber(monthNumber)
                .lastCalculatedMonth(display)
                .build();
    }

    private boolean isClockedIn(List<PersonnelAttendance> punches) {
        if (punches == null || punches.isEmpty()) {
            return false;
        }
        String event = String.valueOf(punches.get(punches.size() - 1).getPunchEvent()).toUpperCase(Locale.ROOT);
        return "IN".equals(event) || "CHECKIN".equals(event);
    }

    private String lastInAt(List<PersonnelAttendance> punches) {
        if (punches == null) {
            return null;
        }
        for (int i = punches.size() - 1; i >= 0; i--) {
            PersonnelAttendance punch = punches.get(i);
            String event = String.valueOf(punch.getPunchEvent()).toUpperCase(Locale.ROOT);
            if (("IN".equals(event) || "CHECKIN".equals(event)) && punch.getPunchTimestamp() != null) {
                return punch.getPunchTimestamp().toString();
            }
        }
        return null;
    }
}
