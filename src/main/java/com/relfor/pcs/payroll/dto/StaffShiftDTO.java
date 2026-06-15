package com.relfor.pcs.payroll.dto;

import java.util.List;
import java.util.Map;

public class StaffShiftDTO {
	private long id;
	private String firstName;
	private String lastName;
	private List<Map<String, Object>> slots;

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public List<Map<String, Object>> getSlots() {
		return slots;
	}

	public void setSlots(List<Map<String, Object>> slots) {
		this.slots = slots;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

}
