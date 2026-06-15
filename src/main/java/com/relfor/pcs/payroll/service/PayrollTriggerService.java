package com.relfor.pcs.payroll.service;

import com.relfor.pcs.payroll.dto.ResponseModel;
import com.relfor.pcs.payroll.entity.*;
import com.relfor.pcs.payroll.projection.MonthlySummaryCalculationProjection;
import com.relfor.pcs.payroll.repository.DayWiseAttendanceSummaryRepository;
import com.relfor.pcs.payroll.repository.MonthWiseAttendanceSummaryRepository;
import com.relfor.pcs.payroll.repository.StoreDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class PayrollTriggerService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	StoreDetailsRepository storeDetailsRepository;
	@Autowired
	AttendanceRetrievalRoutingService attendanceRetrievalRoutingService;
	@Autowired
	MonthWiseAttendanceSummaryRepository monthWiseAttendanceSummaryRepository;
	@Autowired
	DayWiseAttendanceSummaryRepository dayWiseAttendanceSummaryRepository;
	@Autowired
	AttendanceManagementService attendanceManagementService;
	@Autowired
	SalaryCalculation salaryCalculationService;

	// Assumptions:
	// 1. All stores will be in same time zone
	// 2. If the time zones are different, this will be triggered separately separate stores
	// 3. If storeId is 0 then it will be processed for all the stores under that tenant and storeDetails.getRetrieveAttendanceWithOtherStores() will be assumed to be true
	@Transactional
	public ResponseModel retrieveAttendanceDataOnDemand(String applicationName, Long tenantId, Long storeId, Instant fromDate, Instant toDate) {
		ResponseModel responseModel = new ResponseModel();
		List<StoreDetails> storeDetailsList = storeDetailsRepository.fetchStoreDetailListForTenant(applicationName, tenantId, storeId);
		List<String> outputList = new ArrayList<>();
		if (!storeDetailsList.isEmpty()) {
			Map<String, StoreDetails> terminalToStoreMap = new HashMap<>();
			String biometricVendorName = storeDetailsList.get(0).getTenantCompanyMapping().getBiometricVendorName();
			String vendorUrl = storeDetailsList.get(0).getTenantCompanyMapping().getVendorUrl();
			String corporateId = storeDetailsList.get(0).getTenantCompanyMapping().getVendorCorporateId();
			String userName = storeDetailsList.get(0).getTenantCompanyMapping().getVendorUserName();
			String password = storeDetailsList.get(0).getTenantCompanyMapping().getVendorPassword();
			ZonedDateTime fromDateZoned = fromDate.atZone(ZoneId.of(storeDetailsList.get(0).getTimeZone()));
			if (toDate == null) {
				toDate = Instant.now();
			}
			ZonedDateTime toDateZoned = toDate.atZone(ZoneId.of(storeDetailsList.get(0).getTimeZone()));
			for (StoreDetails store : storeDetailsList) {
				if (store.getStoreId() != null && store.getTerminalDetailsList() != null) {
					for (TerminalDetails terminal : store.getTerminalDetailsList()) {
						if (terminal.getTerminalSerialNumber() != null) {
							terminalToStoreMap.put(store.getTenantCompanyMapping().getTenantId() + "-" + terminal.getTerminalSerialNumber(), store);
						}
					}
				}
			}
			attendanceRetrievalRoutingService.adhocRetrieveDataFromVendorAndSaveInDb(biometricVendorName, tenantId, storeId, applicationName, vendorUrl, corporateId, userName, password, fromDateZoned, toDateZoned, terminalToStoreMap, outputList);
		}
		responseModel.setData(outputList);
		responseModel.setCode(HttpStatus.OK);
		responseModel.setMessage("OK");
		return responseModel;
	}

	@Transactional
	public ResponseModel calculateMonthWiseSummary(String applicationName, Long tenantId, Long storeId, String month, Integer year) {
		List<StoreDetails> storeDetailsList = storeDetailsRepository.fetchStoreDetailListForTenant(applicationName, tenantId, storeId);
		List<String> outputList = new ArrayList<>();
		ResponseModel responseModel = new ResponseModel();
		if (!storeDetailsList.isEmpty()) {
			int salaryCycleStartDay = storeDetailsList.get(0).getTenantCompanyMapping().getSalaryCycleStartDay();
			int monthValue = Month.valueOf(month.toUpperCase()).getValue();
			LocalDate fromDate = LocalDate.of(year, monthValue, salaryCycleStartDay);
			LocalDate toDate;
			if (salaryCycleStartDay == 1) {
				toDate = fromDate.with(TemporalAdjusters.lastDayOfMonth());
			} else {
				toDate = LocalDate.of(monthValue == 12 ? year + 1 : year, monthValue == 12 ? 1 : monthValue + 1, salaryCycleStartDay - 1);
			}

			List<MonthWiseAttendanceSummary> existingMonthWiseSummaryList = monthWiseAttendanceSummaryRepository
					.getExistingMonthWiseSummaryList(applicationName, tenantId, storeId, month.toUpperCase(), year);

			outputList.add(String.format("No of existing summary record for the range given: %d",existingMonthWiseSummaryList.size()));

			List<MonthWiseAttendanceSummary> monthWiseAttendanceSummaryList = new ArrayList<>();
			if (storeId == 0) {
				for (StoreDetails storeDetails: storeDetailsList) {
					this.processStoreMonthlySummary(
							tenantId,
							storeDetails.getStoreId(),
							applicationName,
							fromDate,
							toDate,
							existingMonthWiseSummaryList,
							monthWiseAttendanceSummaryList,
							outputList
					);
				}
			} else {
				this.processStoreMonthlySummary(
						tenantId,
						storeId,
						applicationName,
						fromDate,
						toDate,
						existingMonthWiseSummaryList,
						monthWiseAttendanceSummaryList,
						outputList
				);
			}
			if (!monthWiseAttendanceSummaryList.isEmpty()) {
				monthWiseAttendanceSummaryRepository.saveAll(monthWiseAttendanceSummaryList);
				outputList.add(String.format("No of month wise records updated plus newly created: %d",monthWiseAttendanceSummaryList.size()));
			}
		}
		responseModel.setData(outputList);
		responseModel.setCode(HttpStatus.OK);
		responseModel.setMessage("OK");
		return responseModel;
	}

	private void processStoreMonthlySummary(
			Long tenantId,
			Long storeId,
			String applicationName,
			LocalDate fromDate,
			LocalDate toDate,
			List<MonthWiseAttendanceSummary> existingMonthWiseSummaryList,
			List<MonthWiseAttendanceSummary> monthWiseAttendanceSummaryList,
			List<String> outputList) {

		List<MonthlySummaryCalculationProjection> monthlySummaryCalculationProjectionList =
				dayWiseAttendanceSummaryRepository.calculateMonthlySummary(
						tenantId,
						storeId,
						applicationName,
						fromDate,
						toDate
				);

		if (!monthlySummaryCalculationProjectionList.isEmpty()) {
			for (MonthlySummaryCalculationProjection summary : monthlySummaryCalculationProjectionList) {
				Optional<MonthWiseAttendanceSummary> existingMonthWiseSummary =
						existingMonthWiseSummaryList.stream()
								.filter(s -> Objects.equals(s.getTenantId(), summary.getTenantId())
										&& Objects.equals(s.getStoreId(), summary.getStoreId())
										&& Objects.equals(s.getPersonnelCode(), summary.getPersonnelCode()))
								.findFirst();

				MonthWiseAttendanceSummary monthWiseAttendanceSummary =
						existingMonthWiseSummary.orElseGet(MonthWiseAttendanceSummary::new);

				attendanceManagementService.constructMonthlySummaryAndAddToList(
						monthWiseAttendanceSummaryList,
						monthWiseAttendanceSummary,
						summary,
						applicationName,
						tenantId,
						summary.getStoreId(),
						fromDate,
						toDate
				);
			}
		} else {
			outputList.add(String.format(
					"No record found in day wise summary for store %d in the date range",
					storeId
			));
		}
	}

	@Transactional
	public ResponseModel calculateSalary(String applicationName, Long tenantId, Long storeId, String month, Integer year) {
		logger.info("START Salary Calculation | Tenant: {} | Store: {} | Month: {} | Year: {}",
				tenantId, storeId, month, year);
		List<StoreDetails> storeDetailsList = storeDetailsRepository.fetchStoreDetailListForTenant(applicationName, tenantId, storeId);
		List<String> outputList = new ArrayList<>();
		ResponseModel responseModel = new ResponseModel();
		if (!storeDetailsList.isEmpty()) {
			List<MonthWiseAttendanceSummary> existingMonthWiseSummaryList = monthWiseAttendanceSummaryRepository
					.getExistingMonthWiseSummaryList(applicationName, tenantId, storeId, month.toUpperCase(), year);

			logger.info("Found {} attendance summaries for Tenant: {}", existingMonthWiseSummaryList.size(), tenantId);
			outputList.add(String.format("Number of records found in month wise summary: %d",existingMonthWiseSummaryList.size()));

			existingMonthWiseSummaryList.sort(Comparator.comparing(MonthWiseAttendanceSummary::getStoreId));
			Long currentStoreId = null;
			List<SalaryComponentDefinitions> salaryComponentDefinitionsList = null;
			boolean skipCurrentStore = false;
			for (MonthWiseAttendanceSummary monthWiseAttendanceSummary: existingMonthWiseSummaryList) {
				if (currentStoreId == null
						|| !Objects.equals(monthWiseAttendanceSummary.getStoreId(), currentStoreId)) {
					currentStoreId = monthWiseAttendanceSummary.getStoreId();
					salaryComponentDefinitionsList = salaryCalculationService.findSalaryCompenentDefinitionsList(monthWiseAttendanceSummary, currentStoreId);
					skipCurrentStore = ObjectUtils.isEmpty(salaryComponentDefinitionsList);
					if (skipCurrentStore) {
						outputList.add(String.format("Salary Definitions not found for the store: %d", currentStoreId));
					}
				}
				if (!skipCurrentStore) {
					PersonnelPayslipHistory personnelPayslipHistory = salaryCalculationService.calculateSalaryComponents(monthWiseAttendanceSummary, salaryComponentDefinitionsList, monthWiseAttendanceSummary.getPersonnelCode(), storeDetailsList);
					if (personnelPayslipHistory != null) {
						outputList.add(String.format("Processed salary calculation for store: %d and staff: %d", currentStoreId, monthWiseAttendanceSummary.getPersonnelCode()));
					} else {
						outputList.add(String.format("Salary details not found for store: %d and staff: %d", currentStoreId, monthWiseAttendanceSummary.getPersonnelCode()));
					}
				}
			}
		}
		responseModel.setData(outputList);
		responseModel.setCode(HttpStatus.OK);
		responseModel.setMessage("OK");
		logger.info("END Salary Calculation");
		return responseModel;
	}
}