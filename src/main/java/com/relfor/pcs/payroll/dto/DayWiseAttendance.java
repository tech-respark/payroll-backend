package com.relfor.pcs.payroll.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class DayWiseAttendance {
	private LocalDate dateOfAttendance;
	private String dayOfAttendance;
	private BigDecimal totalHoursWorkedInADay;
	private BigDecimal totalBreakTimeInADay;
	private List<IndividualPunches> individualPunchesList;

	public LocalDate getDateOfAttendance() {
		return dateOfAttendance;
	}

	public void setDateOfAttendance(LocalDate dateOfAttendance) {
		this.dateOfAttendance = dateOfAttendance;
	}

	public String getDayOfAttendance() {
		return dayOfAttendance;
	}

	public void setDayOfAttendance(String dayOfAttendance) {
		this.dayOfAttendance = dayOfAttendance;
	}

	public BigDecimal getTotalHoursWorkedInADay() {
		return totalHoursWorkedInADay;
	}

	public void setTotalHoursWorkedInADay(BigDecimal totalHoursWorkedInADay) {
		this.totalHoursWorkedInADay = totalHoursWorkedInADay;
	}

	public List<IndividualPunches> getIndividualPunchesList() {
		return individualPunchesList;
	}

	public void setIndividualPunchesList(List<IndividualPunches> individualPunchesList) {
		this.individualPunchesList = individualPunchesList;
	}

	public BigDecimal getTotalBreakTimeInADay() {
		return totalBreakTimeInADay;
	}

	public void setTotalBreakTimeInADay(BigDecimal totalBreakTimeInADay) {
		this.totalBreakTimeInADay = totalBreakTimeInADay;
	}

	@Override
	public String toString() {
		return "DayWiseAttendance{" +
				"dateOfAttendance=" + dateOfAttendance +
				", dayOfAttendance='" + dayOfAttendance + '\'' +
				", totalHoursWorkedInADay=" + totalHoursWorkedInADay +
				", totalBreakTimeInADay=" + totalBreakTimeInADay +
				", individualPunchesList=" + individualPunchesList +
				'}';
	}
}
