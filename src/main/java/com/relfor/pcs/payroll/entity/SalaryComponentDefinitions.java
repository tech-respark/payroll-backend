package com.relfor.pcs.payroll.entity;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "salary_component_definitions")
public class SalaryComponentDefinitions {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "increment")
	@GenericGenerator(name = "increment", strategy = "increment")
	private Long id;
	private Long tenantId;
	private Long storeId;
	private String componentName;
	private String componentType; //FIXED or FORMULA
	private String componentCategory; //EARNING or DEDUCTION
	private Integer priorityIndex;
	private String componentNameAlias;
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "salary_component_definitions_id", referencedColumnName = "id")
	private List<SalaryComponentRules> salaryComponentRulesList;
	private Boolean isComputable = Boolean.FALSE;
	private Boolean includeInTotal = Boolean.FALSE;
	private Boolean isCalculatedMonthly = Boolean.FALSE;

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

	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	public String getComponentType() {
		return componentType;
	}

	public void setComponentType(String componentType) {
		this.componentType = componentType;
	}

	public String getComponentCategory() {
		return componentCategory;
	}

	public void setComponentCategory(String componentCategory) {
		this.componentCategory = componentCategory;
	}

	public Integer getPriorityIndex() {
		return priorityIndex;
	}

	public void setPriorityIndex(Integer priorityIndex) {
		this.priorityIndex = priorityIndex;
	}

	public String getComponentNameAlias() {
		return componentNameAlias;
	}

	public void setComponentNameAlias(String componentNameAlias) {
		this.componentNameAlias = componentNameAlias;
	}

	public List<SalaryComponentRules> getSalaryComponentRulesList() {
		return salaryComponentRulesList;
	}

	public void setSalaryComponentRulesList(List<SalaryComponentRules> salaryComponentRulesList) {
		this.salaryComponentRulesList = salaryComponentRulesList;
	}

	public Boolean getComputable() {
		return isComputable;
	}

	public void setComputable(Boolean computable) {
		isComputable = computable;
	}

	public Boolean getIncludeInTotal() {
		return includeInTotal;
	}

	public void setIncludeInTotal(Boolean includeInTotal) {
		this.includeInTotal = includeInTotal;
	}

	public Boolean getIsCalculatedMonthly() {
		return isCalculatedMonthly;
	}

	public void setIsCalculatedMonthly(Boolean isCalculatedMonthly) {
		this.isCalculatedMonthly = isCalculatedMonthly;
	}
}