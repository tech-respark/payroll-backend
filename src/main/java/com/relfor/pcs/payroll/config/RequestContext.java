package com.relfor.pcs.payroll.config;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestContext {

	private String internalTraceId;
	private String token;
	private String userId;
	private String remoteIP;
	private String browser;
	private String latitude;
	private String longitude;
	private String storeId;
	private String tenantId;
	private String device;
	private String username;
	private List<Long> allowedStoreIdList;
	private String allowedStoreIds;

	public String getInternalTraceId() {
		return internalTraceId;
	}
	public void setInternalTraceId(String internalTraceId) {
		this.internalTraceId = internalTraceId;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getRemoteIP() {
		return remoteIP;
	}
	public void setRemoteIP(String remoteIP) {
		this.remoteIP = remoteIP;
	}
	public String getBrowser() {
		return browser;
	}
	public void setBrowser(String browser) {
		this.browser = browser;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	public String getStoreId() {
		return storeId;
	}
	public void setStoreId(String storeId) {
		this.storeId = storeId;
	}
	public String getTenantId() {
		return tenantId;
	}
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
	public String getDevice() {
		return device;
	}
	public void setDevice(String device) {
		this.device = device;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public List<Long> getAllowedStoreIdList() {
		return allowedStoreIdList;
	}

	public void setAllowedStoreIdList(List<Long> allowedStoreIdList) {
		this.allowedStoreIdList = allowedStoreIdList;
	}

	public String getAllowedStoreIds() {
		return allowedStoreIds;
	}

	public void setAllowedStoreIds(String allowedStoreIds) {
		this.allowedStoreIds = allowedStoreIds;
	}

	@Override
	public String toString() {
		return "RequestContext [internalTraceId=" + internalTraceId + ", token=" + token + ", userId=" + userId
				+ ", remoteIP=" + remoteIP + ", browser=" + browser + ", latitude=" + latitude + ", longitude="
				+ longitude + ", storeId=" + storeId + ", tenantId=" + tenantId + ", device=" + device + ", username="
				+ username + ", allowedStoreIdList=" + allowedStoreIdList + "]";
	}

	public void setAllowedStoreIdsFromHeader(String allowedStoresHeader) {
		try {
			if (allowedStoresHeader != null && !allowedStoresHeader.isBlank()) {
				// Remove brackets and whitespace: "[101, 102]" -> "101,102"
				String cleanHeader = allowedStoresHeader.replaceAll("[\\[\\]\\s]", "");

				if (!cleanHeader.isEmpty()) {
					this.allowedStoreIdList = Arrays.stream(cleanHeader.split(","))
							.map(Long::parseLong)
							.collect(Collectors.toList());
					this.allowedStoreIds = allowedStoresHeader;
					return;
				}
			}
		} catch (Exception e) {
		}
		// Fallback
		long defaultId = (this.storeId != null) ? Long.parseLong(this.storeId) : 0L;
		this.allowedStoreIdList = List.of(defaultId);
		this.allowedStoreIds = "[0]";
	}

}
