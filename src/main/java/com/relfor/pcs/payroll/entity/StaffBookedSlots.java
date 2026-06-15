package com.relfor.pcs.payroll.entity;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Date;

@Entity
@Table(name = "staff_booked_slots")
public class StaffBookedSlots {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	private String appointmentId;
	private long staffId;
	@Temporal(TemporalType.DATE)
	private Date appointmentDate;
	private String bookedSlot;
	private boolean isCanceled = false;
	private String service;
	@CreationTimestamp
	private Instant createdOn;
	@UpdateTimestamp
	private Instant modifiedOn;
	private long createdBy;
	private long modifiedBy;

	public long getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(long createdBy) {
		this.createdBy = createdBy;
	}

	public long getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(long modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public String getAppointmentId() {
		return appointmentId;
	}

	public void setAppointmentId(String appointmentId) {
		this.appointmentId = appointmentId;
	}

	public Instant getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Instant createdOn) {
		this.createdOn = createdOn;
	}

	public Instant getModifiedOn() {
		return modifiedOn;
	}

	public void setModifiedOn(Instant modifiedOn) {
		this.modifiedOn = modifiedOn;
	}

	public long getId() {
		return id;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getStaffId() {
		return staffId;
	}

	public void setStaffId(long staffId) {
		this.staffId = staffId;
	}

	public Date getAppointmentDate() {
		return appointmentDate;
	}

	public void setAppointmentDate(Date appointmentDate) {
		this.appointmentDate = appointmentDate;
	}

	public String getBookedSlot() {
		return bookedSlot;
	}

	public void setBookedSlot(String bookedSlot) {
		this.bookedSlot = bookedSlot;
	}

	public boolean isCanceled() {
		return isCanceled;
	}

	public void setCanceled(boolean isCanceled) {
		this.isCanceled = isCanceled;
	}

}