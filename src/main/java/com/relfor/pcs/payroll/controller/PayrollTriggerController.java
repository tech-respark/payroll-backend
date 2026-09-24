package com.relfor.pcs.payroll.controller;

import com.relfor.pcs.payroll.dto.ResponseModel;
import com.relfor.pcs.payroll.dto.StaffDTO;
import com.relfor.pcs.payroll.service.PayrollTriggerService;
import com.relfor.pcs.payroll.util.ResponseHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/payroll-management/v1/trigger")
public class PayrollTriggerController {
	@Autowired
	PayrollTriggerService payrollTriggerService;

	@PostMapping("/retrieveAttendanceData")
	public ResponseEntity<?> retrieveAttendanceDataOnDemand(@RequestParam Long tenantId,
															@RequestParam(defaultValue = "0") Long storeId,
															@RequestParam Instant fromDate,
															@RequestParam(required = false) Instant toDate){
		ResponseModel responseModel = payrollTriggerService.retrieveAttendanceDataOnDemand(tenantId, storeId, fromDate, toDate);
		return ResponseHandler.generateResponseModel(responseModel);
	}

	@PostMapping("/calculateMonthWiseSummary")
	public ResponseEntity<?> calculateMonthWiseSummary(@RequestParam Long tenantId,
													 @RequestParam(defaultValue = "0") Long storeId,
													 @RequestParam String month,
													 @RequestParam Integer year){
		ResponseModel responseModel = payrollTriggerService.calculateMonthWiseSummary(tenantId, storeId, month, year);
		return ResponseHandler.generateResponseModel(responseModel);
	}

	@PostMapping("/calculateSalary")
	public ResponseEntity<?> calculateSalary(@RequestParam Long tenantId,
											 @RequestParam(defaultValue = "0") Long storeId,
											 @RequestParam String month,
											 @RequestParam Integer year){
		ResponseModel responseModel = payrollTriggerService.calculateSalary(tenantId, storeId, month, year);
		return ResponseHandler.generateResponseModel(responseModel);
	}
}