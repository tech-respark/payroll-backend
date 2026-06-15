package com.relfor.pcs.payroll.dto;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "payroll_scheduler_info")
public class PayrollSchedulerInfo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private Long tenantId;
	private Long storeId;
	
	private String event;
	private Instant invocationTime;
	private Boolean isProcessed;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getTenantId() {
		return tenantId;
	}

	public void setTenantId(Long tenantId) {
		this.tenantId = tenantId;
	}

	public Long getStoreId() {
		return storeId;
	}

	public void setStoreId(Long storeId) {
		this.storeId = storeId;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public Instant getInvocationTime() {
		return invocationTime;
	}

	public void setInvocationTime(Instant invocationTime) {
		this.invocationTime = invocationTime;
	}

	public Boolean getProcessed() {
		return isProcessed;
	}

	public void setProcessed(Boolean processed) {
		isProcessed = processed;
	}
}
