package com.relfor.pcs.payroll.controller;

import com.relfor.pcs.payroll.dto.ResponseModel;
import com.relfor.pcs.payroll.model.AttendanceRegularizationInputModel;
import com.relfor.pcs.payroll.model.AttendanceRequestsDTO;
import com.relfor.pcs.payroll.model.InOutHistoryInputModel;
import com.relfor.pcs.payroll.service.ResparkAttendanceService;
import com.relfor.pcs.payroll.service.ResparkInOutHistoryService;
import com.relfor.pcs.payroll.util.ResponseHandler;
import com.relfor.pcs.payroll.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/payroll-management/v1")
public class ResparkAttendanceController {
	@Autowired
	ResparkAttendanceService resparkAttendanceService;
	@Autowired
	ResparkInOutHistoryService resparkInOutHistoryService;

	@PostMapping("/regularizeAttendance")
	public ResponseEntity<?> regularizeAttendance(@RequestBody AttendanceRegularizationInputModel attendanceRegularizationInputModel) {
		SecurityUtils.requireSelfOrAnyAuthority(attendanceRegularizationInputModel.getStaffId(),
				com.relfor.pcs.payroll.security.Permissions.MANAGE_ATTENDANCE);
		ResponseModel responseModel = resparkAttendanceService.regularizeAttendance(attendanceRegularizationInputModel);
		return ResponseHandler.generateResponseModel(responseModel);
	}

	@PostMapping("/inOutHistoryInformation")
	public ResponseEntity<?> getInOutHistoryInformation(@RequestBody InOutHistoryInputModel inOutHistoryInputModel){
		inOutHistoryInputModel.setStaffIds(SecurityUtils.constrainStaffIds(inOutHistoryInputModel.getStaffIds()));
		ResponseModel responseModel = resparkInOutHistoryService.getInOutHistoryInformation(inOutHistoryInputModel);
		return ResponseHandler.generateResponseModel(responseModel);
	}

	@PostMapping("/advancedInOutHistoryInformation")
	public ResponseEntity<?> getAdvancedInOutHistoryInformation(@RequestBody InOutHistoryInputModel inOutHistoryInputModel){
		inOutHistoryInputModel.setStaffIds(SecurityUtils.constrainStaffIds(inOutHistoryInputModel.getStaffIds()));
		ResponseModel responseModel = resparkInOutHistoryService.getAdvancedInOutHistoryInformation(inOutHistoryInputModel);
		return ResponseHandler.generateResponseModel(responseModel);
	}

	@PostMapping("/regularizationRequests")
	public ResponseEntity<?> getRegularizationRequests(@RequestBody InOutHistoryInputModel inOutHistoryInputModel){
		inOutHistoryInputModel.setStaffIds(SecurityUtils.constrainStaffIds(inOutHistoryInputModel.getStaffIds()));
		ResponseModel responseModel = resparkAttendanceService.getRegularizationRequests(inOutHistoryInputModel);
		return ResponseHandler.generateResponseModel(responseModel);
	}

	@PostMapping("/flagRegularizationRequests")
	@PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_ATTENDANCE)")
	public ResponseEntity<?> flagRegularizationRequests(@RequestBody List<AttendanceRequestsDTO> inputAttendanceRequestsDTOList){
		ResponseModel responseModel = resparkAttendanceService.flagRegularizationRequests(inputAttendanceRequestsDTOList);
		return ResponseHandler.generateResponseModel(responseModel);
	}

	@GetMapping("/personnelAttendanceForADay")
	public ResponseEntity<?> getPersonnelAttendanceForADay(
			@RequestParam Long tenantId,
			@RequestParam Long storeId,
			@RequestParam Long staffId,
			@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate attendanceDate
	){
		tenantId = SecurityUtils.getTenantId(tenantId);
		storeId = SecurityUtils.getStoreId(storeId);
		SecurityUtils.requireSelfOrAnyAuthority(staffId, com.relfor.pcs.payroll.security.Permissions.MANAGE_ATTENDANCE);
		ResponseModel responseModel = resparkInOutHistoryService.getPersonnelAttendanceForADay(tenantId, storeId, staffId, attendanceDate);
		return ResponseHandler.generateResponseModel(responseModel);
	}

	@GetMapping("/tenantStoreConfig")
	public ResponseEntity<?> getTenantStoreConfiguration(@RequestParam Long tenantId,
														 @RequestParam Long storeId) {
		tenantId = SecurityUtils.getTenantId(tenantId);
		storeId =SecurityUtils.getStoreId(storeId);
		ResponseModel responseModel = resparkAttendanceService.getTenantStoreConfiguration(tenantId, storeId);
		return ResponseHandler.generateResponseModel(responseModel);
	}
}