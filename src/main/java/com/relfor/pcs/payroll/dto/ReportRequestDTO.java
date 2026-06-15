package com.relfor.pcs.payroll.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ReportRequestDTO<T> {

	private static final Logger logger = LoggerFactory.getLogger(ReportRequestDTO.class);

	private Long tenantId;
	private Long storeId;

	private Date fromDate;
	private Date toDate;

	private String fromDateStr;
	private String toDateStr;
	private String month;
	private String year;
	private T data;

	public ReportRequestDTO() {
	}

	public ReportRequestDTO(Long tenantId, Long storeId, Date fromDate, Date toDate, String fromDateStr, String toDateStr, String month, String year, T data) {
		this.tenantId = tenantId;
		this.storeId = storeId;

		this.setFromDate(fromDate);
		this.setToDate(toDate);

		if (fromDate == null && fromDateStr != null) {
			this.setFromDateStr(fromDateStr);
		}
		if (toDate == null && toDateStr != null) {
			this.setToDateStr(toDateStr);
		}

		this.month = month;
		this.year = year;
		this.data = data;
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

	public Date getFromDate() {
		return fromDate;
	}

	public void setFromDate(Date fromDate) {
		if (fromDate != null) {
			this.fromDate = new java.sql.Date(fromDate.getTime());
			this.fromDateStr = this.fromDate.toString();
		} else {
			this.fromDate = null;
			this.fromDateStr = null;
		}
	}

	public Date getToDate() {
		return toDate;
	}

	public void setToDate(Date toDate) {
		if (toDate != null) {
			this.toDate = new java.sql.Date(toDate.getTime());
			this.toDateStr = this.toDate.toString();
		} else {
			this.toDate = null;
			this.toDateStr = null;
		}
	}

	public String getFromDateStr() {
		return fromDateStr;
	}

	public void setFromDateStr(String fromDateStr) {
		this.fromDateStr = fromDateStr;
		if (fromDateStr != null && !fromDateStr.trim().isEmpty()) {
			try {
				Date parsedDate = new SimpleDateFormat("yyyy-MM-dd").parse(fromDateStr);
				this.fromDate = new java.sql.Date(parsedDate.getTime());
			} catch (Exception e) {
				logger.warn("Failed to parse fromDateStr '{}' to Date format 'yyyy-MM-dd'", fromDateStr, e);
				this.fromDate = null;
			}
		} else {
			this.fromDate = null;
		}
	}

	public String getToDateStr() {
		return toDateStr;
	}

	public void setToDateStr(String toDateStr) {
		this.toDateStr = toDateStr;
		if (toDateStr != null && !toDateStr.trim().isEmpty()) {
			try {
				Date parsedDate = new SimpleDateFormat("yyyy-MM-dd").parse(toDateStr);
				this.toDate = new java.sql.Date(parsedDate.getTime());
			} catch (Exception e) {
				logger.warn("Failed to parse toDateStr '{}' to Date format 'yyyy-MM-dd'", toDateStr, e);
				this.toDate = null;
			}
		} else {
			this.toDate = null;
		}
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "ReportRequestDTO{" +
				"tenantId=" + tenantId +
				", storeId=" + storeId +
				", fromDate=" + fromDate +
				", toDate=" + toDate +
				", fromDateStr='" + fromDateStr + '\'' +
				", toDateStr='" + toDateStr + '\'' +
				", month='" + month + '\'' +
				", year='" + year + '\'' +
				", data=" + data +
				'}';
	}
}
