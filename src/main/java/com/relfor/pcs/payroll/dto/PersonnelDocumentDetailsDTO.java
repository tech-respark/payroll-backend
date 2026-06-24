package com.relfor.pcs.payroll.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class PersonnelDocumentDetailsDTO {
	private Long id;
	private String documentName;
	private String documentNumber;
	private Long staffId;

	public String getDocumentName() {
		return documentName;
	}

	public void setDocumentName(String documentName) {
		this.documentName = documentName;
	}

	public String getDocumentNumber() {
		return documentNumber;
	}

	public void setDocumentNumber(String documentNumber) {
		this.documentNumber = documentNumber;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getStaffId() {
		return staffId;
	}

	public void setStaffId(Long staffId) {
		this.staffId = staffId;
	}

	@Override
	public String toString() {
		return "PersonnelDocumentDetailsDTO{" +
				"id=" + id +
				", documentName='" + documentName + '\'' +
				", documentNumber='" + documentNumber + '\'' +
				", staffId=" + staffId +
				'}';
	}
}
