package com.relfor.pcs.payroll.entity;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;

@Entity
@Table(name = "tenant_company_mapping")
public class TenantCompanyMapping {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "increment")
	@GenericGenerator(name = "increment", strategy = "increment")
	private Long id;
	private Long tenantId;
	private String tenantName;
	private String applicationName;
	private String personnelCompanyId;
	private Integer salaryCycleStartDay;
	private Integer summaryCalculationOffsetDays;
	private Integer salaryCalculationOffsetDays;
	private String biometricVendorName;
	private String vendorUrl;
	private String vendorCorporateId;
	private String vendorUserName;
	private String vendorPassword;
	private String penaltyAbsentDays;
	private Boolean isPaidLeaveApplicable;
	private String externalSoftwareUrl;
	private String externalAppName;

	public String getExternalSoftwareUrl() {
		return externalSoftwareUrl;
	}

	public void setExternalSoftwareUrl(String externalSoftwareUrl) {
		this.externalSoftwareUrl = externalSoftwareUrl;
	}

	public String getExternalAppName() {
		return externalAppName;
	}

	public void setExternalAppName(String externalAppName) {
		this.externalAppName = externalAppName;
	}

	public Integer getSalaryCycleStartDay() {
		return salaryCycleStartDay;
	}

	public void setSalaryCycleStartDay(Integer salaryCycleStartDay) {
		this.salaryCycleStartDay = salaryCycleStartDay;
	}

	public String getPersonnelCompanyId() {
		return personnelCompanyId;
	}

	public void setPersonnelCompanyId(String personnelCompanyId) {
		this.personnelCompanyId = personnelCompanyId;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getTenantName() {
		return tenantName;
	}

	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}

	public Long getTenantId() {
		return tenantId;
	}

	public void setTenantId(Long tenantId) {
		this.tenantId = tenantId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getSalaryCalculationOffsetDays() {
		return salaryCalculationOffsetDays;
	}

	public void setSalaryCalculationOffsetDays(Integer salaryCalculationOffsetDays) {
		this.salaryCalculationOffsetDays = salaryCalculationOffsetDays;
	}

	public String getBiometricVendorName() {
		return biometricVendorName;
	}

	public void setBiometricVendorName(String biometricVendorName) {
		this.biometricVendorName = biometricVendorName;
	}

	public Integer getSummaryCalculationOffsetDays() {
		return summaryCalculationOffsetDays;
	}

	public void setSummaryCalculationOffsetDays(Integer summaryCalculationOffsetDays) {
		this.summaryCalculationOffsetDays = summaryCalculationOffsetDays;
	}

	public String getVendorCorporateId() {
		return vendorCorporateId;
	}

	public void setVendorCorporateId(String vendorCorporateId) {
		this.vendorCorporateId = vendorCorporateId;
	}

	public String getVendorUserName() {
		return vendorUserName;
	}

	public void setVendorUserName(String vendorUserName) {
		this.vendorUserName = vendorUserName;
	}

	public String getVendorPassword() {
		return vendorPassword;
	}

	public void setVendorPassword(String vendorPassword) {
		this.vendorPassword = vendorPassword;
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

	public String getVendorUrl() {
		return vendorUrl;
	}

	public void setVendorUrl(String vendorUrl) {
		this.vendorUrl = vendorUrl;
	}
}