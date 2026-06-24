package com.relfor.pcs.payroll.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonAlias;

public class PersonnelWorkExperienceDetailsDTO {
	private Long id;
	private Long staffId;
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

	public Long getStaffId() {
		return staffId;
	}

	public void setStaffId(Long staffId) {
		this.staffId = staffId;
	}

	@Override
	public String toString() {
		return "PersonnelWorkExperienceDetailsDTO{" +
				"id=" + id +
				", staffId=" + staffId +
				", companyName='" + companyName + '\'' +
				", designation='" + designation + '\'' +
				", fromDate=" + fromDate +
				", toDate=" + toDate +
				'}';
	}
}
