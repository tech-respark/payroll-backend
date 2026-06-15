package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.model.AttendanceRequestsDTO;
import com.relfor.pcs.payroll.model.InOutHistoryInputModel;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

import java.time.ZoneId;

@Repository
public interface PersonnelAttendanceCustomRepo {
	Page<AttendanceRequestsDTO> getRegularizationRequests(InOutHistoryInputModel inOutHistoryInputModel, ZoneId zoneId);
}