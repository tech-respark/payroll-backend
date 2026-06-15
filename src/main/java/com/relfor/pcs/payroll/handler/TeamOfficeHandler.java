package com.relfor.pcs.payroll.handler;

import com.relfor.pcs.payroll.dto.ResponseModel;
import com.relfor.pcs.payroll.dto.constants.BiometricEntryUploadSource;
import com.relfor.pcs.payroll.dto.constants.RegularizationRequestStatuses;
import com.relfor.pcs.payroll.dto.ShiftSlotDTO;
import com.relfor.pcs.payroll.entity.*;
import com.relfor.pcs.payroll.model.TeamOfficePunchDataModel;
import com.relfor.pcs.payroll.model.TeamOfficeResponseModel;
import com.relfor.pcs.payroll.model.TenantStoreDTO;
import com.relfor.pcs.payroll.repository.PersonnelAttendanceRepository;
import com.relfor.pcs.payroll.repository.PersonnelDetailsRepository;
import com.relfor.pcs.payroll.repository.StoreDetailsRepository;
import com.relfor.pcs.payroll.repository.TenantCompanyMappingRepository;
import com.relfor.pcs.payroll.util.ApiHelper;
import com.relfor.pcs.payroll.util.AsyncAttendanceSummaryCalculation;
//import io.swagger.models.auth.In;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.reactive.function.client.WebClient;

import javax.swing.text.html.Option;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service("TEAM_OFFICE")
public class TeamOfficeHandler implements AttendanceRetrievalHandlerService{
	private static final DateTimeFormatter dateTimeFormatterInput = DateTimeFormatter.ofPattern("dd/MM/yyyy_HH:mm");
	private static final DateTimeFormatter dateTimeFormatterOutput = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private WebClient.Builder webClientBuilder;
	@Autowired
	PersonnelDetailsRepository personnelDetailsRepository;
	@Autowired
	PersonnelAttendanceRepository personnelAttendanceRepository;
	@Autowired
	AsyncAttendanceSummaryCalculation asyncAttendanceSummaryCalculation;
	@Autowired
	StoreDetailsRepository storeDetailsRepository;
	@Autowired
	ApiHelper apiHelper;

	@Override
	public String retrieveScheduledAttendanceDataFromVendor(List<StoreDetails> storeDetailsList,
															Map<String, Instant> invocationTimestampMap,
															List<StoreDetails> storeDetailsListForMonthlySummary) {
		Map<String, StoreDetails> terminalToStoreMap = new HashMap<>();
		for (StoreDetails store : storeDetailsList) {
			if (store.getStoreId() != null && store.getTerminalDetailsList() != null) {
				for (TerminalDetails terminal : store.getTerminalDetailsList()) {
					if (terminal.getTerminalSerialNumber() != null) {
						terminalToStoreMap.put(store.getTenantCompanyMapping().getTenantId() + "-" + terminal.getTerminalSerialNumber(), store);
					}
				}
			}
		}

		storeDetailsList.sort(Comparator.comparing(store -> store.getTenantCompanyMapping().getTenantId()));
		TeamOfficeResponseModel teamOfficeResponseModel = null;
		Long tenantId = null;
		String applicationName = null;
		String corporateId = null;
		String userName = null;
		String password = null;
		String vendorUrl = null;
		HttpHeaders headers = apiHelper.getDefaultHeaders();
		for (StoreDetails storeDetails: storeDetailsList) {
			logger.info("Inside Team Office Handler for TenantId: {} & StoreId: {}", storeDetails.getTenantCompanyMapping().getTenantId(), storeDetails.getStoreId());
			corporateId = storeDetails.getTenantCompanyMapping().getVendorCorporateId();
			userName = storeDetails.getTenantCompanyMapping().getVendorUserName();
			password = storeDetails.getTenantCompanyMapping().getVendorPassword();
			vendorUrl = storeDetails.getTenantCompanyMapping().getVendorUrl();
			Instant invocationTimestampForStore = invocationTimestampMap.getOrDefault(storeDetails.getTenantCompanyMapping().getTenantId()
					+"-"+storeDetails.getStoreId(), null);
			if (tenantId == null
					|| !storeDetails.getRetrieveAttendanceWithOtherStores()
					|| !tenantId.equals(storeDetails.getTenantCompanyMapping().getTenantId())) {
				tenantId = storeDetails.getTenantCompanyMapping().getTenantId();
				applicationName = storeDetails.getTenantCompanyMapping().getApplicationName();
				teamOfficeResponseModel = null;
			} else {
				storeDetails.setTimestampOfLastAttendanceRetrieval(invocationTimestampForStore);
			}

			List<PersonnelAttendance> personnelAttendanceListForProcessing = new ArrayList<>();
			if (ObjectUtils.isEmpty(teamOfficeResponseModel)) {
				if (invocationTimestampForStore != null) {
					ZonedDateTime toDateZoned = invocationTimestampForStore.atZone(ZoneId.of(storeDetails.getTimeZone()));
					this.checkCriteriaAndAddToStoreDetailsListForMonthly(storeDetails, storeDetailsListForMonthlySummary, toDateZoned);
//					String toDate = zonedDateTime.format(dateTimeFormatterInput);
					ZonedDateTime fromDateZoned;
					if (ObjectUtils.isEmpty(storeDetails.getTimestampOfLastAttendanceRetrieval())) {
						fromDateZoned = toDateZoned.minusDays(1).toLocalDate().atStartOfDay(ZoneId.of(storeDetails.getTimeZone()));
					} else {
						fromDateZoned = storeDetails.getTimestampOfLastAttendanceRetrieval().plusSeconds(60).atZone(ZoneId.of(storeDetails.getTimeZone()));
					}

//					String authString = corporateId + ":" + userName + ":" + password + ":" + "true:";
//					String encodeAuthString = Base64.getEncoder().encodeToString(authString.getBytes());
//
//					teamOfficeResponseModel = this.retrievePunchDataFromVendorApi(vendorUrl, fromDate, toDate, encodeAuthString);\
					logger.info("Data needs to be fetched from an API for TenantId: {} & StoreId: {}", storeDetails.getTenantCompanyMapping().getTenantId(), storeDetails.getStoreId());
					teamOfficeResponseModel = this.constructArgumentsAndRetrieveDataFromVendor(vendorUrl,
							corporateId, userName, password, fromDateZoned, toDateZoned);
					storeDetails.setTimestampOfLastAttendanceRetrieval(invocationTimestampForStore);
					if (!ObjectUtils.isEmpty(teamOfficeResponseModel) && !Boolean.TRUE.equals(teamOfficeResponseModel.getError())) {
						this.savePersonnelAttendanceData(teamOfficeResponseModel, terminalToStoreMap, tenantId, applicationName, personnelAttendanceListForProcessing);
					}
				}
			} else if(invocationTimestampForStore != null) {
				logger.info("Data has been fetched already with other stores for TenantId: {} & StoreId: {}", storeDetails.getTenantCompanyMapping().getTenantId(), storeDetails.getStoreId());
				ZonedDateTime zonedDateTime = invocationTimestampForStore.atZone(ZoneId.of(storeDetails.getTimeZone()));
				this.checkCriteriaAndAddToStoreDetailsListForMonthly(storeDetails, storeDetailsListForMonthlySummary, zonedDateTime);
			}

			if (!personnelAttendanceListForProcessing.isEmpty()) {
				Map<Long, List<PersonnelAttendance>> attendanceByStoreId = personnelAttendanceListForProcessing.stream()
						.collect(Collectors.groupingBy(PersonnelAttendance::getStoreId));
				for (Map.Entry<Long, List<PersonnelAttendance>> entry: attendanceByStoreId.entrySet()) {
					if (!ObjectUtils.isEmpty(entry.getValue())) {
						asyncAttendanceSummaryCalculation.syncCalculateAttendanceSummaryForApprovalOrRejection(entry.getValue(), null, headers);
					}
				}
			}
		}
		storeDetailsRepository.saveAll(storeDetailsList);
		return "Attendance data retrieved from Team Office";
	}

	@Override
	public void adhocRetrieveDataFromVendorAndSaveInDb(Long tenantId,
													   Long storeId,
													   String applicationName,
													   String vendorUrl,
													   String corporateId,
													   String userName,
													   String password,
													   ZonedDateTime fromDateZoned,
													   ZonedDateTime toDateZoned,
													   Map<String, StoreDetails> terminalToStoreMap,
													   List<String> outputList) {
		List<PersonnelAttendance> personnelAttendanceListToBeDeleted = personnelAttendanceRepository
				.findExistingPersonnelAttendanceData(tenantId, storeId, applicationName, BiometricEntryUploadSource.API.name(),
						fromDateZoned.toLocalDateTime(), toDateZoned.toLocalDateTime());
		outputList.add(String.format("No of records to be deleted: %d", personnelAttendanceListToBeDeleted.size()));
		if (!personnelAttendanceListToBeDeleted.isEmpty()) {
			personnelAttendanceRepository.deleteAll(personnelAttendanceListToBeDeleted);
		}
		TeamOfficeResponseModel teamOfficeResponseModel = this.constructArgumentsAndRetrieveDataFromVendor(vendorUrl,
				corporateId, userName, password, fromDateZoned, toDateZoned);
		List<PersonnelAttendance> personnelAttendanceListForProcessing = new ArrayList<>();
		if (!ObjectUtils.isEmpty(teamOfficeResponseModel) && !Boolean.TRUE.equals(teamOfficeResponseModel.getError())) {
			this.savePersonnelAttendanceData(teamOfficeResponseModel, terminalToStoreMap, tenantId, applicationName, personnelAttendanceListForProcessing);
		}
		if (!personnelAttendanceListForProcessing.isEmpty()) {
			Map<Long, List<PersonnelAttendance>> attendanceByStoreId = personnelAttendanceListForProcessing.stream()
					.collect(Collectors.groupingBy(PersonnelAttendance::getStoreId));
			for (Map.Entry<Long, List<PersonnelAttendance>> entry : attendanceByStoreId.entrySet()) {
				if (!ObjectUtils.isEmpty(entry.getValue())) {
					HttpHeaders headers = apiHelper.getDefaultHeaders();
					asyncAttendanceSummaryCalculation.syncCalculateAttendanceSummaryForApprovalOrRejection(entry.getValue(), null, headers);
				}
			}
		}
		outputList.add(String.format("No of new entries added: %d", personnelAttendanceListForProcessing.size()));
	}

	public TeamOfficeResponseModel constructArgumentsAndRetrieveDataFromVendor(String vendorUrl,
																			   String corporateId,
																			   String userName,
																			   String password,
																			   ZonedDateTime fromDateZoned,
																			   ZonedDateTime toDateZoned) {

		String toDate = toDateZoned.format(dateTimeFormatterInput);
		String fromDate = fromDateZoned.format(dateTimeFormatterInput);
		String authString = corporateId + ":" + userName + ":" + password + ":" + "true:";
		String encodeAuthString = Base64.getEncoder().encodeToString(authString.getBytes());

		logger.info("Calling team office API from {} to {}", fromDate, toDate);
		return this.retrievePunchDataFromVendorApi(vendorUrl, fromDate, toDate, encodeAuthString);
	}

	private void checkCriteriaAndAddToStoreDetailsListForMonthly(StoreDetails storeDetails,
																 List<StoreDetails> storeDetailsListForMonthlySummary,
																 ZonedDateTime zonedDateTime) {
		Integer offset = storeDetails.getTenantCompanyMapping().getSummaryCalculationOffsetDays();
		Integer startDay = storeDetails.getTenantCompanyMapping().getSalaryCycleStartDay();
		Integer recentMonth = storeDetails.getRecentSummaryCalculatedMonth();
		int currentMonth = zonedDateTime.getMonthValue();
		if (offset != null) {
			int effectiveCutoffDay = (startDay == 1) ? offset : (startDay - 1 + offset);
			boolean isWithinCutoff = effectiveCutoffDay <= zonedDateTime.getDayOfMonth();
			boolean isLastMonthsSummaryCalculated = false;
			Integer expectedLastMonth = null;
			if (recentMonth != null) {
				expectedLastMonth = currentMonth == 1 ? 12 : currentMonth - 1;
				isLastMonthsSummaryCalculated = recentMonth.equals(expectedLastMonth);
			}
			if (isWithinCutoff && !isLastMonthsSummaryCalculated) {
				storeDetails.setRecentSummaryCalculatedMonth(expectedLastMonth != null ? expectedLastMonth : (currentMonth == 1 ? 12 : currentMonth -1));
				storeDetailsListForMonthlySummary.add(storeDetails);
			}
		}
	}

	private void savePersonnelAttendanceData(TeamOfficeResponseModel teamOfficeResponseModel,
											 Map<String, StoreDetails> terminalToStoreMap,
											 Long tenantId, String applicationName,
											 List<PersonnelAttendance> personnelAttendanceListForProcessing) {
		if (!ObjectUtils.isEmpty(teamOfficeResponseModel.getPunchDataModelList().isEmpty())) {
			List<TeamOfficePunchDataModel> punchDataModelList = teamOfficeResponseModel.getPunchDataModelList();
			punchDataModelList.sort(Comparator.comparing(TeamOfficePunchDataModel::getEmpCode, Comparator.nullsLast(String::compareTo)));
			List<String> uniqueEmpCodes = punchDataModelList.stream()
					.map(TeamOfficePunchDataModel::getEmpCode)
					.filter(Objects::nonNull)
					.distinct()
					.collect(Collectors.toList());
			List<PersonnelDetails> personnelDetailsList = personnelDetailsRepository.getPersonnelByEmployeeCode(tenantId, uniqueEmpCodes);
			PersonnelDetails personnelDetails = null;
			String currentEmpCode = null;
			List<PersonnelAttendance> personnelAttendanceList = new ArrayList<>();
			StoreDetails storeDetails = null;
			if (!personnelDetailsList.isEmpty()) {
				for (TeamOfficePunchDataModel individualPunches: punchDataModelList) {
					if (currentEmpCode == null || !StringUtils.equalsIgnoreCase(currentEmpCode, individualPunches.getEmpCode())) {
						currentEmpCode = individualPunches.getEmpCode();
						personnelDetails = null;
						if (!personnelAttendanceList.isEmpty() && !ObjectUtils.isEmpty(storeDetails)) {
							personnelAttendanceRepository.saveAll(personnelAttendanceList);
//							asyncAttendanceSummaryCalculation.syncCalculateAttendanceSummaryForApprovalOrRejection(new ArrayList<>(personnelAttendanceList), storeDetails.getIsActualTimeBasedAttendance(), headers);
							personnelAttendanceListForProcessing.addAll(personnelAttendanceList);
							personnelAttendanceList = new ArrayList<>();
						}
						storeDetails = terminalToStoreMap.getOrDefault(tenantId + "-" + individualPunches.getMcid(), null);
					}
					if (personnelDetails == null) {
						Optional<PersonnelDetails> personnelDetailsOptional =
								personnelDetailsList.stream().filter(pd ->
												StringUtils.equalsIgnoreCase(pd.getEmployeeCode(), individualPunches.getEmpCode()))
										.findFirst();
						if (personnelDetailsOptional.isPresent()) {
							personnelDetails = personnelDetailsOptional.get();
						}
					}

					if (personnelDetails != null) {
						LocalDateTime localDateTime = LocalDateTime.parse(individualPunches.getPunchDate(), dateTimeFormatterOutput);
						PersonnelAttendance personnelAttendance = new PersonnelAttendance();
						personnelAttendance.setTenantId(tenantId);
						personnelAttendance.setStoreId(storeDetails != null ? storeDetails.getStoreId() : 0);
						personnelAttendance.setApplicationName(applicationName);
						personnelAttendance.setTerminalSerialNumber(individualPunches.getMcid());
						personnelAttendance.setPersonnelCode(personnelDetails.getPersonnelCode());
						personnelAttendance.setAttendanceDate(localDateTime.toLocalDate());
						personnelAttendance.setAttendanceDayOfWeek(localDateTime.getDayOfWeek().name());
						personnelAttendance.setPunchTimestamp(storeDetails != null ? localDateTime.atZone(ZoneId.of(storeDetails.getTimeZone())).toInstant() : null);
						personnelAttendance.setUploadSource(BiometricEntryUploadSource.API.name());
						personnelAttendance.setCreatedTimestamp(Instant.now());
						personnelAttendance.setCreatedBy(0L);
						personnelAttendance.setModifiedTimestamp(Instant.now());
						personnelAttendance.setModifiedBy(0L);
						personnelAttendance.setCurrentStatus(RegularizationRequestStatuses.APPROVED.name());
						personnelAttendance.setVersionId(0L);
						personnelAttendanceList.add(personnelAttendance);
					}
				}
			}
			if (!personnelAttendanceList.isEmpty()) {
				logger.info("Total Number of punches to be saved for TenantId: {}, StoreId: {} is {}", personnelAttendanceList.get(0).getTenantId(),
						personnelAttendanceList.get(0).getStoreId(), personnelAttendanceList.size());
				personnelAttendanceRepository.saveAll(personnelAttendanceList);
				personnelAttendanceListForProcessing.addAll(personnelAttendanceList);
//				if (!ObjectUtils.isEmpty(storeDetails)) {
//					asyncAttendanceSummaryCalculation.syncCalculateAttendanceSummaryForApprovalOrRejection(personnelAttendanceList, storeDetails.getIsActualTimeBasedAttendance(), headers);
//				}
			}
		}
	}

	public TeamOfficeResponseModel retrievePunchDataFromVendorApi(String vendorUrl, String fromDate, String toDate, String encodedString) {
		final String baseUrl = vendorUrl + "?Empcode=ALL" + "&FromDate=" + fromDate + "&ToDate=" + toDate;
		return webClientBuilder.build()
				.get().uri(baseUrl)
				.header(HttpHeaders.AUTHORIZATION, "Basic " + encodedString)
				.retrieve().bodyToMono(TeamOfficeResponseModel.class).block();
	}
}
