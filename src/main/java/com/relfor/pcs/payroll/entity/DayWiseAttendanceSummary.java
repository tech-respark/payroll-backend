package com.relfor.pcs.payroll.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "day_wise_attendance_summary")
public class DayWiseAttendanceSummary extends BaseEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Long tenantId;
	private Long storeId;
	private String applicationName;
	private String terminalSerialNumber;
	private Long personnelId;
	private LocalDate attendanceDate;
	private String attendanceDayOfWeek;
	private BigDecimal totalHoursWorkedInADay;
	private BigDecimal totalBreakTimeInADay;
	private BigDecimal sumOfActualHoursWorkedInADay;

	private BigDecimal workingHoursAsPerRoster;
	private BigDecimal breakHoursAsPerRoster;
	private Long earlyExitMins;
	private Long lateArrivalMins;
	private Long diffBetweenActualAndRosterWorkMins; //if negative then under worked and if positive then over worked
	private String attendanceRemark;
	private LocalTime firstCheckinPerRoster;
	private LocalTime lastCheckoutPerRoster;
	private Boolean isOnLeave;
	private Boolean isWeeklyOff;
	private Boolean isPresent;
	private Boolean isAbsent;
	private Boolean isPenaltyAbsent;
	private Boolean isExtraDay;
	private Boolean isLateArrival;
	private Boolean isEarlyExit;
	private Boolean isHoliday;

	@Enumerated(EnumType.STRING)
	private DayCategory dayCategory;

	private BigDecimal workUnits;

	private BigDecimal leaveUnits;

	public enum DayCategory {
		PRESENT, ABSENT, HALF_DAY, ON_LEAVE, WEEKLY_OFF, HOLIDAY, SPLIT
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTerminalSerialNumber() {
		return terminalSerialNumber;
	}

	public void setTerminalSerialNumber(String terminalSerialNumber) {
		this.terminalSerialNumber = terminalSerialNumber;
	}

	public Long getPersonnelId() {
		return personnelId;
	}

	public void setPersonnelId(Long personnelId) {
		this.personnelId = personnelId;
	}

	public LocalDate getAttendanceDate() {
		return attendanceDate;
	}

	public void setAttendanceDate(LocalDate attendanceDate) {
		this.attendanceDate = attendanceDate;
	}

	public String getAttendanceDayOfWeek() {
		return attendanceDayOfWeek;
	}

	public void setAttendanceDayOfWeek(String attendanceDayOfWeek) {
		this.attendanceDayOfWeek = attendanceDayOfWeek;
	}

	public BigDecimal getTotalHoursWorkedInADay() {
		return totalHoursWorkedInADay;
	}

	public void setTotalHoursWorkedInADay(BigDecimal totalHoursWorkedInADay) {
		this.totalHoursWorkedInADay = totalHoursWorkedInADay;
	}

	public BigDecimal getTotalBreakTimeInADay() {
		return totalBreakTimeInADay;
	}

	public void setTotalBreakTimeInADay(BigDecimal totalBreakTimeInADay) {
		this.totalBreakTimeInADay = totalBreakTimeInADay;
	}

	public Long getTenantId() {
		return tenantId;
	}

	public void setTenantId(Long tenantId) {
		this.tenantId = tenantId;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public Long getStoreId() {
		return storeId;
	}

	public void setStoreId(Long storeId) {
		this.storeId = storeId;
	}

	public BigDecimal getSumOfActualHoursWorkedInADay() {
		return sumOfActualHoursWorkedInADay;
	}

	public void setSumOfActualHoursWorkedInADay(BigDecimal sumOfActualHoursWorkedInADay) {
		this.sumOfActualHoursWorkedInADay = sumOfActualHoursWorkedInADay;
	}

	public Long getDiffBetweenActualAndRosterWorkMins() {
		return diffBetweenActualAndRosterWorkMins;
	}

	public void setDiffBetweenActualAndRosterWorkMins(Long diffBetweenActualAndRosterWorkMins) {
		this.diffBetweenActualAndRosterWorkMins = diffBetweenActualAndRosterWorkMins;
	}

	public Long getLateArrivalMins() {
		return lateArrivalMins;
	}

	public void setLateArrivalMins(Long lateArrivalMins) {
		this.lateArrivalMins = lateArrivalMins;
	}

	public Long getEarlyExitMins() {
		return earlyExitMins;
	}

	public void setEarlyExitMins(Long earlyExitMins) {
		this.earlyExitMins = earlyExitMins;
	}

	public BigDecimal getWorkingHoursAsPerRoster() {
		return workingHoursAsPerRoster;
	}

	public void setWorkingHoursAsPerRoster(BigDecimal workingHoursAsPerRoster) {
		this.workingHoursAsPerRoster = workingHoursAsPerRoster;
	}

	public BigDecimal getBreakHoursAsPerRoster() {
		return breakHoursAsPerRoster;
	}

	public void setBreakHoursAsPerRoster(BigDecimal breakTimeAsPerRoster) {
		this.breakHoursAsPerRoster = breakTimeAsPerRoster;
	}

	public String getAttendanceRemark() {
		return attendanceRemark;
	}

	public void setAttendanceRemark(String attendanceRemark) {
		this.attendanceRemark = attendanceRemark;
	}

	public Boolean getIsOnLeave() {
		return isOnLeave;
	}

	public void setIsOnLeave(Boolean onLeave) {
		isOnLeave = onLeave;
	}

	public Boolean getIsEarlyExit() {
		return isEarlyExit;
	}

	public void setIsEarlyExit(Boolean earlyExit) {
		isEarlyExit = earlyExit;
	}

	public Boolean getIsWeeklyOff() {
		return isWeeklyOff;
	}

	public void setIsWeeklyOff(Boolean weeklyOff) {
		isWeeklyOff = weeklyOff;
	}

	public Boolean getIsAbsent() {
		return isAbsent;
	}

	public void setIsAbsent(Boolean absent) {
		isAbsent = absent;
	}

	public Boolean getIsPenaltyAbsent() {
		return isPenaltyAbsent;
	}

	public void setIsPenaltyAbsent(Boolean penaltyAbsent) {
		isPenaltyAbsent = penaltyAbsent;
	}

	public Boolean getIsLateArrival() {
		return isLateArrival;
	}

	public void setIsLateArrival(Boolean lateArrival) {
		isLateArrival = lateArrival;
	}

	public Boolean getIsExtraDay() {
		return isExtraDay;
	}

	public void setIsExtraDay(Boolean extraDay) {
		isExtraDay = extraDay;
	}

	public LocalTime getFirstCheckinPerRoster() {
		return firstCheckinPerRoster;
	}

	public void setFirstCheckinPerRoster(LocalTime firstCheckinPerRoster) {
		this.firstCheckinPerRoster = firstCheckinPerRoster;
	}

	public LocalTime getLastCheckoutPerRoster() {
		return lastCheckoutPerRoster;
	}

	public void setLastCheckoutPerRoster(LocalTime lastCheckoutPerRoster) {
		this.lastCheckoutPerRoster = lastCheckoutPerRoster;
	}

	public Boolean getIsPresent() {
		return isPresent;
	}

	public void setIsPresent(Boolean present) {
		isPresent = present;
	}

	public Boolean getIsHoliday() {
		return isHoliday;
	}

	public void setIsHoliday(Boolean holiday) {
		isHoliday = holiday;
	}

	public DayCategory getDayCategory() {
		return dayCategory;
	}

	public void setDayCategory(DayCategory dayCategory) {
		this.dayCategory = dayCategory;
	}

	public BigDecimal getWorkUnits() {
		return workUnits;
	}

	public void setWorkUnits(BigDecimal workUnits) {
		this.workUnits = workUnits;
	}

	public BigDecimal getLeaveUnits() {
		return leaveUnits;
	}

	public void setLeaveUnits(BigDecimal leaveUnits) {
		this.leaveUnits = leaveUnits;
	}
}