package com.relfor.pcs.payroll.dto;
import java.util.List;
public class RoleCreateRequest {
    private String name;
    private String description;
    private Long tenantId; // Can be extracted from JWT or passed in request
    private Long storeId;
    private List<String> permissions;
    private List<String> assignedReports;
    private Integer rIndex;
    private Integer rValue;
    private Integer active;
    private Boolean hideFromUi;
    private Long restrictionDays;

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

	public List<String> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<String> permissions) {
		this.permissions = permissions;
	}

	public List<String> getAssignedReports() {
		return assignedReports;
	}

	public void setAssignedReports(List<String> assignedReports) {
		this.assignedReports = assignedReports;
	}

	public Integer getrIndex() {
		return rIndex;
	}

	public void setrIndex(Integer rIndex) {
		this.rIndex = rIndex;
	}

	public Integer getrValue() {
		return rValue;
	}

	public void setrValue(Integer rValue) {
		this.rValue = rValue;
	}

	public Integer getActive() {
		return active;
	}

	public void setActive(Integer active) {
		this.active = active;
	}

	public Boolean getHideFromUi() {
		return hideFromUi;
	}

	public void setHideFromUi(Boolean hideFromUi) {
		this.hideFromUi = hideFromUi;
	}

	public Long getRestrictionDays() {
		return restrictionDays;
	}

	public void setRestrictionDays(Long restrictionDays) {
		this.restrictionDays = restrictionDays;
	}
}
