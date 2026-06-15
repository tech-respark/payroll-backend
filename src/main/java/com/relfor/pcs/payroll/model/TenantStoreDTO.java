package com.relfor.pcs.payroll.model;

import java.time.Instant;
import java.time.LocalDate;

public class TenantStoreDTO {
	private Long tenantId;
	private Long storeId;
	private String applicationName;
	private Integer salaryCycleStartDay;
	private Integer summaryCalculationOffsetDays;
	private Integer salaryCalculationOffsetDays;
	private String biometricVendorName;
	private String timeZone;
	private Boolean isActualTimeBasedAttendance;
	private Instant timestampOfLastAttendanceRetrieval;
	private Integer recentSummaryCalculatedMonth;
	private Boolean retrieveAttendanceWithOtherStores;
	private String penaltyAbsentDays;
	private Boolean isPaidLeaveApplicable;

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

	public Integer getSalaryCycleStartDay() {
		return salaryCycleStartDay;
	}

	public void setSalaryCycleStartDay(Integer salaryCycleStartDay) {
		this.salaryCycleStartDay = salaryCycleStartDay;
	}

	public String getBiometricVendorName() {
		return biometricVendorName;
	}

	public void setBiometricVendorName(String biometricVendorName) {
		this.biometricVendorName = biometricVendorName;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public Boolean getIsActualTimeBasedAttendance() {
		return isActualTimeBasedAttendance;
	}

	public void setIsActualTimeBasedAttendance(Boolean isActualTimeBasedAttendance) {
		this.isActualTimeBasedAttendance = isActualTimeBasedAttendance;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public Integer getSummaryCalculationOffsetDays() {
		return summaryCalculationOffsetDays;
	}

	public void setSummaryCalculationOffsetDays(Integer summaryCalculationOffsetDays) {
		this.summaryCalculationOffsetDays = summaryCalculationOffsetDays;
	}

	public Integer getSalaryCalculationOffsetDays() {
		return salaryCalculationOffsetDays;
	}

	public void setSalaryCalculationOffsetDays(Integer salaryCalculationOffsetDays) {
		this.salaryCalculationOffsetDays = salaryCalculationOffsetDays;
	}

	public Instant getTimestampOfLastAttendanceRetrieval() {
		return timestampOfLastAttendanceRetrieval;
	}

	public void setTimestampOfLastAttendanceRetrieval(Instant timestampOfLastAttendanceRetrieval) {
		this.timestampOfLastAttendanceRetrieval = timestampOfLastAttendanceRetrieval;
	}

	public Integer getRecentSummaryCalculatedMonth() {
		return recentSummaryCalculatedMonth;
	}

	public void setRecentSummaryCalculatedMonth(Integer recentSummaryCalculatedMonth) {
		this.recentSummaryCalculatedMonth = recentSummaryCalculatedMonth;
	}

	public Boolean getRetrieveAttendanceWithOtherStores() {
		return retrieveAttendanceWithOtherStores;
	}

	public void setRetrieveAttendanceWithOtherStores(Boolean retrieveAttendanceWithOtherStores) {
		this.retrieveAttendanceWithOtherStores = retrieveAttendanceWithOtherStores;
	}

	public String getPenaltyAbsentDays() {
		return penaltyAbsentDays;
	}

	public void setPenaltyAbsentDays(String penaltyAbsentDays) {
		this.penaltyAbsentDays = penaltyAbsentDays;
	}

	public Boolean getIsPaidLeaveApplicable() {
		return isPaidLeaveApplicable;
	}

	public void setIsPaidLeaveApplicable(Boolean paidLeaveApplicable) {
		isPaidLeaveApplicable = paidLeaveApplicable;
	}
}
