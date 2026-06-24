package com.relfor.pcs.payroll.entity;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "personnel_salary_components")
public class PersonnelSalaryComponents {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "increment")
	@GenericGenerator(name = "increment", strategy = "increment")
	private Long id;
	private Long tenantId;
	private Long storeId;
	private Long staffId;
	private String componentName;
	private BigDecimal annualValue;
	private BigDecimal monthlyValue;
	private BigDecimal dailyValue;
	private BigDecimal hourlyValue;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "salary_component_definitions_id", referencedColumnName = "id")
	private SalaryComponentDefinitions salaryComponentDefinitions;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public Long getStaffId() {
		return staffId;
	}

	public void setStaffId(Long staffId) {
		this.staffId = staffId;
	}

	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	public BigDecimal getAnnualValue() {
		return annualValue;
	}

	public void setAnnualValue(BigDecimal annualValue) {
		this.annualValue = annualValue;
	}

	public BigDecimal getMonthlyValue() {
		return monthlyValue;
	}

	public void setMonthlyValue(BigDecimal monthlyValue) {
		this.monthlyValue = monthlyValue;
	}

	public SalaryComponentDefinitions getSalaryComponentDefinitions() {
		return salaryComponentDefinitions;
	}

	public void setSalaryComponentDefinitions(SalaryComponentDefinitions salaryComponentDefinitions) {
		this.salaryComponentDefinitions = salaryComponentDefinitions;
	}

	public BigDecimal getDailyValue() {
		return dailyValue;
	}

	public void setDailyValue(BigDecimal dailyValue) {
		this.dailyValue = dailyValue;
	}

	public BigDecimal getHourlyValue() {
		return hourlyValue;
	}

	public void setHourlyValue(BigDecimal hourlyValue) {
		this.hourlyValue = hourlyValue;
	}
}