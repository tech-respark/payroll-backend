package com.relfor.pcs.payroll.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class PersonnelDocumentDetailsDTO {
	private Long id;
	private String documentName;
	private String documentNumber;
	@JsonAlias({"personnelCode", "personnelId"})
	private Long personnelId;

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

	public Long getPersonnelId() {
		return personnelId;
	}

	public void setPersonnelId(Long personnelId) {
		this.personnelId = personnelId;
	}

	@Override
	public String toString() {
		return "PersonnelDocumentDetailsDTO{" +
				"id=" + id +
				", documentName='" + documentName + '\'' +
				", documentNumber='" + documentNumber + '\'' +
				", personnelId=" + personnelId +
				'}';
	}
}
