package com.relfor.pcs.payroll.entity;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "staff_shifts")
@EntityListeners(AuditingEntityListener.class)
public class SStaffShifts {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "increment")
	@GenericGenerator(name = "increment", strategy = "increment")
	private long id;

	@CreationTimestamp
	@Column(name = "created_on", nullable = true)
	private Instant createdOn;

	@UpdateTimestamp
	@Column(name = "modified_on", nullable = true)
	private Instant modifiedOn;

	@Column(name = "created_by", nullable = false)
	private long createdBy;

	@Column(name = "modified_by", nullable = false)
	private long modifiedBy;

	@Column(name = "tenant_id", nullable = false)
	private long tenantId;

	@Column(name = "store_id", nullable = false)
	private long storeId;

	@Column(name = "staff_id", nullable = false)
	private long staffId;

	@Column(name = "day", nullable = false)
	private String day;

	@Column(name = "shift_date", nullable = true)
	private LocalDate shiftDate;

	@Column(name = "slot", nullable = false)
	private String slot; // 10:00-15:00

	@Column(name = "on_leave", nullable = false)
	private boolean onLeave;

	@Column(name = "early_out_time", nullable = false)
	private String earlyOutTime;

	private boolean weeklyOff;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "staff_shift_id", referencedColumnName = "id")
	List<StaffBreakTime> staffBreakTime;

	private float productiveMinutes;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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

	public long getStaffId() {
		return staffId;
	}

	public void setStaffId(long staffId) {
		this.staffId = staffId;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public LocalDate getShiftDate() {
		return shiftDate;
	}

	public void setShiftDate(LocalDate shiftDate) {
		this.shiftDate = shiftDate;
	}

	public String getSlot() {
		return slot;
	}

	public void setSlot(String slot) {
		this.slot = slot;
	}

	public boolean getOnLeave() {
		return onLeave;
	}

	public void setOnLeave(boolean onLeave) {
		this.onLeave = onLeave;
	}

	public String getEarlyOutTime() {
		return earlyOutTime;
	}

	public void setEarlyOutTime(String earlyOutTime) {
		this.earlyOutTime = earlyOutTime;
	}

	public boolean getWeeklyOff() {
		return weeklyOff;
	}

	public void setWeeklyOff(boolean weeklyOff) {
		this.weeklyOff = weeklyOff;
	}

	public List<StaffBreakTime> getStaffBreakTime() {
		return staffBreakTime;
	}

	public void setStaffBreakTime(List<StaffBreakTime> staffBreakTime) {
		this.staffBreakTime = staffBreakTime;
	}

	public float getProductiveMinutes() {
		return productiveMinutes;
	}

	public void setProductiveMinutes(float productiveMinutes) {
		this.productiveMinutes = productiveMinutes;
	}

}