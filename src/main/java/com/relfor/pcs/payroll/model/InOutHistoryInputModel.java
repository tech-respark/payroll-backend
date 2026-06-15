package com.relfor.pcs.payroll.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

public class InOutHistoryInputModel {
	private Long tenantId;
	private Long storeId;
	private String applicationName;
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate fromDate;
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate toDate;
	private String currentStatus;
	private Long personnelCode;
	private List<Long> personnelCodes;
	private String sortField;
	private String sortOrder;
	private String uploadSource;
	private PageModel pageModel;

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

	public LocalDate getFromDate() {
		return fromDate;
	}

	public void setFromDate(LocalDate fromDate) {
		this.fromDate = fromDate;
	}

	public LocalDate getToDate() {
		return toDate;
	}

	public void setToDate(LocalDate toDate) {
		this.toDate = toDate;
	}

	public List<Long> getPersonnelCodes() {
		return personnelCodes;
	}

	public void setPersonnelCodes(List<Long> personnelCodes) {
		this.personnelCodes = personnelCodes;
	}

	public String getCurrentStatus() {
		return currentStatus;
	}

	public void setCurrentStatus(String currentStatus) {
		this.currentStatus = currentStatus;
	}

	public PageModel getPageModel() {
		return pageModel;
	}

	public void setPageModel(PageModel pageModel) {
		this.pageModel = pageModel;
	}

	public String getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}

	public String getSortField() {
		return sortField;
	}

	public void setSortField(String sortField) {
		this.sortField = sortField;
	}

	public Long getPersonnelCode() {
		return personnelCode;
	}

	public void setPersonnelCode(Long personnelCode) {
		this.personnelCode = personnelCode;
	}

	public String getUploadSource() {
		return uploadSource;
	}

	public void setUploadSource(String uploadSource) {
		this.uploadSource = uploadSource;
	}
}
