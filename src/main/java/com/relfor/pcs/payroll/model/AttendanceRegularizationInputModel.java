package com.relfor.pcs.payroll.model;

import com.relfor.pcs.payroll.dto.IndividualPunches;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;

public class AttendanceRegularizationInputModel {
	private Long tenantId;
	private Long storeId;
	@JsonAlias({"personnelCode", "personnelId"})
	private Long personnelId;
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate fromDate;
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate toDate;
	List<IndividualPunches> individualPunchesList;

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

	public Long getPersonnelId() {
		return personnelId;
	}

	public void setPersonnelId(Long personnelId) {
		this.personnelId = personnelId;
	}

	public List<IndividualPunches> getIndividualPunchesList() {
		return individualPunchesList;
	}

	public void setIndividualPunchesList(List<IndividualPunches> individualPunchesList) {
		this.individualPunchesList = individualPunchesList;
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
}
