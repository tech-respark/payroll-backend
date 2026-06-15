package com.relfor.pcs.payroll.entity;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "shift_slots")
@EntityListeners(AuditingEntityListener.class)
public class SShiftsSlots extends BaseEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "increment")
	@GenericGenerator(name = "increment", strategy = "increment")
	private long id;

	@Column(name = "shift_name", nullable = false)
	private String shiftName;

	@Column(name = "start_time", nullable = false)
	private String startTime;

	@Column(name = "end_time", nullable = false)
	private String endTime;

	@Column(name = "tenant_id", nullable = false)
	private long tenantId;

	@Column(name = "store_id", nullable = false)
	private long storeId;

	@Column(columnDefinition = "boolean default true", nullable = false)
	private boolean active;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "break_type_id", referencedColumnName = "id")
	List<BreakTypes> breakTypes;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "shift_slot_id", referencedColumnName = "id")
	List<DayWiseShiftsTiming> dayWiseShiftsTiming;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getShiftName() {
		return shiftName;
	}

	public void setShiftName(String shiftName) {
		this.shiftName = shiftName;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public long getTenantId() {
		return tenantId;
	}

	public void setTenantId(long tenantId) {
		this.tenantId = tenantId;
	}

	public long getStoreId() {
		return storeId;
	}

	public void setStoreId(long storeId) {
		this.storeId = storeId;
	}

	public List<BreakTypes> getBreakTypes() {
		return breakTypes;
	}

	public void setBreakTypes(List<BreakTypes> breakTypes) {
		this.breakTypes = breakTypes;
	}

	public List<DayWiseShiftsTiming> getDayWiseShiftsTiming() {
		return dayWiseShiftsTiming;
	}

	public void setDayWiseShiftsTiming(List<DayWiseShiftsTiming> dayWiseShiftsTiming) {
		this.dayWiseShiftsTiming = dayWiseShiftsTiming;
	}

}