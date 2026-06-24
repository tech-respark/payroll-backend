package com.relfor.pcs.payroll.service;

import com.relfor.pcs.payroll.dto.*;
import com.relfor.pcs.payroll.entity.*;
import com.relfor.pcs.payroll.projection.MonthlySummaryCalculationProjection;
import com.relfor.pcs.payroll.projection.PersonnelAttendanceSummaryProjection;
import com.relfor.pcs.payroll.projection.TenantStoreProjection;
import com.relfor.pcs.payroll.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttendanceManagementService {
	@Autowired
	PersonnelDetailsRepository personnelDetailsRepository;
	@Autowired
	DayWiseAttendanceSummaryRepository dayWiseAttendanceSummaryRepository;
	@Autowired
	TenantCompanyMappingRepository tenantCompanyMappingRepository;
	@PersistenceContext
	private EntityManager entityManager;
	@Autowired
	AttendanceRetrievalRoutingService attendanceRetrievalRoutingService;
	@Autowired
	MonthWiseAttendanceSummaryRepository monthWiseAttendanceSummaryRepository;
	@Autowired
	SalaryCalculation salaryCalculation;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String SUCCESS = "SUCCESS";

	public ResponseModel enrollPersonnelForAttendanceManagement(StaffDTO staffDTO) {
		ResponseModel responseModel = new ResponseModel();
		try {
			PersonnelDetails personnelDetails;
			
			if (staffDTO.getId() != null && staffDTO.getId() != 0) {
				Optional<PersonnelDetails> personnelDetailsOptional = personnelDetailsRepository
						.findByIdAndApplicationName(staffDTO.getId(), staffDTO.getApplicationName());
				personnelDetails = personnelDetailsOptional.orElseGet(PersonnelDetails::new);
			} else {
				personnelDetails = new PersonnelDetails();
			}

			this.convertToPersonnelDetails(staffDTO, personnelDetails);
			personnelDetails.setActive(safeGetActive(staffDTO) == 1);
			personnelDetails = personnelDetailsRepository.save(personnelDetails);

			this.convertToStaffDTO(personnelDetails, staffDTO);
			responseModel.setData(staffDTO);
			responseModel.setCode(HttpStatus.OK);
			responseModel.setMessage(SUCCESS);
		} catch (Exception ex) {
			logger.error("Exception in enrollPersonnelForAttendanceManagement: {}", ex.getMessage());
			throw ex;
		}
		return responseModel;
	}

	private float safeGetExperience(StaffDTO dto) {
		try {
			return dto.getExperience();
		} catch (NullPointerException e) {
			return 0.0f;
		}
	}

	private int safeGetActive(StaffDTO dto) {
		try {
			return dto.getActive();
		} catch (NullPointerException e) {
			return 1;
		}
	}

	private int safeGetDisplayRank(StaffDTO dto) {
		try {
			return dto.getDisplayRank();
		} catch (NullPointerException e) {
			return 0;
		}
	}

	private PersonnelDetails convertToPersonnelDetails(StaffDTO staffDTO, PersonnelDetails personnelDetails) {
		personnelDetails.setId(staffDTO.getId());
		personnelDetails.setFirstName(staffDTO.getFirstName());
		personnelDetails.setLastName(staffDTO.getLastName());
		personnelDetails.setGender(staffDTO.getGender());
		personnelDetails.setDesignation(staffDTO.getDesignation());
		personnelDetails.setPersonnelMobileNumber(staffDTO.getMobile());
		personnelDetails.setApplicationName(staffDTO.getApplicationName());
		personnelDetails.setApplicationTenantId(staffDTO.getTenantId());
		personnelDetails.setUsername(staffDTO.getUsername());
		personnelDetails.setPassword(staffDTO.getPwd());
		personnelDetails.setEmail(staffDTO.getEmail());
		personnelDetails.setAddress(staffDTO.getAddress());
		personnelDetails.setSpeciality(staffDTO.getSpeciality());
		personnelDetails.setWeeklyOff(staffDTO.getWeeklyOff() != null ? String.join(",", staffDTO.getWeeklyOff()) : null);
		personnelDetails.setOwner(staffDTO.isOwner());
		personnelDetails.setAreaManager(staffDTO.isAreaManager());
		personnelDetails.setDoctor(staffDTO.isDoctor());
		personnelDetails.setConsultant(staffDTO.isConsultant());
		personnelDetails.setBirthDate(staffDTO.getBirthDate());
		personnelDetails.setJoiningDate(staffDTO.getJoiningDate());
		personnelDetails.setReceiveOnlineNotifications(staffDTO.getReceiveOnlineNotifications());
		personnelDetails.setReceiveNotificationsOfAll(staffDTO.getReceiveNotificationsOfAll());
		personnelDetails.setSalaryAmount(staffDTO.getSalaryAmount());
		personnelDetails.setSpeciality(staffDTO.getSpeciality());
		personnelDetails.setPhone(staffDTO.getPhone());
		personnelDetails.setPasscode(staffDTO.getPasscode());
		personnelDetails.setExperience(safeGetExperience(staffDTO));
		personnelDetails.setDescription(staffDTO.getDescription());
		personnelDetails.setProfileImage(staffDTO.getProfileImage());
		personnelDetails.setOtp(staffDTO.getOtp());
		personnelDetails.setDisplayRank(safeGetDisplayRank(staffDTO));

		if (!ObjectUtils.isEmpty(staffDTO.getPersonnelJoiningDetails())) {
			personnelDetails.setEmployeeCode(staffDTO.getPersonnelJoiningDetails().getEmployeeCode());
			personnelDetails.setReportingTo(staffDTO.getPersonnelJoiningDetails().getReportingTo());
			personnelDetails.setStoreId(staffDTO.getPersonnelJoiningDetails().getStoreId());
			personnelDetails.setUanNumber(staffDTO.getPersonnelJoiningDetails().getUanNumber());
			personnelDetails.setWorkingHours(staffDTO.getPersonnelJoiningDetails().getWorkingHours());
		}

		if (!ObjectUtils.isEmpty(staffDTO.getPersonnelBankAccountDetails())) {
			PersonnelBankAccountDetails personnelBankAccountDetails;
			if (!ObjectUtils.isEmpty(personnelDetails.getPersonnelBankAccountDetails())) {
				personnelBankAccountDetails = personnelDetails.getPersonnelBankAccountDetails();
			} else {
				personnelBankAccountDetails = new PersonnelBankAccountDetails();
			}
			personnelBankAccountDetails = this.convertToBankAccountDetailsEntity(staffDTO.getPersonnelBankAccountDetails(), staffDTO.getId(), personnelBankAccountDetails);
			personnelBankAccountDetails.setPersonnelDetails(personnelDetails);
			personnelDetails.setPersonnelBankAccountDetails(personnelBankAccountDetails);
		} else {
			personnelDetails.setPersonnelBankAccountDetails(null);
		}

		if (!ObjectUtils.isEmpty(staffDTO.getPersonnelWorkExperienceDetailsList())) {
			List<PersonnelWorkExperienceDetails> workExperiences = staffDTO.getPersonnelWorkExperienceDetailsList().stream()
					.map(dto -> {
						return this.convertToWorkExperienceDetailsEntity(dto, staffDTO.getId());
					}).collect(Collectors.toList());
			personnelDetails.setPersonnelWorkExperienceDetails(workExperiences);
		} else {
			personnelDetails.setPersonnelWorkExperienceDetails(null);
		}

		if (!ObjectUtils.isEmpty(staffDTO.getPersonnelDocumentDetailsList())) {
			List<PersonnelDocumentDetails> documentDetails = staffDTO.getPersonnelDocumentDetailsList().stream()
					.map(dto -> {
						return this.convertToDocumentDetailsEntity(dto, staffDTO.getId());
					}).collect(Collectors.toList());
			personnelDetails.setPersonnelDocumentDetails(documentDetails);
		} else {
			personnelDetails.setPersonnelDocumentDetails(null);
		}

		if (!ObjectUtils.isEmpty(staffDTO.getPersonnelEmergencyContactsList())) {
			List<PersonnelEmergencyContactDetails> contacts = staffDTO.getPersonnelEmergencyContactsList().stream()
					.map(dto -> {
						return this.convertToEmergencyContactsEntity(dto, staffDTO.getId());
					}).collect(Collectors.toList());
			personnelDetails.setPersonnelEmergencyContactDetails(contacts);
		} else {
			personnelDetails.setPersonnelEmergencyContactDetails(null);
		}
		return personnelDetails;
	}

	private void convertToStaffDTO(PersonnelDetails personnelDetails, StaffDTO staffDTO) {
		staffDTO.setId(personnelDetails.getId());
		staffDTO.setFirstName(personnelDetails.getFirstName());
		staffDTO.setLastName(personnelDetails.getLastName());
		staffDTO.setGender(personnelDetails.getGender());
		staffDTO.setDesignation(personnelDetails.getDesignation());
		staffDTO.setMobile(personnelDetails.getPersonnelMobileNumber());
		staffDTO.setApplicationName(personnelDetails.getApplicationName());
		staffDTO.setTenantId(personnelDetails.getApplicationTenantId());
		staffDTO.setUsername(personnelDetails.getUsername());
		staffDTO.setPwd(personnelDetails.getPassword());
		staffDTO.setEmail(personnelDetails.getEmail());
		staffDTO.setAddress(personnelDetails.getAddress());
		staffDTO.setSpeciality(personnelDetails.getSpeciality());
		staffDTO.setEnableAppointments(personnelDetails.getEnableAppointments() != null && personnelDetails.getEnableAppointments() ? 1 : 0);
		staffDTO.setAllStaffAppointmentDashboard(personnelDetails.getAllStaffAppointmentDashboard());
		staffDTO.setWeeklyOff(personnelDetails.getWeeklyOff() != null ? personnelDetails.getWeeklyOff().split(",") : null);
		staffDTO.setOwner(personnelDetails.getOwner());
		staffDTO.setAreaManager(personnelDetails.getAreaManager());
		staffDTO.setDoctor(personnelDetails.getDoctor());
		staffDTO.setConsultant(personnelDetails.getConsultant());
		staffDTO.setBirthDate(personnelDetails.getBirthDate());
		staffDTO.setJoiningDate(personnelDetails.getJoiningDate());
		staffDTO.setReceiveOnlineNotifications(personnelDetails.getReceiveOnlineNotifications());
		staffDTO.setReceiveNotificationsOfAll(personnelDetails.getReceiveNotificationsOfAll());
		staffDTO.setSalaryAmount(personnelDetails.getSalaryAmount());
		staffDTO.setSpeciality(personnelDetails.getSpeciality());
		staffDTO.setPhone(personnelDetails.getPhone());
		staffDTO.setPasscode(personnelDetails.getPasscode());
		staffDTO.setExperience(personnelDetails.getExperience());
		staffDTO.setDescription(personnelDetails.getDescription());
		staffDTO.setProfileImage(personnelDetails.getProfileImage());
		staffDTO.setOtp(personnelDetails.getOtp());
		staffDTO.setDisplayRank(personnelDetails.getDisplayRank());

		if (!ObjectUtils.isEmpty(personnelDetails.getPersonnelBankAccountDetails())) {
			staffDTO.setPersonnelBankAccountDetails(this.convertToBankAccountDetailsDTO(personnelDetails.getPersonnelBankAccountDetails()));
		}

		if (!ObjectUtils.isEmpty(personnelDetails.getPersonnelWorkExperienceDetails())) {
			staffDTO.setPersonnelWorkExperienceDetailsList(personnelDetails.getPersonnelWorkExperienceDetails().stream()
					.map(this::convertToWorkExperienceDetailsDTO).collect(Collectors.toList()));
		}
		if (!ObjectUtils.isEmpty(personnelDetails.getPersonnelDocumentDetails())) {
			staffDTO.setPersonnelDocumentDetailsList(personnelDetails.getPersonnelDocumentDetails().stream()
					.map(this::convertToDocumentDetailsDTO).collect(Collectors.toList()));
		}

		if (!ObjectUtils.isEmpty(personnelDetails.getPersonnelEmergencyContactDetails())) {
			staffDTO.setPersonnelEmergencyContactsList(personnelDetails.getPersonnelEmergencyContactDetails().stream()
					.map(this::convertToEmergencyContactsDTO).collect(Collectors.toList()));
		}

		PersonnelJoiningDetailsDTO personnelJoiningDetailsDTO = new PersonnelJoiningDetailsDTO();
		personnelJoiningDetailsDTO.setUanNumber(personnelDetails.getUanNumber());
		personnelJoiningDetailsDTO.setEmployeeCode(personnelDetails.getEmployeeCode());
		personnelJoiningDetailsDTO.setReportingTo(personnelDetails.getReportingTo());
		personnelJoiningDetailsDTO.setWorkingHours(personnelDetails.getWorkingHours());
		personnelJoiningDetailsDTO.setStoreId(personnelDetails.getStoreId());
		staffDTO.setPersonnelJoiningDetails(personnelJoiningDetailsDTO);

		staffDTO.setActive(personnelDetails.getActive() != null && personnelDetails.getActive() ? 1 : 0);
		staffDTO.setIsSyncedWithPersonnelManagement(true);
		staffDTO.setIsSyncedWithBiometricDevice(personnelDetails.getTenantCompanyMappingId() != null);
	}

	private PersonnelBankAccountDetails convertToBankAccountDetailsEntity(PersonnelBankAccountDetailsDTO dto, Long staffId, PersonnelBankAccountDetails personnelBankAccountDetails) {
		personnelBankAccountDetails.setStaffId(staffId);
		personnelBankAccountDetails.setBankName(dto.getBankName());
		personnelBankAccountDetails.setBankBranch(dto.getBankBranch());
		personnelBankAccountDetails.setIfscCode(dto.getIfscCode());
		personnelBankAccountDetails.setAccountNumber(dto.getAccountNumber());

		return personnelBankAccountDetails;
	}

	private PersonnelWorkExperienceDetails convertToWorkExperienceDetailsEntity(PersonnelWorkExperienceDetailsDTO dto, Long staffId) {
		PersonnelWorkExperienceDetails personnelWorkExperienceDetails = new PersonnelWorkExperienceDetails();
		personnelWorkExperienceDetails.setId(dto.getId());
		personnelWorkExperienceDetails.setStaffId(staffId);
		personnelWorkExperienceDetails.setDesignation(dto.getDesignation());
		personnelWorkExperienceDetails.setCompanyName(dto.getCompanyName());
		personnelWorkExperienceDetails.setFromDate(dto.getFromDate());
		personnelWorkExperienceDetails.setToDate(dto.getToDate());

		return personnelWorkExperienceDetails;
	}

	private PersonnelEmergencyContactDetails convertToEmergencyContactsEntity(PersonnelEmergencyContactsDTO dto, Long staffId) {
		PersonnelEmergencyContactDetails personnelEmergencyContactDetails = new PersonnelEmergencyContactDetails();
		personnelEmergencyContactDetails.setId(dto.getId());
		personnelEmergencyContactDetails.setStaffId(staffId);
		personnelEmergencyContactDetails.setContactPersonName(dto.getContactPersonName());
		personnelEmergencyContactDetails.setContactPersonMobile(dto.getContactPersonMobile());
		personnelEmergencyContactDetails.setRelation(dto.getRelation());

		return personnelEmergencyContactDetails;
	}

	private PersonnelDocumentDetails convertToDocumentDetailsEntity(PersonnelDocumentDetailsDTO dto, Long staffId) {
		PersonnelDocumentDetails personnelDocumentDetails = new PersonnelDocumentDetails();
		personnelDocumentDetails.setId(dto.getId());
		personnelDocumentDetails.setStaffId(staffId);
		personnelDocumentDetails.setDocumentName(dto.getDocumentName());
		personnelDocumentDetails.setDocumentNumber(dto.getDocumentNumber());

		return personnelDocumentDetails;
	}

	private PersonnelBankAccountDetailsDTO convertToBankAccountDetailsDTO(PersonnelBankAccountDetails personnelBankAccountDetails) {
		PersonnelBankAccountDetailsDTO dto = new PersonnelBankAccountDetailsDTO();
		dto.setId(personnelBankAccountDetails.getId());
		dto.setStaffId(personnelBankAccountDetails.getStaffId());
		dto.setBankName(personnelBankAccountDetails.getBankName());
		dto.setBankBranch(personnelBankAccountDetails.getBankBranch());
		dto.setIfscCode(personnelBankAccountDetails.getIfscCode());
		dto.setAccountNumber(personnelBankAccountDetails.getAccountNumber());

		return dto;
	}

	private PersonnelWorkExperienceDetailsDTO convertToWorkExperienceDetailsDTO(PersonnelWorkExperienceDetails personnelWorkExperienceDetails) {
		PersonnelWorkExperienceDetailsDTO dto = new PersonnelWorkExperienceDetailsDTO();
		dto.setId(personnelWorkExperienceDetails.getId());
		dto.setStaffId(personnelWorkExperienceDetails.getStaffId());
		dto.setDesignation(personnelWorkExperienceDetails.getDesignation());
		dto.setCompanyName(personnelWorkExperienceDetails.getCompanyName());
		dto.setFromDate(personnelWorkExperienceDetails.getFromDate());
		dto.setToDate(personnelWorkExperienceDetails.getToDate());

		return dto;
	}

	private PersonnelEmergencyContactsDTO convertToEmergencyContactsDTO(PersonnelEmergencyContactDetails personnelEmergencyContactDetails) {
		PersonnelEmergencyContactsDTO dto = new PersonnelEmergencyContactsDTO();
		dto.setId(personnelEmergencyContactDetails.getId());
		dto.setStaffId(personnelEmergencyContactDetails.getStaffId());
		dto.setContactPersonName(personnelEmergencyContactDetails.getContactPersonName());
		dto.setContactPersonMobile(personnelEmergencyContactDetails.getContactPersonMobile());
		dto.setRelation(personnelEmergencyContactDetails.getRelation());

		return dto;
	}

	private PersonnelDocumentDetailsDTO convertToDocumentDetailsDTO(PersonnelDocumentDetails personnelDocumentDetails) {
		PersonnelDocumentDetailsDTO dto = new PersonnelDocumentDetailsDTO();
		dto.setId(personnelDocumentDetails.getId());
		dto.setStaffId(personnelDocumentDetails.getStaffId());
		dto.setDocumentName(personnelDocumentDetails.getDocumentName());
		dto.setDocumentNumber(personnelDocumentDetails.getDocumentNumber());

		return dto;
	}

	public ResponseModel getPersonnelAttendanceSummary(PersonnelAttendanceModel inputPersonnelAttendanceModel) {
		ResponseModel responseModel = new ResponseModel();
		try {
			List<PersonnelAttendanceModel> personnelAttendanceModelList = new ArrayList<>();
			boolean isActualTimeBasedAttendance = false;

			Optional<TenantStoreProjection> tenantStoreProjectionOptional =
					tenantCompanyMappingRepository.getTenantStoreMapping(inputPersonnelAttendanceModel.getTenantId(),
							inputPersonnelAttendanceModel.getStoreId(), inputPersonnelAttendanceModel.getApplicationName());

			if (tenantStoreProjectionOptional.isPresent()) {
				isActualTimeBasedAttendance = tenantStoreProjectionOptional.get().getIsActualTimeBasedAttendance();
			}

			List<PersonnelAttendanceSummaryProjection> personnelAttendanceSummaryProjectionList =
					dayWiseAttendanceSummaryRepository.getAttendanceSummaryBetweenDates(inputPersonnelAttendanceModel.getFromDate(),
							inputPersonnelAttendanceModel.getToDate(), inputPersonnelAttendanceModel.getTenantId(),
							inputPersonnelAttendanceModel.getStoreId(), inputPersonnelAttendanceModel.getApplicationName(),
							isActualTimeBasedAttendance);
			if (!personnelAttendanceSummaryProjectionList.isEmpty()) {
				Map<Long, List<PersonnelAttendanceSummaryProjection>> personnelWiseAttendance = personnelAttendanceSummaryProjectionList.stream()
						.collect(Collectors.groupingBy(PersonnelAttendanceSummaryProjection::getStaffId));
				for (Map.Entry<Long, List<PersonnelAttendanceSummaryProjection>> personnelWiseAttendanceEntry : personnelWiseAttendance.entrySet()) {
					PersonnelAttendanceModel outputPersonnelAttendanceModel =
							this.buildPersonnelAttendanceModel(inputPersonnelAttendanceModel, personnelWiseAttendanceEntry);
					personnelAttendanceModelList.add(outputPersonnelAttendanceModel);
				}
			}
			if (!personnelAttendanceModelList.isEmpty()) {
				responseModel.setData(personnelAttendanceModelList);
				responseModel.setMessage(SUCCESS);
				responseModel.setCode(HttpStatus.OK);
			} else {
				responseModel.setMessage("No Data found in attendance table for the store");
				responseModel.setCode(HttpStatus.NOT_FOUND);
			}
		} catch (Exception ex) {
			logger.error("Exception in getPersonnelAttendanceSummary: {}", ex.getMessage());
			throw ex;
		}
		return responseModel;
	}

	private PersonnelAttendanceModel buildPersonnelAttendanceModel(PersonnelAttendanceModel inputPersonnelAttendanceModel,
																   Map.Entry<Long, List<PersonnelAttendanceSummaryProjection>> personnelWiseAttendanceEntry) {
		PersonnelAttendanceModel model = new PersonnelAttendanceModel();
		model.setTenantId(inputPersonnelAttendanceModel.getTenantId());
		model.setStoreId(inputPersonnelAttendanceModel.getStoreId());
		model.setApplicationName(inputPersonnelAttendanceModel.getApplicationName());
		model.setFromDate(inputPersonnelAttendanceModel.getFromDate());
		model.setToDate(inputPersonnelAttendanceModel.getToDate());
		model.setStaffId(personnelWiseAttendanceEntry.getKey());

		List<PersonnelAttendanceSummaryProjection> individualPersonnelAttendanceList = personnelWiseAttendanceEntry.getValue();
		if (!individualPersonnelAttendanceList.isEmpty()) {
			PersonnelAttendanceSummaryProjection firstProjection = individualPersonnelAttendanceList.get(0);

			model.setPersonnelGender(firstProjection.getPersonnelGender());
			model.setPersonnelName(firstProjection.getPersonnelName());
			model.setPersonnelDesignation(firstProjection.getPersonnelDesignation());
			model.setPersonnelMobileNumber(firstProjection.getPersonnelMobileNumber());

			List<DayWiseAttendance> dayWiseAttendanceList = individualPersonnelAttendanceList.stream()
					.map(this::buildDayWiseAttendance)
					.collect(Collectors.toList());

			model.setDayWiseAttendanceList(dayWiseAttendanceList);
			model.setTotalHoursWorkedForPersonnel(this.calculateTotalHours(individualPersonnelAttendanceList));
			model.setTotalBreakTimeForPersonnel(this.calculateTotalBreakTime(individualPersonnelAttendanceList));
		}

		return model;
	}

	private DayWiseAttendance buildDayWiseAttendance(PersonnelAttendanceSummaryProjection individualPersonnelAttendance) {
		DayWiseAttendance attendance = new DayWiseAttendance();
		attendance.setDateOfAttendance(individualPersonnelAttendance.getAttendanceDate());
		attendance.setDayOfAttendance(individualPersonnelAttendance.getAttendanceDayOfWeek());
		attendance.setTotalHoursWorkedInADay(individualPersonnelAttendance.getTotalHoursWorkedInADay());
		attendance.setTotalBreakTimeInADay(individualPersonnelAttendance.getTotalBreakTimeInADay());
		return attendance;
	}

	private BigDecimal calculateTotalHours(List<PersonnelAttendanceSummaryProjection> projections) {
		return projections.stream()
				.map(PersonnelAttendanceSummaryProjection::getTotalHoursWorkedInADay)
				.filter(Objects::nonNull)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private BigDecimal calculateTotalBreakTime(List<PersonnelAttendanceSummaryProjection> projections) {
		return projections.stream()
				.map(PersonnelAttendanceSummaryProjection::getTotalBreakTimeInADay)
				.filter(Objects::nonNull)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public ResponseModel getPersonnelDetailsForListOfStaffIds(List<Long> staffIds) {
		ResponseModel responseModel = new ResponseModel();
		try {
			logger.info("Inside getPersonnelDetailsForListOfStaffIds for {} staffIds", staffIds.size());
			List<PersonnelDetails> personnelDetailsList = personnelDetailsRepository
					.getPersonnelById(staffIds);

			if (!personnelDetailsList.isEmpty()) {
				List<StaffDTO> staffDTOList = new ArrayList<>();
				for (PersonnelDetails personnelDetail : personnelDetailsList) {
					StaffDTO staffDTOOutput = new StaffDTO();
					this.convertToStaffDTO(personnelDetail, staffDTOOutput);
					staffDTOList.add(staffDTOOutput);
				}
				responseModel.setData(staffDTOList);
				responseModel.setMessage(SUCCESS);
				responseModel.setCode(HttpStatus.OK);
			} else {
				responseModel.setMessage("No Staff Data found for the given IDs");
				responseModel.setCode(HttpStatus.NOT_FOUND);
			}
		} catch (Exception ex) {
			logger.error("Exception inside getPersonnelDetailsForListOfStaffIds");
			throw ex;
		}
		return responseModel;
	}

	private PersonnelDetailsRequestModel getPersonnelDetailsRequestModel(PersonnelDetails personnelDetail) {
		PersonnelDetailsRequestModel personnelDetailsRequestModel = new PersonnelDetailsRequestModel();
		personnelDetailsRequestModel.setFirstName(personnelDetail.getFirstName());
		personnelDetailsRequestModel.setLastName(personnelDetail.getLastName());
		personnelDetailsRequestModel.setDesignation(personnelDetail.getDesignation());
		personnelDetailsRequestModel.setGender(personnelDetail.getGender());
		personnelDetailsRequestModel.setApplicationTenantId(personnelDetail.getApplicationTenantId());
		personnelDetailsRequestModel.setApplicationName(personnelDetail.getApplicationName());
		personnelDetailsRequestModel.setStaffId(personnelDetail.getId());
		personnelDetailsRequestModel.setPersonnelMobileNumber(personnelDetail.getPersonnelMobileNumber());
		personnelDetailsRequestModel.setActive(personnelDetail.getActive() != null ? personnelDetail.getActive() : false);
		return personnelDetailsRequestModel;
	}

	public void retrieveScheduledAttendanceData(List<PayrollSchedulerInfo> payrollSchedulerInfoList) {
		Map<String, Instant> invocationTimestampMap = payrollSchedulerInfoList.stream()
				.collect(Collectors.toMap(i -> i.getTenantId() + "-" + i.getStoreId(),
						PayrollSchedulerInfo::getInvocationTime));

		StringBuilder jpql = new StringBuilder(
				"SELECT s FROM StoreDetails s " +
						"JOIN FETCH s.tenantCompanyMapping t WHERE "
		);

		for (int i = 0; i < payrollSchedulerInfoList.size(); i++) {
			jpql.append("(t.tenantId = :tenant").append(i)
					.append(" AND s.storeId = :store").append(i).append(")");
			if (i < payrollSchedulerInfoList.size() - 1) {
				jpql.append(" OR ");
			}
		}

		TypedQuery<StoreDetails> query = entityManager.createQuery(jpql.toString(), StoreDetails.class);

		for (int i = 0; i < payrollSchedulerInfoList.size(); i++) {
			query.setParameter("tenant" + i, payrollSchedulerInfoList.get(i).getTenantId());
			query.setParameter("store" + i, payrollSchedulerInfoList.get(i).getStoreId());
		}

		List<StoreDetails> storeDetailsList = query.getResultList();
		Map<String, List<StoreDetails>> groupedByVendor = storeDetailsList.stream()
				.filter(store -> store.getTenantCompanyMapping() != null &&
						store.getTenantCompanyMapping().getBiometricVendorName() != null)
				.collect(Collectors.groupingBy(
						store -> store.getTenantCompanyMapping().getBiometricVendorName()));

		List<StoreDetails> storeDetailsListForMonthlySummary = new ArrayList<>();
		List<String> stringList = new ArrayList<>();
		for (Map.Entry<String, List<StoreDetails>> entry : groupedByVendor.entrySet()) {
			String outputString = attendanceRetrievalRoutingService.retrieveScheduledAttendanceDataFromVendor(entry.getKey(), entry.getValue(), invocationTimestampMap, storeDetailsListForMonthlySummary);
			stringList.add(outputString);
		}
		if (!storeDetailsListForMonthlySummary.isEmpty()) {
			logger.info("storeDetailsListForMonthlySummary size: {}", storeDetailsListForMonthlySummary.size());
			List<MonthWiseAttendanceSummary> monthWiseAttendanceSummaryList = new ArrayList<>();
			this.calculateAndSaveMonthlySummary(storeDetailsListForMonthlySummary,
					monthWiseAttendanceSummaryList);
			if (!monthWiseAttendanceSummaryList.isEmpty()) {
				salaryCalculation.processSalaryForMonthlySummary(monthWiseAttendanceSummaryList, storeDetailsListForMonthlySummary);
			}
		}
	}

	private void calculateAndSaveMonthlySummary(List<StoreDetails> storeDetailsListForMonthlySummary,
												List<MonthWiseAttendanceSummary> monthWiseAttendanceSummaryList) {
		storeDetailsListForMonthlySummary.sort(Comparator.comparing(store -> store.getTenantCompanyMapping().getTenantId()));
		Long tenantId = null;
		LocalDate fromDate = null;
		LocalDate toDate = null;

		//For future reference, this has to be changed and the store specific timezone has to be used.
		ZonedDateTime zonedDateTime = Instant.now().atZone(ZoneId.systemDefault());
		int monthValue = zonedDateTime.getMonthValue();
		int year = zonedDateTime.getYear();

		if (monthValue == 1) {
			monthValue = 12;
			year = year - 1;
		} else {
			monthValue = monthValue - 1;
		}

		Integer salaryCycleStartDay = null;
		for (StoreDetails storeDetails : storeDetailsListForMonthlySummary) {
			logger.info("Inside Monthly calculation for TenantId: {} & StoreId: {}", storeDetails.getTenantCompanyMapping().getTenantId(), storeDetails.getStoreId());
			if (tenantId == null
					|| !tenantId.equals(storeDetails.getTenantCompanyMapping().getTenantId())) {
				tenantId = storeDetails.getTenantCompanyMapping().getTenantId();
				salaryCycleStartDay = storeDetails.getTenantCompanyMapping().getSalaryCycleStartDay();
				fromDate = LocalDate.of(year, monthValue, salaryCycleStartDay);
				if (salaryCycleStartDay == 1) {
					toDate = fromDate.with(TemporalAdjusters.lastDayOfMonth());
				} else {
					toDate = LocalDate.of(monthValue == 1 ? year + 1 : year, monthValue + 1, salaryCycleStartDay - 1);
				}
			}

			if (fromDate != null) {
				List<MonthlySummaryCalculationProjection> monthlySummaryCalculationProjectionList =
						dayWiseAttendanceSummaryRepository.calculateMonthlySummary(tenantId, storeDetails.getStoreId(),
								storeDetails.getTenantCompanyMapping().getApplicationName(), fromDate, toDate);
				logger.debug("Total number of monthly summaries for TenantId: {} & StoreId: {} is {}",
						storeDetails.getTenantCompanyMapping().getTenantId(),
						storeDetails.getStoreId(),
						monthlySummaryCalculationProjectionList.size());
				for (MonthlySummaryCalculationProjection summary : monthlySummaryCalculationProjectionList) {
					MonthWiseAttendanceSummary monthWiseAttendanceSummary = new MonthWiseAttendanceSummary();
					this.constructMonthlySummaryAndAddToList(monthWiseAttendanceSummaryList,
							monthWiseAttendanceSummary, summary,
							storeDetails.getTenantCompanyMapping().getApplicationName(),
							tenantId, storeDetails.getStoreId(), fromDate, toDate);
				}
			}
		}
		monthWiseAttendanceSummaryRepository.saveAll(monthWiseAttendanceSummaryList);
	}

	public void constructMonthlySummaryAndAddToList(List<MonthWiseAttendanceSummary> monthWiseAttendanceSummaryList,
													MonthWiseAttendanceSummary monthWiseAttendanceSummary,
													MonthlySummaryCalculationProjection summary,
													String applicationName,
													Long tenantId,
													Long storeId,
													LocalDate fromDate,
													LocalDate toDate) {
		monthWiseAttendanceSummary.setApplicationName(applicationName);
		monthWiseAttendanceSummary.setTenantId(tenantId);
		monthWiseAttendanceSummary.setStoreId(storeId);
		monthWiseAttendanceSummary.setStaffId(summary.getStaffId());
		monthWiseAttendanceSummary.setSalaryCycleFromDate(fromDate);
		monthWiseAttendanceSummary.setSalaryCycleToDate(toDate);
		monthWiseAttendanceSummary.setSalaryMonth(fromDate.getMonth().name());
		monthWiseAttendanceSummary.setSalaryYear(fromDate.getYear());
		monthWiseAttendanceSummary.setTotalDaysInCycle((float) ChronoUnit.DAYS.between(fromDate, toDate) + 1);
		monthWiseAttendanceSummary.setTotalWorkingDays(summary.getTotalWorkingDays());
		monthWiseAttendanceSummary.setTotalWeeklyOffs(summary.getTotalWeeklyOffs());
		monthWiseAttendanceSummary.setTotalHolidays(summary.getTotalHolidays());
		monthWiseAttendanceSummary.setTotalPaidLeaves(summary.getTotalPaidLeaves());
		monthWiseAttendanceSummary.setTotalAbsentDays(summary.getTotalAbsentDays());
		monthWiseAttendanceSummary.setTotalPenaltyAbsentDays(summary.getTotalPenaltyAbsentDays());
		monthWiseAttendanceSummary.setExtraDaysWorked(summary.getExtraDaysWorked());
		monthWiseAttendanceSummary.setTotalPaidDays(summary.getTotalPaidDays());
		monthWiseAttendanceSummary.setTotalLateArrivalMins(summary.getTotalLateArrivalMins());
		monthWiseAttendanceSummary.setTotalEarlyExitMins(summary.getTotalEarlyExitMins());
		monthWiseAttendanceSummary.setTotalOvertimeMins(summary.getTotalOvertimeMins());
		monthWiseAttendanceSummary.setTotalLateArrivals(summary.getTotalLateArrivals());
		monthWiseAttendanceSummary.setTotalEarlyExits(summary.getTotalEarlyExits());
		monthWiseAttendanceSummaryList.add(monthWiseAttendanceSummary);
	}

	public void updatePasswordForPersonnel(StaffDTO staffDTO) {
		try {
			if (staffDTO.getId() != 0) {
				Optional<PersonnelDetails> personnelDetailsOptional = personnelDetailsRepository
						.findByIdAndApplicationName(staffDTO.getId(), staffDTO.getApplicationName());
				if (personnelDetailsOptional.isPresent()) {
					PersonnelDetails personnelDetails = personnelDetailsOptional.get();
					personnelDetails.setPassword(staffDTO.getPwd());
					personnelDetailsRepository.save(personnelDetails);
				}
			}
		} catch (Exception ex) {
			logger.error("Exception in updatePasswordForPersonnel: {}", ex.getMessage());
			throw ex;
		}
	}

	public List<StaffDTO> getAllPersonnelByTenantAndStore(Long tenantId, Long storeId) {
		List<PersonnelDetails> list = personnelDetailsRepository.findByApplicationTenantIdAndStoreId(tenantId, storeId);
		List<StaffDTO> staffDTOList = new ArrayList<>();
		for (PersonnelDetails pd : list) {
			StaffDTO dto = new StaffDTO();
			this.convertToStaffDTO(pd, dto);
			staffDTOList.add(dto);
		}
		return staffDTOList;
	}
}