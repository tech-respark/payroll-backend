package com.relfor.pcs.payroll.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class PersonnelEmergencyContactsDTO {
	private Long id;
	private Long staffId;
	private String contactPersonName;
	private String contactPersonMobile;
	private String relation;

	public Long getStaffId() {
		return staffId;
	}

	public void setStaffId(Long staffId) {
		this.staffId = staffId;
	}

	public String getContactPersonName() {
		return contactPersonName;
	}

	public void setContactPersonName(String contactPersonName) {
		this.contactPersonName = contactPersonName;
	}

	public String getContactPersonMobile() {
		return contactPersonMobile;
	}

	public void setContactPersonMobile(String contactPersonMobile) {
		this.contactPersonMobile = contactPersonMobile;
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "PersonnelEmergencyContactsDTO{" +
				"id=" + id +
				", staffId=" + staffId +
				", contactPersonName='" + contactPersonName + '\'' +
				", contactPersonMobile='" + contactPersonMobile + '\'' +
				", relation='" + relation + '\'' +
				'}';
	}
}
