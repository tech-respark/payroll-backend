package com.relfor.pcs.payroll.entity;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "store_details")
public class StoreDetails {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "increment")
	@GenericGenerator(name = "increment", strategy = "increment")
	private Long id;
	private Long storeId;
	private String storeName;
	private String timeZone;
	private Boolean isActualTimeBasedAttendance;
	private Instant timestampOfLastAttendanceRetrieval;
	private Integer recentSummaryCalculatedMonth;
	private String storeOpenTime;
	private String storeCloseTime;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "tenant_company_mapping_id", referencedColumnName = "id")
	private TenantCompanyMapping tenantCompanyMapping;
	private Boolean retrieveAttendanceWithOtherStores;
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "store_details_id", referencedColumnName = "id")
	private List<TerminalDetails> terminalDetailsList;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public Boolean getIsActualTimeBasedAttendance() {
		return isActualTimeBasedAttendance;
	}

	public void setIsActualTimeBasedAttendance(Boolean isActualTimeBasedAttendance) {
		this.isActualTimeBasedAttendance = isActualTimeBasedAttendance;
	}

	public String getStoreName() {
		return storeName;
	}

	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}

	public Long getStoreId() {
		return storeId;
	}

	public void setStoreId(Long storeId) {
		this.storeId = storeId;
	}

	public TenantCompanyMapping getTenantCompanyMapping() {
		return tenantCompanyMapping;
	}

	public void setTenantCompanyMapping(TenantCompanyMapping tenantCompanyMapping) {
		this.tenantCompanyMapping = tenantCompanyMapping;
	}

	public Boolean getRetrieveAttendanceWithOtherStores() {
		return retrieveAttendanceWithOtherStores;
	}

	public void setRetrieveAttendanceWithOtherStores(Boolean retrieveAttendanceWithOtherStores) {
		this.retrieveAttendanceWithOtherStores = retrieveAttendanceWithOtherStores;
	}

	public List<TerminalDetails> getTerminalDetailsList() {
		return terminalDetailsList;
	}

	public void setTerminalDetailsList(List<TerminalDetails> terminalDetailsList) {
		this.terminalDetailsList = terminalDetailsList;
	}

	public Instant getTimestampOfLastAttendanceRetrieval() {
		return timestampOfLastAttendanceRetrieval;
	}

	public void setTimestampOfLastAttendanceRetrieval(Instant timeStampOfLastAttendanceRetrieval) {
		this.timestampOfLastAttendanceRetrieval = timeStampOfLastAttendanceRetrieval;
	}

	public Integer getRecentSummaryCalculatedMonth() {
		return recentSummaryCalculatedMonth;
	}

	public void setRecentSummaryCalculatedMonth(Integer recentSummaryCalculatedMonth) {
		this.recentSummaryCalculatedMonth = recentSummaryCalculatedMonth;
	}

	public String getStoreOpenTime() {
		return storeOpenTime;
	}

	public void setStoreOpenTime(String storeOpenTime) {
		this.storeOpenTime = storeOpenTime;
	}

	public String getStoreCloseTime() {
		return storeCloseTime;
	}

	public void setStoreCloseTime(String storeCloseTime) {
		this.storeCloseTime = storeCloseTime;
	}
}