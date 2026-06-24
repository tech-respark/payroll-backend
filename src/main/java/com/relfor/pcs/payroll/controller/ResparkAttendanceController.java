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
		ResponseModel responseModel = resparkAttendanceService.regularizeAttendance(attendanceRegularizationInputModel);
		return ResponseHandler.generateResponseModel(responseModel);
	}

	@PostMapping("/inOutHistoryInformation")
	public ResponseEntity<?> getInOutHistoryInformation(@RequestBody InOutHistoryInputModel inOutHistoryInputModel){
		ResponseModel responseModel = resparkInOutHistoryService.getInOutHistoryInformation(inOutHistoryInputModel);
		return ResponseHandler.generateResponseModel(responseModel);
	}

	@PostMapping("/advancedInOutHistoryInformation")
	public ResponseEntity<?> getAdvancedInOutHistoryInformation(@RequestBody InOutHistoryInputModel inOutHistoryInputModel){
		ResponseModel responseModel = resparkInOutHistoryService.getAdvancedInOutHistoryInformation(inOutHistoryInputModel);
		return ResponseHandler.generateResponseModel(responseModel);
	}

	@PostMapping("/regularizationRequests")
	public ResponseEntity<?> getRegularizationRequests(@RequestBody InOutHistoryInputModel inOutHistoryInputModel){
		ResponseModel responseModel = resparkAttendanceService.getRegularizationRequests(inOutHistoryInputModel);
		return ResponseHandler.generateResponseModel(responseModel);
	}

	@PostMapping("/flagRegularizationRequests")
	public ResponseEntity<?> flagRegularizationRequests(@RequestBody List<AttendanceRequestsDTO> inputAttendanceRequestsDTOList){
		ResponseModel responseModel = resparkAttendanceService.flagRegularizationRequests(inputAttendanceRequestsDTOList);
		return ResponseHandler.generateResponseModel(responseModel);
	}

	@GetMapping("/personnelAttendanceForADay")
	public ResponseEntity<?> getPersonnelAttendanceForADay(
			@RequestParam Long tenantId,
			@RequestParam Long storeId,
			@RequestParam Long personnelId,
			@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate attendanceDate
	){
		tenantId = SecurityUtils.getTenantId(tenantId);
		storeId = SecurityUtils.getStoreId(storeId);
		ResponseModel responseModel = resparkInOutHistoryService.getPersonnelAttendanceForADay(tenantId, storeId, personnelId, attendanceDate);
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