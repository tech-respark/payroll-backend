package com.relfor.pcs.payroll.entity;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "month_wise_attendance_summary")
public class MonthWiseAttendanceSummary extends BaseEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "increment")
	@GenericGenerator(name = "increment", strategy = "increment")
	private Long id;
	private Long tenantId;
	private Long storeId;
	private String applicationName;
	private Long staffId;
	private LocalDate salaryCycleFromDate;
	private LocalDate salaryCycleToDate;
	private String salaryMonth;
	private Float totalDaysInCycle;
	private Float totalWeeklyOffs;
	private Float totalHolidays;
	private Float totalWorkingDays;
	private Float totalPaidLeaves;
	private Float totalAbsentDays;
	private Float totalPenaltyAbsentDays;
	private Float extraDaysWorked;
	private Float totalPaidDays;
	private Integer totalEarlyExitMins;
	private Integer totalLateArrivalMins;
	private Integer totalOvertimeMins;
	private Integer salaryYear;
	private Integer totalLateArrivals;
	private Integer totalEarlyExits;

	// Getters and Setters
	public Long getId() { return id; }

	public void setId(Long id) { this.id = id; }

	public Long getStaffId() { return staffId; }

	public void setStaffId(Long staffId) { this.staffId = staffId; }

	public LocalDate getSalaryCycleFromDate() { return salaryCycleFromDate; }

	public void setSalaryCycleFromDate(LocalDate salaryCycleFromDate) { this.salaryCycleFromDate = salaryCycleFromDate; }

	public LocalDate getSalaryCycleToDate() { return salaryCycleToDate; }

	public void setSalaryCycleToDate(LocalDate salaryCycleToDate) { this.salaryCycleToDate = salaryCycleToDate; }

	public String getSalaryMonth() { return salaryMonth; }

	public void setSalaryMonth(String salaryMonth) { this.salaryMonth = salaryMonth; }

	public Float getTotalDaysInCycle() { return totalDaysInCycle; }

	public void setTotalDaysInCycle(Float totalDaysInCycle) { this.totalDaysInCycle = totalDaysInCycle; }

	public Float getTotalWeeklyOffs() { return totalWeeklyOffs; }

	public void setTotalWeeklyOffs(Float totalWeeklyOffs) { this.totalWeeklyOffs = totalWeeklyOffs; }

	public Float getTotalHolidays() { return totalHolidays; }

	public void setTotalHolidays(Float totalHolidays) { this.totalHolidays = totalHolidays; }

	public Float getTotalWorkingDays() { return totalWorkingDays; }

	public void setTotalWorkingDays(Float totalWorkingDays) { this.totalWorkingDays = totalWorkingDays; }

	public Float getTotalPaidLeaves() { return totalPaidLeaves; }

	public void setTotalPaidLeaves(Float totalPaidLeaves) { this.totalPaidLeaves = totalPaidLeaves; }

	public Float getTotalAbsentDays() { return totalAbsentDays; }

	public void setTotalAbsentDays(Float totalAbsentDays) { this.totalAbsentDays = totalAbsentDays; }

	public Float getTotalPenaltyAbsentDays() { return totalPenaltyAbsentDays; }

	public void setTotalPenaltyAbsentDays(Float totalPenaltyAbsentDays) { this.totalPenaltyAbsentDays = totalPenaltyAbsentDays; }

	public Integer getTotalEarlyExitMins() { return totalEarlyExitMins; }

	public void setTotalEarlyExitMins(Integer totalEarlyExitMins) { this.totalEarlyExitMins = totalEarlyExitMins; }

	public Integer getTotalLateArrivalMins() { return totalLateArrivalMins; }

	public void setTotalLateArrivalMins(Integer totalLateArrivalMins) { this.totalLateArrivalMins = totalLateArrivalMins; }

	public Float getExtraDaysWorked() { return extraDaysWorked; }

	public void setExtraDaysWorked(Float extraDaysWorked) { this.extraDaysWorked = extraDaysWorked; }

	public Integer getTotalOvertimeMins() { return totalOvertimeMins; }

	public void setTotalOvertimeMins(Integer totalOvertimeMins) { this.totalOvertimeMins = totalOvertimeMins; }

	public Float getTotalPaidDays() {
		return totalPaidDays;
	}

	public void setTotalPaidDays(Float totalPaidDays) {
		this.totalPaidDays = totalPaidDays;
	}

	public Long getTenantId() {
		return tenantId;
	}

	public void setTenantId(Long tenantId) {
		this.tenantId = tenantId;
	}

	public Long getStoreId() {
		return storeId;
	}

	public void setStoreId(Long storeId) {
		this.storeId = storeId;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public Integer getSalaryYear() {
		return salaryYear;
	}

	public void setSalaryYear(Integer salaryYear) {
		this.salaryYear = salaryYear;
	}

	public Integer getTotalLateArrivals() {
		return totalLateArrivals;
	}

	public void setTotalLateArrivals(Integer totalLateArrivals) {
		this.totalLateArrivals = totalLateArrivals;
	}

	public Integer getTotalEarlyExits() {
		return totalEarlyExits;
	}

	public void setTotalEarlyExits(Integer totalEarlyExits) {
		this.totalEarlyExits = totalEarlyExits;
	}
}