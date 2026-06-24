package com.relfor.pcs.payroll.entity;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;

@Entity
@Table(name = "personnel_emergency_contact_details")
public class PersonnelEmergencyContactDetails extends BaseEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "increment")
	@GenericGenerator(name = "increment", strategy = "increment")
	private Long id;
	private Long personnelId;
	private String contactPersonName;
	private String contactPersonMobile;
	private String relation;

	// Getters and Setters
	public Long getId() { return id; }

	public void setId(Long id) { this.id = id; }

	public Long getPersonnelId() { return personnelId; }

	public void setPersonnelId(Long personnelId) { this.personnelId = personnelId; }

	public String getContactPersonName() { return contactPersonName; }

	public void setContactPersonName(String contactPersonName) { this.contactPersonName = contactPersonName; }

	public String getContactPersonMobile() { return contactPersonMobile; }

	public void setContactPersonMobile(String contactPersonMobile) { this.contactPersonMobile = contactPersonMobile; }

	public String getRelation() { return relation; }

	public void setRelation(String relation) { this.relation = relation; }
}