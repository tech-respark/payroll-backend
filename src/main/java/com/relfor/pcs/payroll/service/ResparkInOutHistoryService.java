package com.relfor.pcs.payroll.service;

import com.relfor.pcs.payroll.dto.ResponseModel;
import com.relfor.pcs.payroll.dto.DayWiseAttendance;
import com.relfor.pcs.payroll.dto.IndividualPunches;
import com.relfor.pcs.payroll.dto.PersonnelAttendanceModel;
import com.relfor.pcs.payroll.dto.constants.BiometricApplicationNames;
import com.relfor.pcs.payroll.dto.constants.BiometricEntryUploadSource;
import com.relfor.pcs.payroll.dto.constants.RegularizationRequestStatuses;
import com.relfor.pcs.payroll.entity.PersonnelDetails;
import com.relfor.pcs.payroll.model.InOutHistoryInputModel;
import com.relfor.pcs.payroll.projection.PersonnelAttendanceProjectionForInOutHistory;
import com.relfor.pcs.payroll.projection.TenantStoreProjection;
import com.relfor.pcs.payroll.repository.PersonnelAttendanceRepository;
//import com.relfor.pcs.payroll.repository.PersonnelAttendanceRequestsRepository;
import com.relfor.pcs.payroll.repository.PersonnelDetailsRepository;
import com.relfor.pcs.payroll.repository.TenantCompanyMappingRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ResparkInOutHistoryService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String SUCCESS = "SUCCESS";
	@Autowired
	PersonnelAttendanceRepository personnelAttendanceRepository;
	@Autowired
	TenantCompanyMappingRepository tenantCompanyMappingRepository;
	@Autowired
	PersonnelDetailsRepository personnelDetailsRepository;

	public ResponseModel getInOutHistoryInformation(InOutHistoryInputModel inOutHistoryInputModel) {
		ResponseModel responseModel = new ResponseModel();
		try {
			List<PersonnelAttendanceModel> personnelAttendanceModelList = new ArrayList<>();
			List<PersonnelAttendanceProjectionForInOutHistory> personnelAttendanceData =
					personnelAttendanceRepository.getInOutHistoryBetweenDates(inOutHistoryInputModel.getFromDate(),
							inOutHistoryInputModel.getToDate(), BiometricApplicationNames.RESPARK.name(), inOutHistoryInputModel.getTenantId(),
							inOutHistoryInputModel.getStoreId(), inOutHistoryInputModel.getPersonnelCodes());
			Set<Long> personnelCodesFromInput = new HashSet<>(inOutHistoryInputModel.getPersonnelCodes());
			Set<Long> personnelCodesWithAttendance = new HashSet<>();
			if (!personnelAttendanceData.isEmpty()) {
				Optional<TenantStoreProjection> tenantStoreProjectionOptional =
						tenantCompanyMappingRepository.getTenantStoreMapping(inOutHistoryInputModel.getTenantId(),
								inOutHistoryInputModel.getStoreId(), BiometricApplicationNames.RESPARK.name());
				ZoneId zoneId = ZoneId.systemDefault();
				if (tenantStoreProjectionOptional.isPresent()) {
					zoneId = ZoneId.of(tenantStoreProjectionOptional.get().getTimeZone());
				}

				Map<Long, List<PersonnelAttendanceProjectionForInOutHistory>> personnelWiseAttendance = personnelAttendanceData.stream()
						.collect(Collectors.groupingBy(PersonnelAttendanceProjectionForInOutHistory::getPersonnelCode));
				if (!personnelWiseAttendance.isEmpty()) {
					for (Map.Entry<Long, List<PersonnelAttendanceProjectionForInOutHistory>> entry : personnelWiseAttendance.entrySet()) {
						PersonnelAttendanceModel personnelAttendanceModel = this.processPersonnelAttendance(
								entry.getKey(), entry.getValue(),
								inOutHistoryInputModel.getTenantId(),
								inOutHistoryInputModel.getStoreId(),
								inOutHistoryInputModel.getApplicationName(),
								inOutHistoryInputModel.getFromDate(),
								inOutHistoryInputModel.getToDate(),
								zoneId
						);
						personnelAttendanceModelList.add(personnelAttendanceModel);
						personnelCodesWithAttendance.add(entry.getKey());
					}
				}
			}

			personnelCodesFromInput.removeAll(personnelCodesWithAttendance);
			if (!personnelCodesFromInput.isEmpty()) {
				List<PersonnelDetails> personnelListWithoutAttendance = personnelDetailsRepository.getPersonnelByPersonnelCode(new ArrayList<>(personnelCodesFromInput));
				if (!personnelListWithoutAttendance.isEmpty()) {
					for (PersonnelDetails personnel: personnelListWithoutAttendance) {
						PersonnelAttendanceModel personnelAttendanceModel =
								this.createPersonnelAttendanceModel(personnel.getPersonnelCode(),
										inOutHistoryInputModel.getTenantId(), inOutHistoryInputModel.getStoreId(),
										inOutHistoryInputModel.getApplicationName(), inOutHistoryInputModel.getFromDate(),
										inOutHistoryInputModel.getToDate());
						this.populatePersonnelDetails(personnelAttendanceModel,
								personnel.getGender(), (Optional.ofNullable(personnel.getFirstName()).orElse("")
										+ " " + Optional.ofNullable(personnel.getLastName()).orElse("")).trim(),
								personnel.getDesignation(), personnel.getPersonnelMobileNumber());
						personnelAttendanceModelList.add(personnelAttendanceModel);
					}
				}
			}

			responseModel.setCode(HttpStatus.OK);
			responseModel.setData(personnelAttendanceModelList);
			responseModel.setMessage(SUCCESS);
		} catch (Exception ex) {
			logger.error("Exception inside getInOutHistoryInformation: {}", ex.getMessage());
			throw ex;
		}
		return responseModel;
	}

	public ResponseModel getPersonnelAttendanceForADay(Long tenantId, Long storeId, Long personnelCode, LocalDate attendanceDate) {
		ResponseModel responseModel = new ResponseModel();
		try {
			PersonnelAttendanceModel personnelAttendanceModel = null;
			List<PersonnelAttendanceProjectionForInOutHistory> personnelAttendanceData =
					personnelAttendanceRepository.getInOutHistoryBetweenDates(attendanceDate, attendanceDate,
							BiometricApplicationNames.RESPARK.name(), tenantId, storeId, List.of(personnelCode));
			if (!personnelAttendanceData.isEmpty()) {
				Optional<TenantStoreProjection> tenantStoreProjectionOptional =
						tenantCompanyMappingRepository.getTenantStoreMapping(tenantId, storeId,
								BiometricApplicationNames.RESPARK.name());
				ZoneId zoneId = ZoneId.systemDefault();
				if (tenantStoreProjectionOptional.isPresent()) {
					zoneId = ZoneId.of(tenantStoreProjectionOptional.get().getTimeZone());
				}

				personnelAttendanceModel = this.processPersonnelAttendance(
						personnelCode, personnelAttendanceData,
						tenantId, storeId,
						BiometricApplicationNames.RESPARK.name(),
						attendanceDate,
						attendanceDate,
						zoneId
				);
			}
			responseModel.setCode(HttpStatus.OK);
			responseModel.setData(personnelAttendanceModel);
			responseModel.setMessage(SUCCESS);
		} catch (Exception ex) {
			logger.error("Exception inside getPersonnelAttendanceForADay: {}", ex.getMessage());
			throw ex;
		}
		return responseModel;
	}

	private PersonnelAttendanceModel processPersonnelAttendance(
			Long personnelCode,
			List<PersonnelAttendanceProjectionForInOutHistory> individualPersonnelAttendanceList,
			Long tenantId, Long storeId, String applicationName,
			LocalDate fromDate, LocalDate toDate, ZoneId zoneId) {
		PersonnelAttendanceModel personnelModel = this.createPersonnelAttendanceModel(
				personnelCode, tenantId, storeId, applicationName, fromDate, toDate);
		if (!individualPersonnelAttendanceList.isEmpty()) {
			this.populatePersonnelDetails(personnelModel,
					individualPersonnelAttendanceList.get(0).getPersonnelGender(),
					individualPersonnelAttendanceList.get(0).getPersonnelName(),
					individualPersonnelAttendanceList.get(0).getPersonnelDesignation(),
					individualPersonnelAttendanceList.get(0).getPersonnelMobileNumber());

			Map<LocalDate, List<PersonnelAttendanceProjectionForInOutHistory>> dateWisePersonnelAttendance = individualPersonnelAttendanceList.stream()
					.collect(Collectors.groupingBy(PersonnelAttendanceProjectionForInOutHistory::getAttendanceDate));
			List<DayWiseAttendance> dayWiseAttendanceList = new ArrayList<>();
			if (!dateWisePersonnelAttendance.isEmpty()) {
				for (Map.Entry<LocalDate, List<PersonnelAttendanceProjectionForInOutHistory>> entry : dateWisePersonnelAttendance.entrySet()) {
					DayWiseAttendance dayWiseAttendance = this.processDateWiseAttendance(
							entry.getKey(), entry.getValue(), zoneId
					);
					dayWiseAttendanceList.add(dayWiseAttendance);
				}
				personnelModel.setDayWiseAttendanceList(dayWiseAttendanceList);
			}
		}
		return personnelModel;
	}

	private PersonnelAttendanceModel createPersonnelAttendanceModel(Long personnelCode, Long tenantId,
																	Long storeId, String applicationName,
																	LocalDate fromDate, LocalDate toDate) {
		PersonnelAttendanceModel model = new PersonnelAttendanceModel();
		model.setTenantId(tenantId);
		model.setStoreId(storeId);
		model.setApplicationName(applicationName);
		model.setFromDate(fromDate);
		model.setToDate(toDate);
		model.setPersonnelCode(personnelCode);
		return model;
	}

	private void populatePersonnelDetails(PersonnelAttendanceModel model,
										  String personnelGender,
										  String personnelName,
										  String personnelDesignation,
										  String personnelMobileNumber) {
		model.setPersonnelGender(personnelGender);
		model.setPersonnelName(personnelName);
		model.setPersonnelDesignation(personnelDesignation);
		model.setPersonnelMobileNumber(personnelMobileNumber);
	}

	private DayWiseAttendance processDateWiseAttendance(
			LocalDate dateOfAttendance,
			List<PersonnelAttendanceProjectionForInOutHistory> individualDateWiseAttendance,
			ZoneId zoneId) {
		DayWiseAttendance dayAttendance = new DayWiseAttendance();
		dayAttendance.setDateOfAttendance(dateOfAttendance);
		if (!individualDateWiseAttendance.isEmpty()) {
			dayAttendance.setDayOfAttendance(individualDateWiseAttendance.get(0).getAttendanceDayOfWeek());
			List<IndividualPunches> punchesList = new ArrayList<>();
			punchesList = this.processIndividualPunches(individualDateWiseAttendance,
					punchesList, zoneId);
			dayAttendance.setIndividualPunchesList(punchesList);
		}
		return dayAttendance;
	}

	private List<IndividualPunches> processIndividualPunches(
			List<PersonnelAttendanceProjectionForInOutHistory> attendanceList,
			List<IndividualPunches> punchesList,
			ZoneId zoneId) {
		attendanceList.sort(Comparator.comparing(PersonnelAttendanceProjectionForInOutHistory::getPunchTimestamp)
				.thenComparing(PersonnelAttendanceProjectionForInOutHistory::getCurrentStatus, Comparator.nullsFirst(Comparator.naturalOrder())));

		for (int i = 0; i < attendanceList.size(); i++) {
			PersonnelAttendanceProjectionForInOutHistory record = attendanceList.get(i);
			IndividualPunches punch = new IndividualPunches();
			punch.setPunchDate(record.getAttendanceDate());
			punch.setPunchTime(record.getPunchTimestamp().atZone(zoneId).toLocalTime());
			punch.setCreatedBy(record.getCreatedBy());
			punch.setCreatedTimestamp(record.getCreatedTimestamp());
			punch.setModifiedBy(record.getModifiedBy());
			punch.setModifiedTimestamp(record.getModifiedTimestamp());
			punch.setPunchEvent(record.getPunchEvent());
			punch.setPersonnelAttendanceId(record.getPersonnelAttendanceId());
			punch.setCurrentStatus(record.getCurrentStatus());
			punch.setUploadSource(record.getUploadSource());
			punchesList.add(punch);
		}

		return punchesList;
	}
}