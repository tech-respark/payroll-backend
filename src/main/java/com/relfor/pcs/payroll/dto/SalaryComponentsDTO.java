package com.relfor.pcs.payroll.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAlias;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SalaryComponentsDTO {

	private Long tenantId;
	private Long storeId;
	private Long staffId;
	private String personnelName;
	private LocalDate salaryDate;
	private Map<String, BigDecimal> earnings;
	private Map<String, BigDecimal> deductions;
	private BigDecimal totalEarning;
	private BigDecimal totalDeduction;
	private BigDecimal salaryAmount;
	private Float totalDays;
	private Float totalWorkingDays;
	private Float absentDays;
	private Float penaltyAbsentDays;
	private Float totalHolidays;
	private Float totalweeklyOff;
	private Float totalPaidLeaves;
	private Float totalPaidDays;

	private String uanNumber;
	private String bankName;
	private String accountNumber;
	private String IfscCode;
	private String panNo;
	private String salaryPeriod;
	private String designation;
	private String employeeCode;
	private String storeName;
	private String salaryMonth;
	private Integer salaryYear;

	private List<SalaryComponentDTO> earningsList;
	private List<SalaryComponentDTO> deductionsList;

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

	public Long getStaffId() {
		return staffId;
	}

	public void setStaffId(Long staffId) {
		this.staffId = staffId;
	}

	public String getPersonnelName() {
		return personnelName;
	}

	public void setPersonnelName(String personnelName) {
		this.personnelName = personnelName;
	}

	public LocalDate getSalaryDate() {
		return salaryDate;
	}

	public void setSalaryDate(LocalDate salaryDate) {
		this.salaryDate = salaryDate;
	}

	public Map<String, BigDecimal> getEarnings() {
		return earnings;
	}

	public void setEarnings(Map<String, BigDecimal> earnings) {
		this.earnings = earnings;
	}

	public Map<String, BigDecimal> getDeductions() {
		return deductions;
	}

	public void setDeductions(Map<String, BigDecimal> deductions) {
		this.deductions = deductions;
	}

	public BigDecimal getTotalEarning() {
		return totalEarning;
	}

	public void setTotalEarning(BigDecimal totalEarning) {
		this.totalEarning = totalEarning;
	}

	public BigDecimal getTotalDeduction() {
		return totalDeduction;
	}

	public void setTotalDeduction(BigDecimal totalDeduction) {
		this.totalDeduction = totalDeduction;
	}

	public BigDecimal getSalaryAmount() {
		return salaryAmount;
	}

	public void setSalaryAmount(BigDecimal salaryAmount) {
		this.salaryAmount = salaryAmount;
	}

	public Float getTotalDays() {
		return totalDays;
	}

	public void setTotalDays(Float totalDays) {
		this.totalDays = totalDays;
	}

	public Float getTotalWorkingDays() {
		return totalWorkingDays;
	}

	public void setTotalWorkingDays(Float totalWorkingDays) {
		this.totalWorkingDays = totalWorkingDays;
	}

	public Float getAbsentDays() {
		return absentDays;
	}

	public void setAbsentDays(Float absentDays) {
		this.absentDays = absentDays;
	}

	public Float getTotalHolidays() {
		return totalHolidays;
	}

	public void setTotalHolidays(Float totalHolidays) {
		this.totalHolidays = totalHolidays;
	}

	public Float getTotalweeklyOff() {
		return totalweeklyOff;
	}

	public void setTotalweeklyOff(Float totalweeklyOff) {
		this.totalweeklyOff = totalweeklyOff;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getIfscCode() {
		return IfscCode;
	}

	public void setIfscCode(String IfscCode) {
		this.IfscCode = IfscCode;
	}

	public String getPanNo() {
		return panNo;
	}

	public void setPanNo(String panNo) {
		this.panNo = panNo;
	}

	public String getSalaryPeriod() {
		return salaryPeriod;
	}

	public void setSalaryPeriod(String salaryPeriod) {
		this.salaryPeriod = salaryPeriod;
	}

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}

	public String getEmployeeCode() {
		return employeeCode;
	}

	public void setEmployeeCode(String employeeCode) {
		this.employeeCode = employeeCode;
	}

	public String getStoreName() {
		return storeName;
	}

	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}

	public String getSalaryMonth() {
		return salaryMonth;
	}

	public void setSalaryMonth(String salaryMonth) {
		this.salaryMonth = salaryMonth;
	}

	public Integer getSalaryYear() {
		return salaryYear;
	}

	public void setSalaryYear(Integer salaryYear) {
		this.salaryYear = salaryYear;
	}

	public Float getPenaltyAbsentDays() {
		return penaltyAbsentDays;
	}

	public void setPenaltyAbsentDays(Float penaltyAbsentDays) {
		this.penaltyAbsentDays = penaltyAbsentDays;
	}

	public Float getTotalPaidLeaves() {
		return totalPaidLeaves;
	}

	public void setTotalPaidLeaves(Float totalPaidLeaves) {
		this.totalPaidLeaves = totalPaidLeaves;
	}

	public String getUanNumber() {
		return uanNumber;
	}

	public void setUanNumber(String uanNumber) {
		this.uanNumber = uanNumber;
	}

	public Float getTotalPaidDays() {
		return totalPaidDays;
	}

	public void setTotalPaidDays(Float totalPaidDays) {
		this.totalPaidDays = totalPaidDays;
	}

	public List<SalaryComponentDTO> getEarningsList() {
		return earningsList;
	}

	public void setEarningsList(List<SalaryComponentDTO> earningsList) {
		this.earningsList = earningsList;
	}

	public List<SalaryComponentDTO> getDeductionsList() {
		return deductionsList;
	}

	public void setDeductionsList(List<SalaryComponentDTO> deductionsList) {
		this.deductionsList = deductionsList;
	}
}
