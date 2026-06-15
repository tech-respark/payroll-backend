package com.relfor.pcs.payroll.dto;

import java.time.Instant;
import java.util.Date;

public class ShiftSlotDTO {
	private Date appointmentDay;
	private String service;
	private Long expertId;
	private String slot;
	private Instant createdOn;
	private Instant modifiedOn;
	private String id;
	private boolean isCancelled;

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

	public Date getAppointmentDay() {
		return appointmentDay;
	}

	public void setAppointmentDay(Date appointmentDay) {
		this.appointmentDay = appointmentDay;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public Long getExpertId() {
		return expertId;
	}

	public void setExpertId(Long expertId) {
		this.expertId = expertId;
	}

	public String getSlot() {
		return slot;
	}

	public void setSlot(String slot) {
		this.slot = slot;
	}

	public boolean isCancelled() {
		return isCancelled;
	}

	public void setCancelled(boolean isCancelled) {
		this.isCancelled = isCancelled;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
