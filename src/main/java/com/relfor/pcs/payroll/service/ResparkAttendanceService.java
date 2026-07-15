package com.relfor.pcs.payroll.service;

import com.relfor.pcs.payroll.dto.ResponseModel;
import com.relfor.pcs.payroll.dto.IndividualPunches;
import com.relfor.pcs.payroll.dto.constants.BiometricApplicationNames;
import com.relfor.pcs.payroll.dto.constants.BiometricEntryUploadSource;
import com.relfor.pcs.payroll.dto.constants.RegularizationRequestStatuses;
import com.relfor.pcs.payroll.entity.PersonnelAttendance;
import com.relfor.pcs.payroll.entity.StoreDetails;
import com.relfor.pcs.payroll.model.*;
import com.relfor.pcs.payroll.projection.TenantStoreProjection;
import com.relfor.pcs.payroll.repository.*;
import com.relfor.pcs.payroll.util.AsyncAttendanceSummaryCalculation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ResparkAttendanceService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String SUCCESS = "SUCCESS";
	@Autowired
	TenantCompanyMappingRepository tenantCompanyMappingRepository;
	@Autowired
	AsyncAttendanceSummaryCalculation asyncAttendanceSummaryCalculation;
	@Autowired
	PersonnelAttendanceCustomRepo personnelAttendanceCustomRepo;
	@Autowired
	PersonnelAttendanceRepository personnelAttendanceRepository;
	@PersistenceContext
	private EntityManager entityManager;
	@Autowired
	StoreDetailsRepository storeDetailsRepository;
	@Autowired
	StoreProfileConfigRepository storeProfileConfigRepository;
	
	public ResponseModel regularizeAttendance(AttendanceRegularizationInputModel attendanceRegularizationInputModel){
		ResponseModel responseModel = new ResponseModel();
		try{
						Optional<TenantStoreProjection> tenantStoreProjectionOptional =
					tenantCompanyMappingRepository.getTenantStoreMapping(attendanceRegularizationInputModel.getTenantId(),
							attendanceRegularizationInputModel.getStoreId(), BiometricApplicationNames.RESPARK.name());
			boolean isActualTimeBasedAttendance = false;
			ZoneId zoneId = ZoneId.systemDefault();
			if (tenantStoreProjectionOptional.isPresent()) {
				isActualTimeBasedAttendance = tenantStoreProjectionOptional.get().getIsActualTimeBasedAttendance();
				zoneId = ZoneId.of(tenantStoreProjectionOptional.get().getTimeZone());
			}

			List<LocalDate> dateOfAttendanceList = this.createDateOfAttendanceList(attendanceRegularizationInputModel.getFromDate(),
					attendanceRegularizationInputModel.getToDate());

			List<IndividualPunches> inputIndividualPunchesList = attendanceRegularizationInputModel.getIndividualPunchesList();

			List<PersonnelAttendance> approvedOrRejectedAttendanceList = new ArrayList<>();
			List<PersonnelAttendance> personnelAttendanceListToBeSaved = new ArrayList<>();
			if (!dateOfAttendanceList.isEmpty()
					&& !attendanceRegularizationInputModel.getIndividualPunchesList().isEmpty()) {
				for (LocalDate dateOfAttendance: dateOfAttendanceList) {
					logger.info("Regularizing punches for date: {}", dateOfAttendance);
					this.processIndividualPunches(inputIndividualPunchesList,
							attendanceRegularizationInputModel,
							personnelAttendanceListToBeSaved,
							approvedOrRejectedAttendanceList,
							dateOfAttendance, zoneId);
				}
			}

			if (!personnelAttendanceListToBeSaved.isEmpty()) {
				personnelAttendanceRepository.saveAll(personnelAttendanceListToBeSaved);

				List<IndividualPunches> outputIndividualPunchesList = new ArrayList<>();
				for (PersonnelAttendance personnelAttendance: personnelAttendanceListToBeSaved) {
					IndividualPunches outputIndividualPunch = this.createIndividualPunch(personnelAttendance, zoneId);
					outputIndividualPunchesList.add(outputIndividualPunch);
				}

				attendanceRegularizationInputModel.setIndividualPunchesList(outputIndividualPunchesList);
			}

			if (!approvedOrRejectedAttendanceList.isEmpty()) {
				asyncAttendanceSummaryCalculation.asyncCalculateAttendanceSummaryForApprovalOrRejection(approvedOrRejectedAttendanceList, isActualTimeBasedAttendance);
			}

			responseModel.setData(attendanceRegularizationInputModel);
			responseModel.setCode(HttpStatus.OK);
			responseModel.setMessage(SUCCESS);
		} catch (Exception ex) {
			logger.error("Exception inside regularizeAttendance", ex);
			throw ex;
		}
		return responseModel;
	}

	private List<LocalDate> createDateOfAttendanceList(LocalDate fromDate, LocalDate toDate) {
		List<LocalDate> dateOfAttendanceList = new ArrayList<>();
		LocalDate currentDate = fromDate;
		while (!currentDate.isAfter(toDate)) {
			dateOfAttendanceList.add(currentDate);
			currentDate = currentDate.plusDays(1);
		}
		return dateOfAttendanceList;
	}

	private void processIndividualPunches(List<IndividualPunches> individualPunchesList,
											 AttendanceRegularizationInputModel attendanceRegularizationInputModel,
											 List<PersonnelAttendance> personnelAttendanceListToBeSaved,
											 List<PersonnelAttendance> approvedOrRejectedAttendanceList,
											 LocalDate dateOfAttendance,
											 ZoneId zoneId) {
		for (IndividualPunches individualPunch : individualPunchesList) {
			if (ObjectUtils.isEmpty(individualPunch.getPersonnelAttendanceId())) {
				this.handleNewPunch(individualPunch, attendanceRegularizationInputModel,
						personnelAttendanceListToBeSaved,
						approvedOrRejectedAttendanceList,
						dateOfAttendance, zoneId);
			} else {
				this.handleExistingPunch(individualPunch,
						personnelAttendanceListToBeSaved,
						approvedOrRejectedAttendanceList,
						dateOfAttendance, zoneId);
			}
		}
	}

	private void handleNewPunch(IndividualPunches individualPunch,
								AttendanceRegularizationInputModel attendanceRegularizationInputModel,
								List<PersonnelAttendance> personnelAttendanceListToBeSaved,
								List<PersonnelAttendance> approvedOrRejectedAttendanceList,
								LocalDate dateOfAttendance,
								ZoneId zoneId) {
		PersonnelAttendance personnelAttendance = new PersonnelAttendance();
		personnelAttendance.setTenantId(attendanceRegularizationInputModel.getTenantId());
		personnelAttendance.setStoreId(attendanceRegularizationInputModel.getStoreId());
		personnelAttendance.setApplicationName(BiometricApplicationNames.RESPARK.name());
		personnelAttendance.setAttendanceDate(dateOfAttendance);
		personnelAttendance.setAttendanceDayOfWeek(dateOfAttendance.getDayOfWeek().name());
		personnelAttendance.setCreatedBy(individualPunch.getCreatedBy());
		personnelAttendance.setCreatedTimestamp(Instant.now());
		personnelAttendance.setModifiedBy(individualPunch.getCreatedBy());
		personnelAttendance.setModifiedTimestamp(Instant.now());
		personnelAttendance.setCurrentStatus(individualPunch.getCurrentStatus());
		personnelAttendance.setStaffId(attendanceRegularizationInputModel.getStaffId());
		personnelAttendance.setPunchTimestamp(this.getPunchTimestamp(individualPunch, dateOfAttendance, zoneId));
		personnelAttendance.setPunchEvent(individualPunch.getPunchEvent());
		personnelAttendance.setUploadSource(BiometricEntryUploadSource.REGULARIZATION.name());
		personnelAttendance.setRemark(individualPunch.getRemark());

		logger.info("Adding new punch while regularizing: {}", personnelAttendance.toLogString());
		personnelAttendanceListToBeSaved.add(personnelAttendance);
		if (personnelAttendance.getCurrentStatus().equalsIgnoreCase(RegularizationRequestStatuses.APPROVED.name())
				|| personnelAttendance.getCurrentStatus().equalsIgnoreCase(RegularizationRequestStatuses.REJECTED.name())) {
			approvedOrRejectedAttendanceList.add(personnelAttendance);
		}
	}

	private void handleExistingPunch(IndividualPunches individualPunch,
									 List<PersonnelAttendance> personnelAttendanceListToBeSaved,
									 List<PersonnelAttendance> approvedOrRejectedAttendanceList,
									 LocalDate dateOfAttendance,
									 ZoneId zoneId) {
		Optional<PersonnelAttendance> personnelAttendanceOptional = personnelAttendanceRepository
				.findById(individualPunch.getPersonnelAttendanceId());
		if (personnelAttendanceOptional.isPresent()) {
			personnelAttendanceOptional.get().setAttendanceDate(dateOfAttendance);
			personnelAttendanceOptional.get().setAttendanceDayOfWeek(dateOfAttendance.getDayOfWeek().name());
			personnelAttendanceOptional.get().setPunchTimestamp(this.getPunchTimestamp(individualPunch, dateOfAttendance, zoneId));
			personnelAttendanceOptional.get().setPunchEvent(individualPunch.getPunchEvent());
			personnelAttendanceOptional.get().setModifiedBy(individualPunch.getCreatedBy());
			personnelAttendanceOptional.get().setModifiedTimestamp(Instant.now());
			personnelAttendanceOptional.get().setCurrentStatus(individualPunch.getCurrentStatus());

			logger.info("Updating existing punch while regularizing: {}", personnelAttendanceOptional.get().toLogString());
			personnelAttendanceListToBeSaved.add(personnelAttendanceOptional.get());

			if (personnelAttendanceOptional.get().getCurrentStatus().equalsIgnoreCase(RegularizationRequestStatuses.APPROVED.name())
					|| personnelAttendanceOptional.get().getCurrentStatus().equalsIgnoreCase(RegularizationRequestStatuses.REJECTED.name())) {
				approvedOrRejectedAttendanceList.add(personnelAttendanceOptional.get());
			}
		} else {
			logger.info("Personnel Attendance Request does not exist for ID: {}", individualPunch.getPersonnelAttendanceId());
		}
	}

	private Instant getPunchTimestamp(IndividualPunches individualPunch, LocalDate dateOfAttendance, ZoneId zoneId) {
		LocalDateTime localDateTime = dateOfAttendance.atTime(individualPunch.getPunchTime());
		if (individualPunch.getPunchDateOffset() != null
				&& individualPunch.getPunchDateOffset() != 0) {
			localDateTime = localDateTime.plusDays(individualPunch.getPunchDateOffset());
		}
		return localDateTime.atZone(zoneId).toInstant();
	}

	public ResponseModel getRegularizationRequests(InOutHistoryInputModel inOutHistoryInputModel) {
		ResponseModel responseModel = new ResponseModel();
		try {
			RegularizationRequestsOutputModel regularizationRequestsOutputModel = new RegularizationRequestsOutputModel();
			List<AttendanceRequestsDTO> regularizationRequests = new ArrayList<>();
			PageModel pageModel = new PageModel();
			Optional<TenantStoreProjection> tenantStoreProjectionOptional =
					tenantCompanyMappingRepository.getTenantStoreMapping(inOutHistoryInputModel.getTenantId(),
							inOutHistoryInputModel.getStoreId(), BiometricApplicationNames.RESPARK.name());
			ZoneId zoneId = tenantStoreProjectionOptional.map(tenantStoreProjection -> ZoneId.of(tenantStoreProjection.getTimeZone())).orElseGet(ZoneId::systemDefault);
			inOutHistoryInputModel.setUploadSource(BiometricEntryUploadSource.REGULARIZATION.name());
			Page<AttendanceRequestsDTO> attendanceRequestsDTOPage = personnelAttendanceCustomRepo.getRegularizationRequests(inOutHistoryInputModel, zoneId);
			if (!attendanceRequestsDTOPage.isEmpty() &&
					!attendanceRequestsDTOPage.getContent().isEmpty()) {
				regularizationRequests = attendanceRequestsDTOPage.getContent();
				List<AttendanceRequestsDTO> modifiableRegularizationRequestsList = new ArrayList<>(regularizationRequests);
				modifiableRegularizationRequestsList.sort(Comparator
						.comparing(AttendanceRequestsDTO::getStaffId)
						.thenComparing(AttendanceRequestsDTO::getAttendanceDate));
				pageModel.setTotalNumberOfRecords(attendanceRequestsDTOPage.getTotalElements());

				List<IndividualPunches> individualPunchesList = this.findAndCreateIndividualPunchesListFromDbUsingJpql(modifiableRegularizationRequestsList,
						inOutHistoryInputModel.getTenantId(), inOutHistoryInputModel.getStoreId(), zoneId);
				if (!individualPunchesList.isEmpty()) {
					for (AttendanceRequestsDTO attendanceRequest: regularizationRequests) {
						attendanceRequest.setIndividualPunchesList(individualPunchesList.stream()
								.filter(req -> Objects.equals(req.getStaffId(), attendanceRequest.getStaffId())
										&& req.getAttendanceDate().isEqual(attendanceRequest.getAttendanceDate())).collect(Collectors.toList()));
					}
				}
			} else {
				pageModel.setTotalNumberOfRecords(0L);
			}
			regularizationRequestsOutputModel.setRegularizationRequests(regularizationRequests);
			pageModel.setPageNumber(inOutHistoryInputModel.getPageModel().getPageNumber());
			pageModel.setRecordsPerPage(inOutHistoryInputModel.getPageModel().getRecordsPerPage());
			regularizationRequestsOutputModel.setPageModel(pageModel);

			responseModel.setCode(HttpStatus.OK);
			responseModel.setMessage(SUCCESS);
			responseModel.setData(regularizationRequestsOutputModel);
		} catch (Exception ex) {
			logger.error("Exception in getRegularizationRequests: {}", ex.getMessage());
			throw ex;
		}
		return responseModel;
	}

	private List<IndividualPunches> findAndCreateIndividualPunchesListFromDbUsingJpql(List<AttendanceRequestsDTO> regularizationRequests,
																						Long tenantId, Long storeId, ZoneId zoneId) {
		StringBuilder jpqlBuilderForPersonnelAttendance = new StringBuilder("SELECT p FROM PersonnelAttendance p WHERE p.tenantId = :tenantId AND p.storeId = :storeId AND (");
		List<String> personnelAndDatesConditions = new ArrayList<>();
		int index = 0;
		Map<String, Object> personnelAndDatesParameters = new HashMap<>();

		Long staffId = null;
		List<LocalDate> attendanceDates = new ArrayList<>();
		String personnelParam = null;
		String dateParam = null;
		for (int i = 0; i < regularizationRequests.size(); i++) {
			AttendanceRequestsDTO attendanceRequest = regularizationRequests.get(i);
			Long currentCode = attendanceRequest.getStaffId();

			// First iteration or new staffId detected
			if (i == 0 || !currentCode.equals(staffId)) {
				// Save the previous personnel data before switching to a new one
				if (i > 0) {
					personnelAndDatesConditions.add("(p.staffId = :" + personnelParam + " AND p.attendanceDate IN (:" + dateParam + "))");
					personnelAndDatesParameters.put(personnelParam, staffId);
					personnelAndDatesParameters.put(dateParam, new ArrayList<>(attendanceDates));
				}

				// Initialize new personnel tracking
				staffId = currentCode;
				personnelParam = "staffId" + index;
				dateParam = "attendanceDates" + index;
				attendanceDates = new ArrayList<>();
				index++;
			}

			// Add unique attendance date
			if (!attendanceDates.contains(attendanceRequest.getAttendanceDate())) {
				attendanceDates.add(attendanceRequest
						.getAttendanceDate());
			}
		}

		//Save the last personnel's data
		if (staffId != null) {
			personnelAndDatesConditions.add("(p.staffId = :" + personnelParam + " AND p.attendanceDate IN (:" + dateParam + "))");
			personnelAndDatesParameters.put(personnelParam, staffId);
			personnelAndDatesParameters.put(dateParam, attendanceDates);
		}

		jpqlBuilderForPersonnelAttendance.append(String.join(" OR ", personnelAndDatesConditions)).append(")");

		TypedQuery<PersonnelAttendance> queryForPersonnelAttendance = entityManager.createQuery(jpqlBuilderForPersonnelAttendance.toString(), PersonnelAttendance.class);
		queryForPersonnelAttendance.setParameter("tenantId", tenantId);
		queryForPersonnelAttendance.setParameter("storeId", storeId);

		// Set dynamically created parameters
		for (Map.Entry<String, Object> param : personnelAndDatesParameters.entrySet()) {
			queryForPersonnelAttendance.setParameter(param.getKey(), param.getValue());
		}

		List<PersonnelAttendance> personnelAttendanceListFromDb = queryForPersonnelAttendance.getResultList();

		List<IndividualPunches> individualPunchesList = new ArrayList<>();

		if (!personnelAttendanceListFromDb.isEmpty()) {
			for (PersonnelAttendance personnelAttendance: personnelAttendanceListFromDb) {
				IndividualPunches individualPunch = this.createIndividualPunch(personnelAttendance, zoneId);
				individualPunchesList.add(individualPunch);
			}
		}

		return individualPunchesList;
	}

	public IndividualPunches createIndividualPunch(PersonnelAttendance personnelAttendance,
												   ZoneId zoneId) {
		IndividualPunches individualPunch = new IndividualPunches();
		if (!ObjectUtils.isEmpty(personnelAttendance)) {
			individualPunch.setPersonnelAttendanceId(personnelAttendance.getId());
			individualPunch.setPunchDate(personnelAttendance.getAttendanceDate());
			individualPunch.setPunchTime(personnelAttendance.getPunchTimestamp().atZone(zoneId).toLocalTime());
			individualPunch.setCreatedBy(personnelAttendance.getCreatedBy());
			individualPunch.setCreatedTimestamp(personnelAttendance.getCreatedTimestamp());
			individualPunch.setModifiedBy(personnelAttendance.getModifiedBy());
			individualPunch.setModifiedTimestamp(personnelAttendance.getModifiedTimestamp());
			individualPunch.setPunchEvent(personnelAttendance.getPunchEvent());
			individualPunch.setCurrentStatus(personnelAttendance.getCurrentStatus());
			individualPunch.setUploadSource(personnelAttendance.getUploadSource());
			individualPunch.setAttendanceDate(personnelAttendance.getAttendanceDate());
			individualPunch.setStaffId(personnelAttendance.getStaffId());
		}
		return individualPunch;
	}

	public ResponseModel flagRegularizationRequests(List<AttendanceRequestsDTO> inputAttendanceRequestsDTOList){
		ResponseModel responseModel = new ResponseModel();
		try {
						if (!inputAttendanceRequestsDTOList.isEmpty()) {
				List<PersonnelAttendance> approvedOrRejectedPersonnelAttendanceList = new ArrayList<>();

				List<Long> personnelAttendanceIdList = inputAttendanceRequestsDTOList.stream()
						.map(AttendanceRequestsDTO::getPersonnelAttendanceId).collect(Collectors.toList());

				List<PersonnelAttendance> personnelAttendanceList =
						personnelAttendanceRepository.findByIdIn(personnelAttendanceIdList);

				if (!personnelAttendanceList.isEmpty()) {
					for (PersonnelAttendance personnelAttendance: personnelAttendanceList) {
						Optional<AttendanceRequestsDTO> attendanceRequestsDTOOptional =
								inputAttendanceRequestsDTOList.stream().filter(
										dto -> dto.getPersonnelAttendanceId()
												.equals(personnelAttendance.getId())).findFirst();
						if (attendanceRequestsDTOOptional.isPresent()) {
							personnelAttendance.setModifiedBy(attendanceRequestsDTOOptional.get().getModifiedBy());
							personnelAttendance.setModifiedTimestamp(Instant.now());
							personnelAttendance.setCurrentStatus(attendanceRequestsDTOOptional.get().getCurrentStatus());
							personnelAttendance.setRemark(attendanceRequestsDTOOptional.get().getRemark());
							try {
								personnelAttendanceRepository.save(personnelAttendance);
								if (StringUtils.equalsIgnoreCase(personnelAttendance.getCurrentStatus(),RegularizationRequestStatuses.APPROVED.name())
										|| StringUtils.equalsIgnoreCase(personnelAttendance.getCurrentStatus(),RegularizationRequestStatuses.REJECTED.name())) {
									approvedOrRejectedPersonnelAttendanceList.add(personnelAttendance);
								}
							} catch (ObjectOptimisticLockingFailureException ex) {
								logger.error("OptimisticLockException has occurred for PersonnelAttendance ID: {}, Exception: {}", personnelAttendance.getId(), ex.getMessage());
							}
						}
					}

					if (!approvedOrRejectedPersonnelAttendanceList.isEmpty()) {
						asyncAttendanceSummaryCalculation.syncCalculateAttendanceSummaryForApprovalOrRejection(approvedOrRejectedPersonnelAttendanceList, null);
					}
				}
				responseModel.setData(inputAttendanceRequestsDTOList);
				responseModel.setMessage("SUCCESS");
				responseModel.setCode(HttpStatus.OK);
			} else {
				responseModel.setCode(HttpStatus.BAD_REQUEST);
				responseModel.setMessage("The input list is empty.");
			}
		} catch (Exception ex) {
			logger.error("Exception inside flagRegularizationRequests: {}", ex.getMessage());
			throw ex;
		}
		return responseModel;
	}

	public ResponseModel getTenantStoreConfiguration(Long tenantId, Long storeId) {
		ResponseModel responseModel = new ResponseModel();
		try {
			Optional<StoreDetails> storeDetailsOptional =
					storeDetailsRepository.fetchStoreAndTenantDetails(
							BiometricApplicationNames.RESPARK.name(), tenantId, storeId);
			if (storeDetailsOptional.isPresent()) {
				TenantStoreDTO tenantStoreDTO = new TenantStoreDTO();
				StoreDetails storeDetails = storeDetailsOptional.get();
				tenantStoreDTO.setTenantId(storeDetails.getTenantCompanyMapping().getTenantId());
				tenantStoreDTO.setStoreId(storeDetails.getStoreId());
				tenantStoreDTO.setApplicationName(storeDetails.getTenantCompanyMapping().getApplicationName());
				tenantStoreDTO.setSalaryCycleStartDay(storeDetails.getTenantCompanyMapping().getSalaryCycleStartDay());
				tenantStoreDTO.setSummaryCalculationOffsetDays(storeDetails.getTenantCompanyMapping().getSummaryCalculationOffsetDays());
				tenantStoreDTO.setSalaryCalculationOffsetDays(storeDetails.getTenantCompanyMapping().getSalaryCalculationOffsetDays());
				tenantStoreDTO.setBiometricVendorName(storeDetails.getTenantCompanyMapping().getBiometricVendorName());
				tenantStoreDTO.setTimeZone(storeDetails.getTimeZone());
				tenantStoreDTO.setIsActualTimeBasedAttendance(storeDetails.getIsActualTimeBasedAttendance());
				tenantStoreDTO.setTimestampOfLastAttendanceRetrieval(storeDetails.getTimestampOfLastAttendanceRetrieval());
				tenantStoreDTO.setRecentSummaryCalculatedMonth(storeDetails.getRecentSummaryCalculatedMonth());
				tenantStoreDTO.setRetrieveAttendanceWithOtherStores(storeDetails.getRetrieveAttendanceWithOtherStores());
				tenantStoreDTO.setStoreOpenTime(storeDetails.getStoreOpenTime());
				tenantStoreDTO.setStoreCloseTime(storeDetails.getStoreCloseTime());
				tenantStoreDTO.setFinancialYearStartMonth(storeDetails.getFinancialYearStartMonth());
				tenantStoreDTO.setPayrollLockedUpToDate(storeDetails.getPayrollLockedUpToDate());
				tenantStoreDTO.setPenaltyAbsentDays(storeDetails.getTenantCompanyMapping().getPenaltyAbsentDays());
				tenantStoreDTO.setIsPaidLeaveApplicable(storeDetails.getTenantCompanyMapping().getIsPaidLeaveApplicable());
				tenantStoreDTO.setStoreName(storeDetails.getStoreName());
				tenantStoreDTO.setCurrencySymbol(storeDetails.getCurrencySymbol());

				Optional<com.relfor.pcs.payroll.entity.StoreProfileConfig> profileConfigOptional = storeProfileConfigRepository.findByTenantIdAndStoreId(tenantId, storeId);
				if (profileConfigOptional.isPresent()) {
					tenantStoreDTO.setCompanyName(profileConfigOptional.get().getCompanyName());
					tenantStoreDTO.setAddress(profileConfigOptional.get().getAddress());
				}

				responseModel.setData(tenantStoreDTO);
			}
			responseModel.setMessage("SUCCESS");
			responseModel.setCode(HttpStatus.OK);
		} catch (Exception ex) {
			logger.error("Exception inside getTenantStoreConfiguration: {}", ex.getMessage());
			throw ex;
		}
		return responseModel;
	}
}