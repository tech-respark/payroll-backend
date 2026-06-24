package com.relfor.pcs.payroll.service;

import com.relfor.pcs.payroll.dto.ResponseModel;
import com.relfor.pcs.payroll.dto.DayWiseAttendance;
import com.relfor.pcs.payroll.dto.IndividualPunches;
import com.relfor.pcs.payroll.dto.PersonnelAttendanceModel;
import com.relfor.pcs.payroll.dto.constants.BiometricApplicationNames;
import com.relfor.pcs.payroll.dto.constants.BiometricEntryUploadSource;
import com.relfor.pcs.payroll.dto.constants.RegularizationRequestStatuses;
import com.relfor.pcs.payroll.entity.PersonnelDetails;
import com.relfor.pcs.payroll.entity.SStaffShifts;
import com.relfor.pcs.payroll.model.InOutHistoryInputModel;
import com.relfor.pcs.payroll.projection.PersonnelAttendanceProjectionForInOutHistory;
import com.relfor.pcs.payroll.projection.TenantStoreProjection;
import com.relfor.pcs.payroll.repository.PersonnelAttendanceRepository;
//import com.relfor.pcs.payroll.repository.PersonnelAttendanceRequestsRepository;
import com.relfor.pcs.payroll.repository.PersonnelDetailsRepository;
import com.relfor.pcs.payroll.repository.TenantCompanyMappingRepository;
import com.relfor.pcs.payroll.repository.SStaffShiftsRepository;
import com.relfor.pcs.payroll.model.ResparkPersonnelAttendanceDTO;
import com.relfor.pcs.payroll.model.ResparkDayWiseAttendanceDTO;
import com.relfor.pcs.payroll.model.ResparkIndividualPunchesDTO;
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
	@Autowired
	SStaffShiftsRepository staffShiftsRepository;

	public ResponseModel getInOutHistoryInformation(InOutHistoryInputModel inOutHistoryInputModel) {
		ResponseModel responseModel = new ResponseModel();
		try {
			List<PersonnelAttendanceModel> personnelAttendanceModelList = new ArrayList<>();
			List<PersonnelAttendanceProjectionForInOutHistory> personnelAttendanceData =
					personnelAttendanceRepository.getInOutHistoryBetweenDates(inOutHistoryInputModel.getFromDate(),
							inOutHistoryInputModel.getToDate(), BiometricApplicationNames.RESPARK.name(), inOutHistoryInputModel.getTenantId(),
							inOutHistoryInputModel.getStoreId(), inOutHistoryInputModel.getPersonnelIds());
			Set<Long> personnelIdsFromInput = new HashSet<>(inOutHistoryInputModel.getPersonnelIds());
			Set<Long> personnelIdsWithAttendance = new HashSet<>();
			if (!personnelAttendanceData.isEmpty()) {
				Optional<TenantStoreProjection> tenantStoreProjectionOptional =
						tenantCompanyMappingRepository.getTenantStoreMapping(inOutHistoryInputModel.getTenantId(),
								inOutHistoryInputModel.getStoreId(), BiometricApplicationNames.RESPARK.name());
				ZoneId zoneId = ZoneId.systemDefault();
				if (tenantStoreProjectionOptional.isPresent()) {
					zoneId = ZoneId.of(tenantStoreProjectionOptional.get().getTimeZone());
				}

				Map<Long, List<PersonnelAttendanceProjectionForInOutHistory>> personnelWiseAttendance = personnelAttendanceData.stream()
						.collect(Collectors.groupingBy(PersonnelAttendanceProjectionForInOutHistory::getPersonnelId));
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
						personnelIdsWithAttendance.add(entry.getKey());
					}
				}
			}

			personnelIdsFromInput.removeAll(personnelIdsWithAttendance);
			if (!personnelIdsFromInput.isEmpty()) {
				List<PersonnelDetails> personnelListWithoutAttendance = personnelDetailsRepository.getPersonnelById(new ArrayList<>(personnelIdsFromInput));
				if (!personnelListWithoutAttendance.isEmpty()) {
					for (PersonnelDetails personnel: personnelListWithoutAttendance) {
						PersonnelAttendanceModel personnelAttendanceModel =
								this.createPersonnelAttendanceModel(personnel.getId(),
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

	public ResponseModel getPersonnelAttendanceForADay(Long tenantId, Long storeId, Long personnelId, LocalDate attendanceDate) {
		ResponseModel responseModel = new ResponseModel();
		try {
			PersonnelAttendanceModel personnelAttendanceModel = null;
			List<PersonnelAttendanceProjectionForInOutHistory> personnelAttendanceData =
					personnelAttendanceRepository.getInOutHistoryBetweenDates(attendanceDate, attendanceDate,
							BiometricApplicationNames.RESPARK.name(), tenantId, storeId, List.of(personnelId));
			if (!personnelAttendanceData.isEmpty()) {
				Optional<TenantStoreProjection> tenantStoreProjectionOptional =
						tenantCompanyMappingRepository.getTenantStoreMapping(tenantId, storeId,
								BiometricApplicationNames.RESPARK.name());
				ZoneId zoneId = ZoneId.systemDefault();
				if (tenantStoreProjectionOptional.isPresent()) {
					zoneId = ZoneId.of(tenantStoreProjectionOptional.get().getTimeZone());
				}

				personnelAttendanceModel = this.processPersonnelAttendance(
						personnelId, personnelAttendanceData,
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
			Long personnelId,
			List<PersonnelAttendanceProjectionForInOutHistory> individualPersonnelAttendanceList,
			Long tenantId, Long storeId, String applicationName,
			LocalDate fromDate, LocalDate toDate, ZoneId zoneId) {
		PersonnelAttendanceModel personnelModel = this.createPersonnelAttendanceModel(
				personnelId, tenantId, storeId, applicationName, fromDate, toDate);
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

	private PersonnelAttendanceModel createPersonnelAttendanceModel(Long personnelId, Long tenantId,
																	Long storeId, String applicationName,
																	LocalDate fromDate, LocalDate toDate) {
		PersonnelAttendanceModel model = new PersonnelAttendanceModel();
		model.setTenantId(tenantId);
		model.setStoreId(storeId);
		model.setApplicationName(applicationName);
		model.setFromDate(fromDate);
		model.setToDate(toDate);
		model.setPersonnelId(personnelId);
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

	// --- ADVANCED NEW API METHODS ---

	public ResponseModel getAdvancedInOutHistoryInformation(InOutHistoryInputModel inOutHistoryInputModel) {
		ResponseModel responseModel = new ResponseModel();
		try {
			List<ResparkPersonnelAttendanceDTO> personnelAttendanceModelList = new ArrayList<>();
			List<PersonnelAttendanceProjectionForInOutHistory> personnelAttendanceData =
					personnelAttendanceRepository.getInOutHistoryBetweenDates(inOutHistoryInputModel.getFromDate(),
							inOutHistoryInputModel.getToDate(), BiometricApplicationNames.RESPARK.name(), inOutHistoryInputModel.getTenantId(),
							inOutHistoryInputModel.getStoreId(), inOutHistoryInputModel.getPersonnelIds());
			Set<Long> personnelIdsFromInput = new HashSet<>(inOutHistoryInputModel.getPersonnelIds());
			Set<Long> personnelIdsWithAttendance = new HashSet<>();
			if (!personnelAttendanceData.isEmpty()) {
				Optional<TenantStoreProjection> tenantStoreProjectionOptional =
						tenantCompanyMappingRepository.getTenantStoreMapping(inOutHistoryInputModel.getTenantId(),
								inOutHistoryInputModel.getStoreId(), BiometricApplicationNames.RESPARK.name());
				ZoneId zoneId = ZoneId.systemDefault();
				if (tenantStoreProjectionOptional.isPresent()) {
					zoneId = ZoneId.of(tenantStoreProjectionOptional.get().getTimeZone());
				}

				Map<Long, List<PersonnelAttendanceProjectionForInOutHistory>> personnelWiseAttendance = personnelAttendanceData.stream()
						.collect(Collectors.groupingBy(PersonnelAttendanceProjectionForInOutHistory::getPersonnelId));
				if (!personnelWiseAttendance.isEmpty()) {
					for (Map.Entry<Long, List<PersonnelAttendanceProjectionForInOutHistory>> entry : personnelWiseAttendance.entrySet()) {
						ResparkPersonnelAttendanceDTO personnelAttendanceModel = this.processAdvancedPersonnelAttendance(
								entry.getKey(), entry.getValue(),
								inOutHistoryInputModel.getTenantId(),
								inOutHistoryInputModel.getStoreId(),
								inOutHistoryInputModel.getApplicationName(),
								inOutHistoryInputModel.getFromDate(),
								inOutHistoryInputModel.getToDate(),
								zoneId
						);
						personnelAttendanceModelList.add(personnelAttendanceModel);
						personnelIdsWithAttendance.add(entry.getKey());
					}
				}
			}

			personnelIdsFromInput.removeAll(personnelIdsWithAttendance);
			if (!personnelIdsFromInput.isEmpty()) {
				List<PersonnelDetails> personnelListWithoutAttendance = personnelDetailsRepository.getPersonnelById(new ArrayList<>(personnelIdsFromInput));
				if (!personnelListWithoutAttendance.isEmpty()) {
					for (PersonnelDetails personnel: personnelListWithoutAttendance) {
						ResparkPersonnelAttendanceDTO personnelAttendanceModel =
								this.createAdvancedPersonnelAttendanceModel(personnel.getId(),
										inOutHistoryInputModel.getTenantId(), inOutHistoryInputModel.getStoreId(),
										inOutHistoryInputModel.getApplicationName(), inOutHistoryInputModel.getFromDate(),
										inOutHistoryInputModel.getToDate());
						this.populateAdvancedPersonnelDetails(personnelAttendanceModel,
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
			logger.error("Exception inside getAdvancedInOutHistoryInformation: {}", ex.getMessage());
			throw ex;
		}
		return responseModel;
	}

	private ResparkPersonnelAttendanceDTO processAdvancedPersonnelAttendance(
			Long personnelId,
			List<PersonnelAttendanceProjectionForInOutHistory> individualPersonnelAttendanceList,
			Long tenantId, Long storeId, String applicationName,
			LocalDate fromDate, LocalDate toDate, ZoneId zoneId) {
		ResparkPersonnelAttendanceDTO personnelModel = this.createAdvancedPersonnelAttendanceModel(
				personnelId, tenantId, storeId, applicationName, fromDate, toDate);
		if (!individualPersonnelAttendanceList.isEmpty()) {
			this.populateAdvancedPersonnelDetails(personnelModel,
					individualPersonnelAttendanceList.get(0).getPersonnelGender(),
					individualPersonnelAttendanceList.get(0).getPersonnelName(),
					individualPersonnelAttendanceList.get(0).getPersonnelDesignation(),
					individualPersonnelAttendanceList.get(0).getPersonnelMobileNumber());

			Map<LocalDate, List<PersonnelAttendanceProjectionForInOutHistory>> dateWisePersonnelAttendance = individualPersonnelAttendanceList.stream()
					.collect(Collectors.groupingBy(PersonnelAttendanceProjectionForInOutHistory::getAttendanceDate));
			List<ResparkDayWiseAttendanceDTO> dayWiseAttendanceList = new ArrayList<>();
			if (!dateWisePersonnelAttendance.isEmpty()) {
			    List<SStaffShifts> shifts = staffShiftsRepository.findByTenantIdAndStoreIdAndStaffIdAndShiftDateBetween(tenantId, storeId, personnelId, fromDate, toDate);
			    
				for (Map.Entry<LocalDate, List<PersonnelAttendanceProjectionForInOutHistory>> entry : dateWisePersonnelAttendance.entrySet()) {
				    SStaffShifts dailyShift = shifts.stream().filter(s -> s.getShiftDate() != null && s.getShiftDate().equals(entry.getKey())).findFirst().orElse(null);
					ResparkDayWiseAttendanceDTO dayWiseAttendance = this.processAdvancedDateWiseAttendance(
							entry.getKey(), entry.getValue(), zoneId, dailyShift
					);
					dayWiseAttendanceList.add(dayWiseAttendance);
				}
				personnelModel.setDayWiseAttendanceList(dayWiseAttendanceList);
			}
		}
		return personnelModel;
	}

	private ResparkPersonnelAttendanceDTO createAdvancedPersonnelAttendanceModel(Long personnelId, Long tenantId,
																	Long storeId, String applicationName,
																	LocalDate fromDate, LocalDate toDate) {
		ResparkPersonnelAttendanceDTO model = new ResparkPersonnelAttendanceDTO();
		model.setTenantId(tenantId);
		model.setStoreId(storeId);
		model.setApplicationName(applicationName);
		model.setFromDate(fromDate);
		model.setToDate(toDate);
		model.setPersonnelId(personnelId);
		return model;
	}

	private void populateAdvancedPersonnelDetails(ResparkPersonnelAttendanceDTO model,
										  String personnelGender,
										  String personnelName,
										  String personnelDesignation,
										  String personnelMobileNumber) {
		model.setPersonnelGender(personnelGender);
		model.setPersonnelName(personnelName);
		model.setPersonnelDesignation(personnelDesignation);
		model.setPersonnelMobileNumber(personnelMobileNumber);
	}

	private ResparkDayWiseAttendanceDTO processAdvancedDateWiseAttendance(
			LocalDate dateOfAttendance,
			List<PersonnelAttendanceProjectionForInOutHistory> individualDateWiseAttendance,
			ZoneId zoneId,
			com.relfor.pcs.payroll.entity.SStaffShifts shiftRecord) {
		ResparkDayWiseAttendanceDTO dayAttendance = new ResparkDayWiseAttendanceDTO();
		dayAttendance.setDateOfAttendance(dateOfAttendance);
		
		String shiftStart = "09:30";
		String shiftEnd = "18:30";
		if (shiftRecord != null && shiftRecord.getSlot() != null && shiftRecord.getSlot().contains("-")) {
		    String[] parts = shiftRecord.getSlot().split("-");
		    if (parts.length == 2) {
		        shiftStart = parts[0].trim();
		        shiftEnd = parts[1].trim();
		    }
		}
		dayAttendance.setShiftStartTime(shiftStart);
		dayAttendance.setShiftEndTime(shiftEnd);
		
		if (!individualDateWiseAttendance.isEmpty()) {
			dayAttendance.setDayOfAttendance(individualDateWiseAttendance.get(0).getAttendanceDayOfWeek());
			List<ResparkIndividualPunchesDTO> punchesList = new ArrayList<>();
			punchesList = this.processAdvancedIndividualPunches(individualDateWiseAttendance,
					punchesList, zoneId);
			dayAttendance.setIndividualPunchesList(punchesList);
			
			if (!punchesList.isEmpty()) {
			    String inTime = punchesList.get(0).getPunchTime().toString().substring(0, 5);
			    dayAttendance.setFirstInTime(inTime);
			    
			    String outTime = null;
			    if (punchesList.size() > 1 || punchesList.get(0).getCurrentStatus().equalsIgnoreCase("OUT")) {
			        outTime = punchesList.get(punchesList.size() - 1).getPunchTime().toString().substring(0, 5);
			        dayAttendance.setLastOutTime(outTime);
			    }
			    
			    int shiftStartMins = toMins(shiftStart);
			    int inMins = toMins(inTime);
			    if (inMins > shiftStartMins) {
			        dayAttendance.setLateInMinutes(inMins - shiftStartMins);
			    } else {
			        dayAttendance.setLateInMinutes(0);
			    }
			    
			    if (outTime != null) {
			        int shiftEndMins = toMins(shiftEnd);
			        int outMins = toMins(outTime);
			        if (outMins < shiftEndMins) {
			            dayAttendance.setEarlyOutMinutes(shiftEndMins - outMins);
			        } else {
			            dayAttendance.setEarlyOutMinutes(0);
			        }
			        
			        int dur = outMins - inMins;
			        if (dur > 0) {
			            dayAttendance.setTotalDurationMinutes(dur);
			        }
			    }
			    
			    dayAttendance.setCurrentStatus("Present");
			} else {
			    if (shiftRecord != null) {
			        if (shiftRecord.getWeeklyOff()) dayAttendance.setCurrentStatus("Weekly Off");
			        else if (shiftRecord.getOnLeave()) dayAttendance.setCurrentStatus("Leave");
			        else dayAttendance.setCurrentStatus("Absent");
			    }
			}
		}
		return dayAttendance;
	}
	
	private int toMins(String time) {
	    if (time == null || !time.contains(":")) return 0;
	    String[] parts = time.split(":");
	    return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
	}

	private List<ResparkIndividualPunchesDTO> processAdvancedIndividualPunches(
			List<PersonnelAttendanceProjectionForInOutHistory> attendanceList,
			List<ResparkIndividualPunchesDTO> punchesList,
			ZoneId zoneId) {
		attendanceList.sort(Comparator.comparing(PersonnelAttendanceProjectionForInOutHistory::getPunchTimestamp)
				.thenComparing(PersonnelAttendanceProjectionForInOutHistory::getCurrentStatus, Comparator.nullsFirst(Comparator.naturalOrder())));

		for (int i = 0; i < attendanceList.size(); i++) {
			PersonnelAttendanceProjectionForInOutHistory record = attendanceList.get(i);
			ResparkIndividualPunchesDTO punch = new ResparkIndividualPunchesDTO();
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