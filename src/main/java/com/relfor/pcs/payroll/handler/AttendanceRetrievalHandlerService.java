package com.relfor.pcs.payroll.handler;

import com.relfor.pcs.payroll.dto.ResponseModel;
import com.relfor.pcs.payroll.entity.StoreDetails;
import com.relfor.pcs.payroll.model.TenantStoreDTO;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public interface AttendanceRetrievalHandlerService {
	String retrieveScheduledAttendanceDataFromVendor(List<StoreDetails> storeDetailsList,
													 Map<String, Instant> invocationTimestampMap,
													 List<StoreDetails> storeDetailsListForMonthlySummary);

	void adhocRetrieveDataFromVendorAndSaveInDb(Long tenantId,
												Long storeId,
												String applicationName,
												String vendorUrl,
												String corporateId,
												String userName,
												String password,
												ZonedDateTime fromDateZoned,
												ZonedDateTime toDateZoned,
												Map<String, StoreDetails> terminalToStoreMap,
												List<String> outputList);
}
