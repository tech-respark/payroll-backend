package com.relfor.pcs.payroll.util;
import com.relfor.pcs.payroll.dto.CommonConstants;
import com.relfor.pcs.payroll.util.SensitiveErrorFilter;

import com.relfor.pcs.payroll.dto.ConfigDTO;
import com.relfor.pcs.payroll.dto.SStaff;
import com.relfor.pcs.payroll.dto.STenantStore;
import com.relfor.pcs.payroll.dto.CommonConstants;
import com.relfor.pcs.payroll.dto.PrintBillDetailsDTO;
import com.relfor.pcs.payroll.config.RequestContext;

import  com.relfor.pcs.payroll.dto.ShiftSlotDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Objects;

@Component
public class ApiHelper {
	@Autowired
	protected RequestContext requestContext;
	@Value("${pcs_url}")
	private String pcsUrl;
	@Value("${pcs_txn_url}")
	private String pcsTxnUrl;
	@Value("${pcs_catalog_url}")
	private String pcsCatalogUrl;
	@Autowired
	private WebClient.Builder webClientBuilder;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public HttpHeaders getDefaultHeaders() {
		HttpHeaders headers = new HttpHeaders();
		try {
			headers.set(CommonConstants.INTERNAL_TRACE_ID, requestContext.getInternalTraceId());
			headers.set(CommonConstants.USER_ID, requestContext.getUserId());
			headers.set(CommonConstants.USERNAME, requestContext.getUsername());
			headers.set(CommonConstants.TENANT_ID, requestContext.getTenantId());
			headers.set(CommonConstants.STORE_ID, requestContext.getStoreId());
			headers.set(CommonConstants.AUTHORIZATION, requestContext.getToken());
			headers.set("clientIP", requestContext.getRemoteIP());
			headers.set(CommonConstants.LATITUDE, requestContext.getLatitude());
			headers.set(CommonConstants.LONGITUDE, requestContext.getLongitude());
			headers.set(CommonConstants.USER_AGENT, requestContext.getBrowser());
			headers.set(CommonConstants.ALLOWED_STORES, requestContext.getAllowedStoreIds());
		} catch (Exception e) {
			try {
				headers.set(CommonConstants.INTERNAL_TRACE_ID, MDC.get(CommonConstants.INTERNAL_TRACE_ID));
				headers.set(CommonConstants.USER_ID, MDC.get(CommonConstants.USER_ID));
				headers.set(CommonConstants.USERNAME, MDC.get(CommonConstants.USERNAME));
				headers.set(CommonConstants.TENANT_ID, MDC.get(CommonConstants.TENANT_ID));
				headers.set(CommonConstants.STORE_ID, MDC.get(CommonConstants.STORE_ID));
				headers.set(CommonConstants.AUTHORIZATION, MDC.get(CommonConstants.AUTHORIZATION));
				headers.set("clientIP", MDC.get("clientIP"));
				headers.set(CommonConstants.LATITUDE, MDC.get(CommonConstants.LATITUDE));
				headers.set(CommonConstants.LONGITUDE, MDC.get(CommonConstants.LONGITUDE));
				headers.set(CommonConstants.USER_AGENT, MDC.get(CommonConstants.USER_AGENT));
				headers.set(CommonConstants.ALLOWED_STORES, MDC.get(CommonConstants.ALLOWED_STORES));
			} catch (Exception ex) {
			}
		} finally {
			if (!headers.containsKey(CommonConstants.USER_AGENT)
					|| headers.getFirst(CommonConstants.USER_AGENT) == null
					|| Objects.requireNonNull(headers.getFirst(CommonConstants.USER_AGENT)).isEmpty()) {

				headers.set(CommonConstants.USER_AGENT, "Default-User-Agent");
			}
            headers.set(CommonConstants.CHECK, "OK");
		}
		return headers;
	}

	public void setHeaderInMDC(HttpHeaders headers) {
		try {
			MDC.put(CommonConstants.INTERNAL_TRACE_ID, headers.getFirst(CommonConstants.INTERNAL_TRACE_ID));
			MDC.put("clientIP", headers.getFirst("clientIP"));
			MDC.put(CommonConstants.USER_ID, headers.getFirst(CommonConstants.USER_ID));
			MDC.put(CommonConstants.TENANT_ID, headers.getFirst(CommonConstants.TENANT_ID));
			MDC.put(CommonConstants.STORE_ID, headers.getFirst(CommonConstants.STORE_ID));
			MDC.put(CommonConstants.USERNAME, headers.getFirst(CommonConstants.USERNAME));
			MDC.put(CommonConstants.AUTHORIZATION, headers.getFirst(CommonConstants.AUTHORIZATION));
			MDC.put(CommonConstants.LATITUDE, headers.getFirst(CommonConstants.LATITUDE));
			MDC.put(CommonConstants.LONGITUDE, headers.getFirst(CommonConstants.LONGITUDE));
			MDC.put(CommonConstants.USER_AGENT, headers.getFirst(CommonConstants.USER_AGENT));
			MDC.put(CommonConstants.ALLOWED_STORES, headers.getFirst(CommonConstants.ALLOWED_STORES));
		} catch (Exception e) {
		}
	}

	public List<SStaff> getStaffDataFromPcsForTenant(Long tenantId) {
		try {
            final String baseUrl = String.format("%s/pcs/v1/staffs/tenant?tenantId=%s", pcsUrl, tenantId);
			return webClientBuilder.build().get().uri(baseUrl).retrieve().bodyToMono(new ParameterizedTypeReference<List<SStaff>>() {
			}).block();
		} catch (Exception ex) {
			logger.error("Exception in getStaffDataFromPcsForTenant: {}", ex.getMessage());
			throw ex;
		}
	}

	public List<SStaff> getStaffsByTenantIdAndStoreId(long tenantId, long storeId) {
		try {
			String baseUrl = pcsUrl + "/pcs/v1/staffs?tenantId=" + tenantId + "&storeId=" + storeId;
			return webClientBuilder.build().get().uri(baseUrl).retrieve().bodyToMono(new ParameterizedTypeReference<List<SStaff>>() {
			}).block();
		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);
			throw e;
		}
	}

//	public SStaff getStaffById(long id) {
//		final String baseUrl = pcsUrl + "/pcs/v1/staffs/id/";
//		try {
//			return webClientBuilder.build().get().uri(baseUrl + id).retrieve().bodyToMono(SStaff.class).block();
//		} catch (Exception e) {
//			logger.error(e.getMessage());
//			throw e;
//		}
//	}

	public List<ShiftSlotDTO> getStaffBookedSlots(String startDate) {
		final String baseUrl = pcsTxnUrl + "/pcs-txn/v1/appointment/shifts/" + startDate;
		try {
			return webClientBuilder.build().get().uri(baseUrl).retrieve().bodyToMono(new ParameterizedTypeReference<List<ShiftSlotDTO>>() {
			}).block();
		} catch (Exception e) {
			logger.error("Error in get guests", e);
			throw e;
		}
	}

	public Long getCountOfStaff() {
		try {
			final String baseUrl = pcsUrl + "/pcs/v1/biometric/migration/countOfStaff";
			return webClientBuilder.build().get().uri(baseUrl).retrieve().bodyToMono(Long.class).block();
		} catch (Exception ex) {
			logger.error("Exception in getCountOfStaff: {}", ex.getMessage());
			throw ex;
		}
	}

	public List<SStaff> getAllStaffForMigration() {
		try {
			final String baseUrl = pcsUrl + "/pcs/v1/biometric/migration/getAllStaffForMigration";
			return webClientBuilder.build().get().uri(baseUrl).retrieve().bodyToMono(new ParameterizedTypeReference<List<SStaff>>() {
			}).block();
		} catch (Exception ex) {
			logger.error("Exception in getPaginatedStaffList: {}", ex.getMessage());
			throw ex;
		}
	}

	public List<SStaff> getPaginatedStaffList(int pageNumber, int recordsPerPage) {
		try {
			final String baseUrl = pcsUrl + "/pcs/v1/biometric/migration/getPaginatedStaffForMigration"
					+ "?pageNumber="+pageNumber+"&recordsPerPage="+recordsPerPage;
			return webClientBuilder.build().get().uri(baseUrl).retrieve().bodyToMono(new ParameterizedTypeReference<List<SStaff>>() {
			}).block();
		} catch (Exception ex) {
			logger.error("Exception in getPaginatedStaffList: {}", ex.getMessage());
			throw ex;
		}
	}

	public STenantStore getTenantStore(long tenantId, long storeId) {
		String baseUrl = String.format("%s/pcs/v1/tenants/stores/tenantStore?tenantId=%s&storeId=%s", pcsUrl, tenantId, storeId);
		try {
			return webClientBuilder.build().get().uri(baseUrl).retrieve().bodyToMono(STenantStore.class).block();
		} catch (Exception e) {
			throw e;
		}
	}

	public PrintBillDetailsDTO getBillDetails(long tenantId, long storeId, HttpHeaders headers) {
		final String baseUrl = pcsUrl + "/pcs/v1/printBillDetails?tenantId=" + tenantId + "&storeId=" + storeId;
		try {
			return webClientBuilder.build().get().uri(baseUrl).headers(h -> {
				h.addAll(headers);
			}).retrieve().bodyToMono(PrintBillDetailsDTO.class).block();
		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);
			throw e;
		}
	}

	public ConfigDTO getConfigForPostOrder(Long tenantId, Long storeId, String forMethod) {
		final String baseUrl = pcsCatalogUrl + "/pcs-catalog/v1/getSconfig?tenantId=" + tenantId + "&storeId=" + storeId
				+ "&forMethod=" + forMethod;
		try {
			ConfigDTO configForPostOrderDTO = webClientBuilder.build().get().uri(baseUrl)
					.headers(headers -> {
						headers.addAll(getDefaultHeaders());
					}).retrieve().bodyToMono(ConfigDTO.class).block();
			return configForPostOrderDTO;
		} catch (Exception e) {
			logger.error("Error while getting guest packages: {}", e.getMessage());
			throw e;
		}
	}
}