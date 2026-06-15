package com.relfor.pcs.payroll.dto;

public class PersonnelDocumentDetailsDTO {
	private Long id;
	private String documentName;
	private String documentNumber;
	private Long personnelCode;

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

	public Long getPersonnelCode() {
		return personnelCode;
	}

	public void setPersonnelCode(Long personnelCode) {
		this.personnelCode = personnelCode;
	}

	@Override
	public String toString() {
		return "PersonnelDocumentDetailsDTO{" +
				"id=" + id +
				", documentName='" + documentName + '\'' +
				", documentNumber='" + documentNumber + '\'' +
				", personnelCode=" + personnelCode +
				'}';
	}
}
