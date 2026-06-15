package com.relfor.pcs.payroll.dto;

import java.time.LocalDate;

public class PersonnelWorkExperienceDetailsDTO {
	private Long id;
	private Long personnelCode;
	private String companyName;
	private String designation;
	private LocalDate fromDate;
	private LocalDate toDate;

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
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
		return "PersonnelWorkExperienceDetailsDTO{" +
				"id=" + id +
				", personnelCode=" + personnelCode +
				", companyName='" + companyName + '\'' +
				", designation='" + designation + '\'' +
				", fromDate=" + fromDate +
				", toDate=" + toDate +
				'}';
	}
}
