package com.relfor.pcs.payroll.entity;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "personnel_work_experience_details")
public class PersonnelWorkExperienceDetails {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "increment")
	@GenericGenerator(name = "increment", strategy = "increment")
	private Long id;
	private Long personnelCode;
	private String companyName;
	private String designation;
	private LocalDate fromDate;
	private LocalDate toDate;

	// Getters and Setters
	public Long getId() { return id; }

	public void setId(Long id) { this.id = id; }

	public Long getPersonnelCode() { return personnelCode; }

	public void setPersonnelCode(Long personnelCode) { this.personnelCode = personnelCode; }

	public String getCompanyName() { return companyName; }

	public void setCompanyName(String companyName) { this.companyName = companyName; }

	public String getDesignation() { return designation; }

	public void setDesignation(String designation) { this.designation = designation; }

	public LocalDate getFromDate() { return fromDate; }

	public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }

	public LocalDate getToDate() { return toDate; }

	public void setToDate(LocalDate toDate) { this.toDate = toDate; }
}