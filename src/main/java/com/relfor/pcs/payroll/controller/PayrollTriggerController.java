package com.relfor.pcs.payroll.controller;

import com.relfor.pcs.payroll.dto.ResponseModel;
import com.relfor.pcs.payroll.service.PayrollTriggerService;
import com.relfor.pcs.payroll.util.ResponseHandler;
import com.relfor.pcs.payroll.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/payroll-management/v1/trigger")
public class PayrollTriggerController {
	@Autowired
	PayrollTriggerService payrollTriggerService;

	@PostMapping("/retrieveAttendanceData")
	@PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_ATTENDANCE)")
	public ResponseEntity<?> retrieveAttendanceDataOnDemand(@RequestParam Long tenantId,
															@RequestParam(defaultValue = "0") Long storeId,
															@RequestParam Instant fromDate,
															@RequestParam(required = false) Instant toDate){
		tenantId = SecurityUtils.getTenantId(tenantId);
		storeId = SecurityUtils.getStoreId(storeId);
		ResponseModel responseModel = payrollTriggerService.retrieveAttendanceDataOnDemand(tenantId, storeId, fromDate, toDate);
		return ResponseHandler.generateResponseModel(responseModel);
	}

	@PostMapping("/calculateMonthWiseSummary")
	@PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_ATTENDANCE)")
	public ResponseEntity<?> calculateMonthWiseSummary(@RequestParam Long tenantId,
													 @RequestParam(defaultValue = "0") Long storeId,
													 @RequestParam String month,
													 @RequestParam Integer year){
		tenantId = SecurityUtils.getTenantId(tenantId);
		storeId = SecurityUtils.getStoreId(storeId);
		ResponseModel responseModel = payrollTriggerService.calculateMonthWiseSummary(tenantId, storeId, month, year);
		return ResponseHandler.generateResponseModel(responseModel);
	}

	@PostMapping("/calculateSalary")
	@PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY)")
	public ResponseEntity<?> calculateSalary(@RequestParam Long tenantId,
											 @RequestParam(defaultValue = "0") Long storeId,
											 @RequestParam String month,
											 @RequestParam Integer year){
		tenantId = SecurityUtils.getTenantId(tenantId);
		storeId = SecurityUtils.getStoreId(storeId);
		ResponseModel responseModel = payrollTriggerService.calculateSalary(tenantId, storeId, month, year);
		return ResponseHandler.generateResponseModel(responseModel);
	}
}