package com.relfor.pcs.payroll.service;

import com.relfor.pcs.payroll.dto.ResponseModel;
import com.relfor.pcs.payroll.entity.StoreDetails;
import com.relfor.pcs.payroll.handler.AttendanceRetrievalHandlerService;
import com.relfor.pcs.payroll.model.TenantStoreDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AttendanceRetrievalRoutingService {
	private final Map<String, AttendanceRetrievalHandlerService> attendanceRetrievalHandlers;

	@Autowired
	public AttendanceRetrievalRoutingService(Map<String, AttendanceRetrievalHandlerService> attendanceRetrievalHandlers) {
		this.attendanceRetrievalHandlers = attendanceRetrievalHandlers;
	}

	public String retrieveScheduledAttendanceDataFromVendor(String biometricVendorName, List<StoreDetails> storeDetailsList,
																   Map<String, Instant> invocationTimestampMap,
																   List<StoreDetails> storeDetailsListForMonthlySummary) {
		return getHandler(biometricVendorName).retrieveScheduledAttendanceDataFromVendor(storeDetailsList, invocationTimestampMap, storeDetailsListForMonthlySummary);
	}

	public void adhocRetrieveDataFromVendorAndSaveInDb(String biometricVendorName,
													   Long tenantId,
													   Long storeId,
													   String vendorUrl,
													   String corporateId,
													   String userName,
													   String password,
													   ZonedDateTime fromDateZoned,
													   ZonedDateTime toDateZoned,
													   Map<String, StoreDetails> terminalToStoreMap,
													   List<String> outputList) {
		getHandler(biometricVendorName).adhocRetrieveDataFromVendorAndSaveInDb(tenantId, storeId, vendorUrl, corporateId, userName, password, fromDateZoned, toDateZoned, terminalToStoreMap, outputList);
	}

	private AttendanceRetrievalHandlerService getHandler(String vendor) {
		if (StringUtils.isEmpty(vendor)) {
			throw new IllegalArgumentException("Biometric Vendor cannot be NULL or EMPTY");
		}
		AttendanceRetrievalHandlerService handler = attendanceRetrievalHandlers.get(vendor.toUpperCase());
		if (handler == null) {
			throw new IllegalArgumentException("No handler found for Vendor: " + vendor);
		}
		return handler;
	}
}