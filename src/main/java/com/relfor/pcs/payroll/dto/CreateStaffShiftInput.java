package com.relfor.pcs.payroll.dto;

import com.relfor.pcs.payroll.entity.SStaffShifts;

import java.util.List;

public class CreateStaffShiftInput {

	Long tenantId;
	Long storeId;
	String startDate;
	Long noOfDays;
	Long createdBy;
	Long modifiedBy;
	List<SStaffShifts> staffShiftsList;
	Long shiftSlotId;

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

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public Long getNoOfDays() {
		return noOfDays;
	}

	public void setNoOfDays(Long noOfDays) {
		this.noOfDays = noOfDays;
	}

	public Long getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Long createdBy) {
		this.createdBy = createdBy;
	}

	public Long getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(Long modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public List<SStaffShifts> getStaffShiftsList() {
		return staffShiftsList;
	}

	public void setStaffShiftsList(List<SStaffShifts> staffShiftsList) {
		this.staffShiftsList = staffShiftsList;
	}

	public Long getShiftSlotId() {
		return shiftSlotId;
	}

	public void setShiftSlotId(Long shiftSlotId) {
		this.shiftSlotId = shiftSlotId;
	}

}
