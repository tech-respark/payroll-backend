package com.relfor.pcs.payroll.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "s_store_staff_role")
public class StoreStaffRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "store_id")
    private Long storeId;
    
    @Column(name = "staff_id")
    private Long staffId;
    
    @Column(name = "tenant_id")
    private Long tenantId;
    
    @Column(name = "role_id")
    private Long roleId;
    
    @Column(name = "active")
    private Integer active;
    
    @Column(name = "system_created_on")
    private LocalDateTime systemCreatedOn;
    
    @Column(name = "system_updated_on")
    private LocalDateTime systemUpdatedOn;
    
    @Column(name = "enable_appointments")
    private Integer enableAppointments = 0;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getStoreId() {
		return storeId;
	}

	public void setStoreId(Long storeId) {
		this.storeId = storeId;
	}

	public Long getStaffId() {
		return staffId;
	}

	public void setStaffId(Long staffId) {
		this.staffId = staffId;
	}

	public Long getTenantId() {
		return tenantId;
	}

	public void setTenantId(Long tenantId) {
		this.tenantId = tenantId;
	}

	public Long getRoleId() {
		return roleId;
	}

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}

	public Integer getActive() {
		return active;
	}

	public void setActive(Integer active) {
		this.active = active;
	}

	public LocalDateTime getSystemCreatedOn() {
		return systemCreatedOn;
	}

	public void setSystemCreatedOn(LocalDateTime systemCreatedOn) {
		this.systemCreatedOn = systemCreatedOn;
	}

	public LocalDateTime getSystemUpdatedOn() {
		return systemUpdatedOn;
	}

	public void setSystemUpdatedOn(LocalDateTime systemUpdatedOn) {
		this.systemUpdatedOn = systemUpdatedOn;
	}

	public Integer getEnableAppointments() {
		return enableAppointments;
	}

	public void setEnableAppointments(Integer enableAppointments) {
		this.enableAppointments = enableAppointments;
	}
}
