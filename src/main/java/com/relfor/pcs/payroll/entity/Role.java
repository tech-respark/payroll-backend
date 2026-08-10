package com.relfor.pcs.payroll.entity;

import jakarta.persistence.*;
@Entity
@Table(name = "s_role")
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "r_index")
    private Integer rIndex;
    
    @Column(name = "r_value")
    private Integer rValue;
    
    @Column(name = "active")
    private Integer active = 1;
    
    @Column(name = "tenant_id")
    private Long tenantId;
    
    @Column(name = "hide_from_ui")
    private Boolean hideFromUi;
    
    @Column(name = "assigned_reports", columnDefinition = "TEXT")
    private String assignedReports;
    
    @Column(name = "restriction_days")
    private Long restrictionDays;

    @Column(name = "permissions", columnDefinition = "TEXT")
    private String permissions;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getRIndex() {
		return rIndex;
	}

	public void setRIndex(Integer rIndex) {
		this.rIndex = rIndex;
	}

	public Integer getRValue() {
		return rValue;
	}

	public void setRValue(Integer rValue) {
		this.rValue = rValue;
	}

	public Integer getActive() {
		return active;
	}

	public void setActive(Integer active) {
		this.active = active;
	}

	public Long getTenantId() {
		return tenantId;
	}

	public void setTenantId(Long tenantId) {
		this.tenantId = tenantId;
	}

	public Boolean getHideFromUi() {
		return hideFromUi;
	}

	public void setHideFromUi(Boolean hideFromUi) {
		this.hideFromUi = hideFromUi;
	}

	public String getAssignedReports() {
		return assignedReports;
	}

	public void setAssignedReports(String assignedReports) {
		this.assignedReports = assignedReports;
	}

	public Long getRestrictionDays() {
		return restrictionDays;
	}

	public void setRestrictionDays(Long restrictionDays) {
		this.restrictionDays = restrictionDays;
	}

	public String getPermissions() {
		return permissions;
	}

	public void setPermissions(String permissions) {
		this.permissions = permissions;
	}
}
