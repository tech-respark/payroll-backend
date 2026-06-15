package com.relfor.pcs.payroll.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;

public class SalarySlipModel {
	private HashMap<String, BigDecimal> earnings;
	private HashMap<String, BigDecimal> deductions;
	private HashMap<String, BigDecimal> employerContributions;
	private BigDecimal totalEarnings;
	private BigDecimal totalDeductions;
	private BigDecimal netSalary;
	private BigDecimal costToCompany;
	private LocalDate salaryDate;
	private LocalDate salaryCycleFromDate;
	private LocalDate salaryCycleToDate;
	private String financialYear;
	private Float totalDaysInCycle;
	private Float totalWorkingDays;
	private Float totalHolidays;
	private Float totalAbsentDays;
	private Float totalPaidLeaves;
	private Float totalPaidDays;

	public HashMap<String, BigDecimal> getEarnings() {
		return earnings;
	}

	public void setEarnings(HashMap<String, BigDecimal> earnings) {
		this.earnings = earnings;
	}

	public HashMap<String, BigDecimal> getDeductions() {
		return deductions;
	}

	public void setDeductions(HashMap<String, BigDecimal> deductions) {
		this.deductions = deductions;
	}

	public HashMap<String, BigDecimal> getEmployerContributions() {
		return employerContributions;
	}

	public void setEmployerContributions(HashMap<String, BigDecimal> employerContributions) {
		this.employerContributions = employerContributions;
	}

	public BigDecimal getTotalEarnings() {
		return totalEarnings;
	}

	public void setTotalEarnings(BigDecimal totalEarnings) {
		this.totalEarnings = totalEarnings;
	}

	public BigDecimal getTotalDeductions() {
		return totalDeductions;
	}

	public void setTotalDeductions(BigDecimal totalDeductions) {
		this.totalDeductions = totalDeductions;
	}

	public BigDecimal getNetSalary() {
		return netSalary;
	}

	public void setNetSalary(BigDecimal netSalary) {
		this.netSalary = netSalary;
	}

	public BigDecimal getCostToCompany() {
		return costToCompany;
	}

	public void setCostToCompany(BigDecimal costToCompany) {
		this.costToCompany = costToCompany;
	}

	public LocalDate getSalaryDate() {
		return salaryDate;
	}

	public void setSalaryDate(LocalDate salaryDate) {
		this.salaryDate = salaryDate;
	}

	public LocalDate getSalaryCycleFromDate() {
		return salaryCycleFromDate;
	}

	public void setSalaryCycleFromDate(LocalDate salaryCycleFromDate) {
		this.salaryCycleFromDate = salaryCycleFromDate;
	}

	public LocalDate getSalaryCycleToDate() {
		return salaryCycleToDate;
	}

	public void setSalaryCycleToDate(LocalDate salaryCycleToDate) {
		this.salaryCycleToDate = salaryCycleToDate;
	}

	public String getFinancialYear() {
		return financialYear;
	}

	public void setFinancialYear(String financialYear) {
		this.financialYear = financialYear;
	}

	public Float getTotalDaysInCycle() {
		return totalDaysInCycle;
	}

	public void setTotalDaysInCycle(Float totalDaysInCycle) {
		this.totalDaysInCycle = totalDaysInCycle;
	}

	public Float getTotalWorkingDays() {
		return totalWorkingDays;
	}

	public void setTotalWorkingDays(Float totalWorkingDays) {
		this.totalWorkingDays = totalWorkingDays;
	}

	public Float getTotalHolidays() {
		return totalHolidays;
	}

	public void setTotalHolidays(Float totalHolidays) {
		this.totalHolidays = totalHolidays;
	}

	public Float getTotalAbsentDays() {
		return totalAbsentDays;
	}

	public void setTotalAbsentDays(Float totalAbsentDays) {
		this.totalAbsentDays = totalAbsentDays;
	}

	public Float getTotalPaidLeaves() {
		return totalPaidLeaves;
	}

	public void setTotalPaidLeaves(Float totalPaidLeaves) {
		this.totalPaidLeaves = totalPaidLeaves;
	}

	public Float getTotalPaidDays() {
		return totalPaidDays;
	}

	public void setTotalPaidDays(Float totalPaidDays) {
		this.totalPaidDays = totalPaidDays;
	}
}
