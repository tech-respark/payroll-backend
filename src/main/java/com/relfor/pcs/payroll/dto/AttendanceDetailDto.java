package com.relfor.pcs.payroll.dto;

public class AttendanceDetailDto {
	private String day;
	private String date;
	private String slot;
	private Boolean onLeave;
	private Double hours;
	private Double hoursWorkedForADayAsPerBiometric;
	private Double breakTimeForADayAsPerBiometric;
	private String checkIn;
	private String checkOut;
	private String status;

	public AttendanceDetailDto(String day, String date, String slot, Boolean onLeave, Double hours, Double hoursWorkedForADayAsPerBiometric, Double breakTimeForADayAsPerBiometric, String checkIn, String checkOut, String status) {
		this.day = day;
		this.date = date;
		this.slot = slot;
		this.onLeave = onLeave;
		this.hours = hours;
		this.hoursWorkedForADayAsPerBiometric = hoursWorkedForADayAsPerBiometric;
		this.breakTimeForADayAsPerBiometric = breakTimeForADayAsPerBiometric;
		this.checkIn = checkIn;
		this.checkOut = checkOut;
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getSlot() {
		return slot;
	}

	public void setSlot(String slot) {
		this.slot = slot;
	}

	public Boolean getOnLeave() {
		return onLeave;
	}

	public void setOnLeave(Boolean onLeave) {
		this.onLeave = onLeave;
	}

	public Double getHours() {
		return hours;
	}

	public void setHours(Double hours) {
		this.hours = hours;
	}

	public Double getHoursWorkedForADayAsPerBiometric() {
		return hoursWorkedForADayAsPerBiometric;
	}

	public void setHoursWorkedForADayAsPerBiometric(Double hoursWorkedForADayAsPerBiometric) {
		this.hoursWorkedForADayAsPerBiometric = hoursWorkedForADayAsPerBiometric;
	}

	public Double getBreakTimeForADayAsPerBiometric() {
		return breakTimeForADayAsPerBiometric;
	}

	public void setBreakTimeForADayAsPerBiometric(Double breakTimeForADayAsPerBiometric) {
		this.breakTimeForADayAsPerBiometric = breakTimeForADayAsPerBiometric;
	}

	public String getCheckIn() {
		return checkIn;
	}

	public void setCheckIn(String checkIn) {
		this.checkIn = checkIn;
	}

	public String getCheckOut() {
		return checkOut;
	}

	public void setCheckOut(String checkOut) {
		this.checkOut = checkOut;
	}
}
