package com.relfor.pcs.payroll.repository;

import com.google.common.base.CaseFormat;
import com.relfor.pcs.payroll.model.AttendanceRequestsDTO;
import com.relfor.pcs.payroll.model.InOutHistoryInputModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PersonnelAttendanceCustomRepoImpl implements PersonnelAttendanceCustomRepo {
	@PersistenceContext
	EntityManager entityManager;

	@SuppressWarnings("unchecked")
	@Override
	public Page<AttendanceRequestsDTO> getRegularizationRequests(InOutHistoryInputModel inOutHistoryInputModel, ZoneId zoneId) {
		String sqlString = this.buildSearchSqlForGetRegularizationRequests(inOutHistoryInputModel);
		Query query = this.buildQueryForGetRegularizationRequests(inOutHistoryInputModel, sqlString, false);
		this.setBindingParameterForGetSalesDetails(inOutHistoryInputModel, query);
		List<Object[]> objectList = query.getResultList();
		List<AttendanceRequestsDTO> attendanceRequestsDTOList = new ArrayList<>();
		this.constructAttendanceRequestsDTOListFromObjectList(objectList, attendanceRequestsDTOList, zoneId);

		sqlString = this.buildCountSqlForGetSalesDetails(inOutHistoryInputModel);
		query = this.buildQueryForGetRegularizationRequests(inOutHistoryInputModel, sqlString, true);
		this.setBindingParameterForGetSalesDetails(inOutHistoryInputModel, query);
		Object object = query.getSingleResult();
		int count = 0;
		if (object != null) {
			if (object instanceof Integer) {
				count = ((Integer)object).intValue();
			} else if (object instanceof Long) {
				count = ((Long)object).intValue();
			} else if (object instanceof BigInteger) {
				count = ((BigInteger)object).intValue();
			}
		}
		return new PageImpl<>(attendanceRequestsDTOList, this.createPageable(inOutHistoryInputModel, count), count);
	}

	private String buildSearchSqlForGetRegularizationRequests(InOutHistoryInputModel inOutHistoryInputModel) {
		StringBuilder sqlString = new StringBuilder();
		sqlString.append("SELECT pa.tenant_id AS tenantId, ");
		sqlString.append("pa.store_id AS storeId, ");
		sqlString.append("pa.id AS personnelAttendanceId, ");
		sqlString.append("pa.personnel_id AS personnelId, ");
		sqlString.append("pd.designation AS personnelDesignation, ");
		sqlString.append("pd.gender AS personnelGender, ");
		sqlString.append("pd.personnel_mobile_number AS personnelMobileNumber, ");
		sqlString.append("CONCAT(COALESCE(pd.first_name, ''), ' ', COALESCE(pd.last_name, '')) AS personnelName, ");
		sqlString.append("pa.attendance_date AS attendanceDate, ");
		sqlString.append("pa.attendance_day_of_week AS attendanceDayOfWeek, ");
		sqlString.append("pa.punch_timestamp AS punchTimestamp, ");
		sqlString.append("pa.created_timestamp AS createdTimestamp, ");
		sqlString.append("pa.created_by AS createdBy, ");
		sqlString.append("pa.modified_timestamp AS modifiedTimestamp, ");
		sqlString.append("pa.modified_by AS modifiedBy, ");
		sqlString.append("pa.application_name AS applicationName, ");
		sqlString.append("pa.remark AS remark, ");
		sqlString.append("pa.current_status AS currentStatus, ");
		sqlString.append("pa.punch_event AS punchEvent ");
		sqlString.append("FROM personnel_attendance pa ");
		sqlString.append("JOIN personnel_details pd ");
		sqlString.append("ON pd.id = pa.personnel_id WHERE ");
		return this.appendWhereForGetRegularizationRequests(inOutHistoryInputModel, sqlString, false);
	}

	private String buildCountSqlForGetSalesDetails(InOutHistoryInputModel inOutHistoryInputModel) {
		StringBuilder sqlString = new StringBuilder();
		sqlString.append("SELECT COUNT(pa.id) ");
		sqlString.append("FROM personnel_attendance pa ");
		sqlString.append("JOIN personnel_details pd ");
		sqlString.append("ON pd.id = pa.personnel_id WHERE ");
		return this.appendWhereForGetRegularizationRequests(inOutHistoryInputModel, sqlString, true);
	}

	private String appendWhereForGetRegularizationRequests(InOutHistoryInputModel inOutHistoryInputModel, StringBuilder sqlString, Boolean isCount) {
		boolean start = true;
		if (!ObjectUtils.isEmpty(inOutHistoryInputModel.getTenantId())) {
			if (start) {
				sqlString.append("pa.tenant_id = :tenantId ");
				start = false;
			}else {
				sqlString.append("AND pa.tenant_id = :tenantId ");
			}
		}

		if (!ObjectUtils.isEmpty(inOutHistoryInputModel.getStoreId())){
			if (start) {
				sqlString.append("pa.store_id = :storeId ");
				start = false;
			}else {
				sqlString.append("AND pa.store_id = :storeId ");
			}
		}
		if (!StringUtils.isEmpty(inOutHistoryInputModel.getApplicationName())){
			if (start) {
				sqlString.append("pa.application_name = :applicationName ");
				start = false;
			}else {
				sqlString.append("AND pa.application_name = :applicationName ");
			}
		}
		if (!StringUtils.isEmpty(inOutHistoryInputModel.getCurrentStatus())){
			if (start) {
				sqlString.append("pa.current_status = :currentStatus ");
				start = false;
			}else {
				sqlString.append("AND pa.current_status = :currentStatus ");
			}
		}
		if (!ObjectUtils.isEmpty(inOutHistoryInputModel.getFromDate()) && !ObjectUtils.isEmpty(inOutHistoryInputModel.getToDate())){
			if (start) {
				sqlString.append("pa.attendance_date BETWEEN :fromDate AND :toDate ");
				start = false;
			}else {
				sqlString.append("AND pa.attendance_date BETWEEN :fromDate AND :toDate ");
			}
		}
		if (!ObjectUtils.isEmpty(inOutHistoryInputModel.getPersonnelId())) {
			if (start) {
				sqlString.append("pa.personnel_id = :personnelId ");
				start = false;
			}else {
				sqlString.append("AND pa.personnel_id = :personnelId ");
			}
		}
		if (!ObjectUtils.isEmpty(inOutHistoryInputModel.getUploadSource())) {
			if (start) {
				sqlString.append("pa.upload_source = :uploadSource ");
				start = false;
			}else {
				sqlString.append("AND pa.upload_source = :uploadSource ");
			}
		}
		if (!isCount) {
			if (!StringUtils.isBlank(inOutHistoryInputModel.getSortField())) {
				sqlString.append(" ORDER BY ").append(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, inOutHistoryInputModel.getSortField()));
			} else {
				sqlString.append(" ORDER BY pa.attendance_date");
			}
			if (!StringUtils.isBlank(inOutHistoryInputModel.getSortOrder())) {
				sqlString.append(" ").append(inOutHistoryInputModel.getSortOrder());
			} else {
				sqlString.append(" DESC");
			}
		}

		return sqlString.toString();
	}

	private Query buildQueryForGetRegularizationRequests(InOutHistoryInputModel inOutHistoryInputModel, String sqlString, Boolean isCount) {
		Query query = entityManager.createNativeQuery(sqlString);
		if (!isCount && !ObjectUtils.isEmpty(inOutHistoryInputModel.getPageModel())) {
			int firstResultOnPage = 0;
			int recordsPerPage = 10;
			if (inOutHistoryInputModel.getPageModel().getRecordsPerPage() != null && inOutHistoryInputModel.getPageModel().getRecordsPerPage() != 0) {
				recordsPerPage = inOutHistoryInputModel.getPageModel().getRecordsPerPage();
			}
			if (inOutHistoryInputModel.getPageModel().getPageNumber() != null && inOutHistoryInputModel.getPageModel().getPageNumber() != 0) {
				firstResultOnPage = (inOutHistoryInputModel.getPageModel().getPageNumber()-1)*recordsPerPage;
			}
			query.setFirstResult(firstResultOnPage);
			query.setMaxResults(recordsPerPage);
		}

		return query;
	}

	private void setBindingParameterForGetSalesDetails(InOutHistoryInputModel inOutHistoryInputModel, Query query) {
		if (!ObjectUtils.isEmpty(inOutHistoryInputModel.getTenantId())) {
			query.setParameter("tenantId", inOutHistoryInputModel.getTenantId());
		}
		if (!ObjectUtils.isEmpty(inOutHistoryInputModel.getStoreId())) {
			query.setParameter("storeId", inOutHistoryInputModel.getStoreId());
		}
		if (!StringUtils.isEmpty(inOutHistoryInputModel.getApplicationName())) {
			query.setParameter("applicationName", inOutHistoryInputModel.getApplicationName());
		}
		if (!StringUtils.isEmpty(inOutHistoryInputModel.getCurrentStatus())) {
			query.setParameter("currentStatus", inOutHistoryInputModel.getCurrentStatus());
		}
		if (!ObjectUtils.isEmpty(inOutHistoryInputModel.getFromDate()) && !ObjectUtils.isEmpty(inOutHistoryInputModel.getToDate())){
			query.setParameter("fromDate", inOutHistoryInputModel.getFromDate());
			query.setParameter("toDate", inOutHistoryInputModel.getToDate());
		}
		if (!ObjectUtils.isEmpty(inOutHistoryInputModel.getPersonnelId())) {
			query.setParameter("personnelId", inOutHistoryInputModel.getPersonnelId());
		}
		if (!ObjectUtils.isEmpty(inOutHistoryInputModel.getUploadSource())) {
			query.setParameter("uploadSource", inOutHistoryInputModel.getUploadSource());
		}
	}

	private Pageable createPageable(InOutHistoryInputModel inOutHistoryInputModel, int count) {
		int pageNumber = 0;
		int recordsPerPage = count;
		if (!ObjectUtils.isEmpty(inOutHistoryInputModel.getPageModel())) {
			if (inOutHistoryInputModel.getPageModel().getRecordsPerPage() != null && inOutHistoryInputModel.getPageModel().getRecordsPerPage() != 0) {
				recordsPerPage = inOutHistoryInputModel.getPageModel().getRecordsPerPage();
			} else {
				recordsPerPage = 10;
			}
			if (inOutHistoryInputModel.getPageModel().getPageNumber() != null && inOutHistoryInputModel.getPageModel().getPageNumber() != 0) {
				pageNumber = inOutHistoryInputModel.getPageModel().getPageNumber();
			}
		}
		if (pageNumber == 0) {
			pageNumber = 1;
		}
		return PageRequest.of(pageNumber - 1, Math.max(recordsPerPage, 1));
	}

	private void constructAttendanceRequestsDTOListFromObjectList(List<Object[]> objectList,
																  List<AttendanceRequestsDTO> attendanceRequestsDTOList,
																  ZoneId zoneId){
		if (!objectList.isEmpty()) {
			for (Object[] object: objectList) {
				AttendanceRequestsDTO attendanceRequestsDTO = new AttendanceRequestsDTO();
				if (object[0]!=null) {
					attendanceRequestsDTO.setTenantId(Long.valueOf(String.valueOf(object[0])));
				}
				if (object[1]!=null) {
					attendanceRequestsDTO.setStoreId(Long.valueOf(String.valueOf(object[1])));
				}
				if (object[2]!=null) {
					attendanceRequestsDTO.setPersonnelAttendanceId(Long.valueOf(String.valueOf(object[2])));
				}
				if (object[3]!=null) {
					attendanceRequestsDTO.setPersonnelId(Long.valueOf(String.valueOf(object[3])));
				}
				if (object[4]!=null) {
					attendanceRequestsDTO.setPersonnelDesignation(String.valueOf(object[4]));
				}
				if (object[5]!=null) {
					attendanceRequestsDTO.setPersonnelGender(String.valueOf(object[5]));
				}
				if (object[6]!=null) {
					attendanceRequestsDTO.setPersonnelMobileNumber(String.valueOf(object[6]));
				}
				if (object[7]!=null) {
					attendanceRequestsDTO.setPersonnelName(String.valueOf(object[7]));
				}
				if (object[8]!=null) {
					if (object[8] instanceof LocalDate) {
						attendanceRequestsDTO.setAttendanceDate((LocalDate) object[8]);
					} else if (object[8] instanceof Date) {
						attendanceRequestsDTO.setAttendanceDate(((Date) object[8]).toLocalDate());
					} else if (object[8] instanceof String) {
						attendanceRequestsDTO.setAttendanceDate(LocalDate.parse((String) object[8]));
					}
				}
				if (object[9]!=null) {
					attendanceRequestsDTO.setAttendanceDayOfWeek(String.valueOf(object[9]));
				}
				if (object[10]!=null) {
					Instant punchTimeStamp = null;
					if (object[10] instanceof Instant) {
						punchTimeStamp = ((Instant) object[10]);
					} else if (object[10] instanceof Timestamp) {
						punchTimeStamp = (((Timestamp) object[10]).toInstant());
					} else if (object[10] instanceof String) {
						punchTimeStamp = (Instant.parse((String) object[10]));
					}

					if (punchTimeStamp != null) {
						attendanceRequestsDTO.setPunchDate(punchTimeStamp.atZone(zoneId).toLocalDate());
						attendanceRequestsDTO.setPunchTime(punchTimeStamp.atZone(zoneId).toLocalTime());
					}
				}
				if (object[11]!=null) {
					if (object[11] instanceof Instant) {
						attendanceRequestsDTO.setCreatedTimestamp((Instant) object[11]);
					} else if (object[11] instanceof Timestamp) {
						attendanceRequestsDTO.setCreatedTimestamp(((Timestamp) object[11]).toInstant());
					} else if (object[11] instanceof String) {
						attendanceRequestsDTO.setCreatedTimestamp(Instant.parse((String) object[11]));
					}
				}
				if (object[12]!=null) {
					attendanceRequestsDTO.setCreatedBy(Long.parseLong(String.valueOf(object[12])));
				}
				if (object[13]!=null) {
					if (object[13] instanceof Instant) {
						attendanceRequestsDTO.setModifiedTimestamp((Instant) object[13]);
					} else if (object[13] instanceof Timestamp) {
						attendanceRequestsDTO.setModifiedTimestamp(((Timestamp) object[13]).toInstant());
					} else if (object[13] instanceof String) {
						attendanceRequestsDTO.setModifiedTimestamp(Instant.parse((String) object[13]));
					}
				}
				if (object[14]!=null) {
					attendanceRequestsDTO.setModifiedBy(Long.parseLong(String.valueOf(object[14])));
				}
				if (object[15]!=null) {
					attendanceRequestsDTO.setApplicationName(String.valueOf(object[15]));
				}
				if (object[16]!=null) {
					attendanceRequestsDTO.setRemark(String.valueOf(object[16]));
				}
				if (object[17]!=null) {
					attendanceRequestsDTO.setCurrentStatus(String.valueOf(object[17]));
				}
				if (object[18]!=null) {
					attendanceRequestsDTO.setPunchEvent(String.valueOf(object[18]));
				}
				attendanceRequestsDTOList.add(attendanceRequestsDTO);
			}
		}
	}
}
