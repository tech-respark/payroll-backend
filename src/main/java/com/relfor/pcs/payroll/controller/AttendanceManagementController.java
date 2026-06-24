package com.relfor.pcs.payroll.controller;

import com.relfor.pcs.payroll.dto.ResponseModel;
import com.relfor.pcs.payroll.dto.PersonnelAttendanceModel;
import com.relfor.pcs.payroll.dto.PersonnelDetailsRequestModel;
import com.relfor.pcs.payroll.dto.StaffDTO;
import com.relfor.pcs.payroll.service.AttendanceManagementService;
import com.relfor.pcs.payroll.util.ResponseHandler;
import com.relfor.pcs.payroll.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll-management/v1")
public class AttendanceManagementController {
	@Autowired
	AttendanceManagementService attendanceManagementService;

	@PostMapping("/personnelForAttendanceManagement")
	public ResponseEntity<?> enrollPersonnelForAttendanceManagement(@RequestBody StaffDTO staffDTO){
		ResponseModel responseModel = attendanceManagementService.enrollPersonnelForAttendanceManagement(staffDTO);
		return ResponseHandler.generateResponseModel(responseModel);
	}

	@PostMapping("/personnelAttendanceSummary")
	public ResponseEntity<?> getPersonnelAttendanceSummary(@RequestBody PersonnelAttendanceModel personnelAttendanceModel){
		ResponseModel responseModel = attendanceManagementService.getPersonnelAttendanceSummary(personnelAttendanceModel);
		return ResponseHandler.generateResponseModel(responseModel);
	}

	@PostMapping("/personnelDetailsForListOfStaffIds")
	public ResponseEntity<?> getPersonnelDetailsForListOfStaffIds(@RequestBody List<Long> staffIds) {
		ResponseModel responseModel = attendanceManagementService.getPersonnelDetailsForListOfStaffIds(staffIds);
		return ResponseHandler.generateResponseModel(responseModel);
	}

	@PostMapping("/updatePasswordForPersonnel")
	public void updatePasswordForPersonnel(@RequestBody StaffDTO staffDTO) {
		attendanceManagementService.updatePasswordForPersonnel(staffDTO);
	}

	@GetMapping("/personnel/all")
	public ResponseEntity<?> getAllPersonnel(@RequestParam Long tenantId, @RequestParam Long storeId) {
        tenantId = SecurityUtils.getTenantId(tenantId);
        storeId = SecurityUtils.getStoreId(storeId);
		try {
			List<StaffDTO> result = attendanceManagementService.getAllPersonnelByTenantAndStore(tenantId, storeId);
			return ResponseHandler.generateResponse("OK", null, HttpStatus.OK, result);
		} catch (Exception e) {
			return ResponseHandler.generateResponse("Error", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);
		}
	}
}