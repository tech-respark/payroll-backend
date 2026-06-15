package com.relfor.pcs.payroll.controller;


import com.relfor.pcs.payroll.entity.StaffBreakTime;
import com.relfor.pcs.payroll.service.StaffBreakTimeService;
import com.relfor.pcs.payroll.util.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * This class is the controller for the Staff Break Time API. It provides
 * methods for creating, retrieving, updating, and deleting staff break times.
 */
@RestController
@RequestMapping("/payroll-management/v1")
public class StaffBreakTimeController {

	/**
	 * The Staff Break Time service.
	 */

	private final StaffBreakTimeService staffBreakTimeSvc;

	@Autowired
	public StaffBreakTimeController(StaffBreakTimeService staffBreakTimeSvc) {
		this.staffBreakTimeSvc = staffBreakTimeSvc;
	}

	/**
	 * The logger.
	 */
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Creates a new staff break time.
	 *
	 * @param staffBreakTime the staff break time to create
	 * @return the created staff break time
	 */
	@PostMapping("/staffBreakTimes")
	public ResponseEntity<?> postStaffBreakTime(@RequestBody StaffBreakTime staffBreakTime) {
		try {
			if (staffBreakTime != null) {
				staffBreakTime = staffBreakTimeSvc.postStaffBreakTime(staffBreakTime);
			}
		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);
			return ResponseHandler.generateResponse(e.getClass().getName(), e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR, null);
		}
		return ResponseHandler.generateResponse("Ok", null, HttpStatus.OK, staffBreakTime);
	}

	/**
	 * Creates a new staff break time.
	 *
	 * @param staffBreakTime the staff break time to create
	 * @return the created staff break time
	 */
	@PostMapping("/staffBreakTime")
	public ResponseEntity<?> postStaffBreakTime(@RequestBody List<StaffBreakTime> staffBreakTime) {
		try {
			if (staffBreakTime != null) {
				staffBreakTime = staffBreakTimeSvc.postStaffBreakTime(staffBreakTime);
			}
		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);
			return ResponseHandler.generateResponse(e.getClass().getName(), e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR, null);
		}
		return ResponseHandler.generateResponse("Ok", null, HttpStatus.OK, staffBreakTime);
	}

	/**
	 * Retrieves the staff break time for a given tenant and store.
	 *
	 * @param tenantId the tenant ID
	 * @param storeId  the store ID
	 * @return the staff break time for the given tenant and store
	 */
	@GetMapping("/staffBreakTime")
	public ResponseEntity<?> getStaffBreakTime(@RequestParam Long tenantId, @RequestParam Long storeId) {
		try {

			List<StaffBreakTime> sbt = staffBreakTimeSvc.getStaffBreakTime(storeId, tenantId);

			return ResponseHandler.generateResponse("Ok", null, HttpStatus.OK, sbt);
		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);
			return ResponseHandler.generateResponse(e.getClass().getName(), e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR, null);
		}
	}

	/**
	 * Retrieves the staff break time slot for a given staff.
	 *
	 * @param staffId the staff ID
	 * @return the staff break time slot for the given staff
	 */
	@GetMapping("/staffBreakTimeSlot")
	public ResponseEntity<?> getStaffBreakTimeSlot(@RequestParam Long staffId) {
		try {

			List<StaffBreakTime> sbt = staffBreakTimeSvc.getStaffBreakTimeSlot(staffId);

			return ResponseHandler.generateResponse("Ok", null, HttpStatus.OK, sbt);
		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);
			return ResponseHandler.generateResponse(e.getClass().getName(), e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR, null);
		}
	}

	/**
	 * Retrieves the staff break time for a given tenant, store, and date.
	 *
	 * @param tenantId the tenant ID
	 * @param storeId  the store ID
	 * @param date     the date
	 * @return the staff break time for the given tenant, store, and date
	 */
	@GetMapping("/getStaffBreakTime")
	public ResponseEntity<?> getStaffBreakTimeByTenantIdAndStoreIdAndDate(@RequestParam Long tenantId,
			@RequestParam Long storeId, @RequestParam String date) {
		try {
			List<StaffBreakTime> sbt = staffBreakTimeSvc.getStaffBreakTimeByTenantIdAndStoreIdAndDate(tenantId, storeId,
					date);
			return ResponseHandler.generateResponse("Ok", null, HttpStatus.OK, sbt);
		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);
			return ResponseHandler.generateResponse(e.getClass().getName(), e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR, null);
		}
	}

}