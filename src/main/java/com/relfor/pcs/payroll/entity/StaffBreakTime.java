package com.relfor.pcs.payroll.entity;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;

@Entity
@Table(name = "staff_break_time")
public class StaffBreakTime extends BaseEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "increment")
	@GenericGenerator(name = "increment", strategy = "increment")
	private long id;
	private Long staffId;

	@Column(name = "staff_shift_id")
	private Long staffShiftId; // roaster id
	private String slot; // break time slot
	private String type;// type of break like lunch, tea
	private String remark;
//	private String breakDate;
	private Long totalWorkingHours;
	private Long breakHours;
//	private Boolean active;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Long getStaffId() {
		return staffId;
	}

	public void setStaffId(Long staffId) {
		this.staffId = staffId;
	}

	public String getSlot() {
		return slot;
	}

	public void setSlot(String slot) {
		this.slot = slot;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

//	public String getBreakDate() {
//		return breakDate;
//	}
//
//	public void setBreakDate(String breakDate) {
//		this.breakDate = breakDate;
//	}

	public Long getStaffShiftId() {
		return staffShiftId;
	}

	public void setStaffShiftId(Long staffShiftId) {
		this.staffShiftId = staffShiftId;
	}

	public Long getTotalWorkingHours() {
		return totalWorkingHours;
	}

	public void setTotalWorkingHours(Long totalWorkingHours) {
		this.totalWorkingHours = totalWorkingHours;
	}

	public Long getBreakHours() {
		return breakHours;
	}

	public void setBreakHours(Long breakHours) {
		this.breakHours = breakHours;
	}

//	public Boolean getActive() {
//		return active;
//	}
//
//	public void setActive(Boolean active) {
//		this.active = active;
//	}

}