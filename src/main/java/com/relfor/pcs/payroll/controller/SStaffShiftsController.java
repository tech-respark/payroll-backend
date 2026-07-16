package com.relfor.pcs.payroll.controller;

import com.relfor.pcs.payroll.dto.*;
import com.relfor.pcs.payroll.entity.SShiftsSlots;
import com.relfor.pcs.payroll.entity.SStaffShifts;
import com.relfor.pcs.payroll.repository.SShiftsSlotsRepository;
import com.relfor.pcs.payroll.repository.SStaffShiftsRepository;
import com.relfor.pcs.payroll.service.StaffShiftsService;
import com.relfor.pcs.payroll.util.ResponseHandler;
import com.relfor.pcs.payroll.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SStaffShiftsController class is a RestController that provides endpoints for
 * managing staff shifts and shift slots. It uses Spring dependency injection to
 * inject the required dependencies.
 */
@RestController
@CrossOrigin
@RequestMapping("/payroll-management/v1")
@PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SHIFTS)")
public class SStaffShiftsController {
	/**
	 * SStaffShiftsRepository is used to interact with the database and retrieve
	 * staff shift data.
	 */
    private final SStaffShiftsRepository stSfRepo;
	/**
	 * SShiftsSlotsRepository is used to interact with the database and retrieve
	 * shift slot data.
	 */
    private final SShiftsSlotsRepository shStRepo;
	/**
	 * StaffShiftsService is used to create staff shifts based on the input data.
	 */
	private final StaffShiftsService staffShiftsService;
	/**
	 * Logger is used for logging purposes.
	 */
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public SStaffShiftsController(SStaffShiftsRepository stSfRepo, SShiftsSlotsRepository shStRepo, StaffShiftsService staffShiftsService) {
        this.stSfRepo = stSfRepo;
        this.shStRepo = shStRepo;
        this.staffShiftsService = staffShiftsService;
    }

    /**
	 * createStaffShifts endpoint is used to create staff shifts based on the input
	 * data.
	 *
	 * @param createStaffShiftInput input data for creating staff shifts
	 * @return ResponseEntity with the created staff shifts or an error message
	 */
	@PostMapping("/staffshifts")
	public ResponseEntity<?> createStaffShifts(@RequestBody CreateStaffShiftInput createStaffShiftInput) {
		try {
            createStaffShiftInput.setTenantId(SecurityUtils.getTenantId(createStaffShiftInput.getTenantId()));
            createStaffShiftInput.setStoreId(SecurityUtils.getStoreId(createStaffShiftInput.getStoreId()));

			List<SStaffShifts> result = staffShiftsService.createStaffShiftNewInput(createStaffShiftInput);
			return ResponseHandler.generateResponse("OK", null, HttpStatus.OK, result);

		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);

			return ResponseHandler.generateResponse(e.getCause().toString(), e.getMessage(), HttpStatus.BAD_REQUEST,
					null);
		}
	}

    /**
     * Retrieves staff shifts based on tenant, store, and optionally date and staff ID.
     * - If staffId and date are provided, it fetches shifts for that specific staff on that date.
     * - If only date is provided, it fetches all shifts for the store on that date.
     * - If neither is provided, it fetches all shifts for the tenant and store.
     *
     * @param tenantId The ID of the tenant.
     * @param storeId  The ID of the store.
     * @param date     The specific date to filter shifts for (optional, format: yyyy-MM-dd).
     * @param staffId  The ID of the staff to filter shifts for (optional).
     * @return A ResponseEntity containing a list of SStaffShifts or an error response.
     */
    @GetMapping("/staffshifts")
    public ResponseEntity<?> getStaffShiftsByStoreId(@RequestParam long tenantId, @RequestParam long storeId,
                                                     @RequestParam(required = false) String date,
                                                     @RequestParam(required = false) Long staffId) {
        tenantId = SecurityUtils.getTenantId(tenantId);
        storeId = SecurityUtils.getStoreId(storeId);
        List<SStaffShifts> staffShifts;
        try {
            if (staffId != null && date != null)
                staffShifts = stSfRepo.findByTenantIdAndStoreIdAndShiftDateAndStaffId(tenantId, storeId,
                        LocalDate.parse(date), staffId);
            else if (date != null) staffShifts = stSfRepo.findByTenantIdAndStoreIdAndShiftDate(tenantId, storeId,
                    LocalDate.parse(date));
            else
                staffShifts = stSfRepo.findByTenantIdAndStoreId(tenantId, storeId);
            return ResponseEntity.ok().body(staffShifts);
        } catch (Exception e) {
            logger.error(e.getClass().getName(), e);
            return ResponseHandler.generateResponse("Internal Server error please contact to admin.", e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

	@GetMapping("/staffshifts/store/weekly")
	public ResponseEntity<?> getStoreWeeklyShifts(
			@RequestParam long tenantId,
			@RequestParam long storeId,
			@RequestParam String startDate) {
        tenantId = SecurityUtils.getTenantId(tenantId);
        storeId = SecurityUtils.getStoreId(storeId);
		try {
			LocalDate start = LocalDate.parse(startDate);
			LocalDate end = start.plusDays(6);
			List<SStaffShifts> staffShifts = stSfRepo.findStoreShiftsForWeek(
					tenantId, storeId, start, end);
			return ResponseEntity.ok().body(staffShifts);
		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);
			return ResponseHandler.generateResponse("Internal Server error please contact to admin.",e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR,null);
		}
	}

	@GetMapping("/staffshifts/staff")
	public ResponseEntity<?> getStaffShiftsForNext7Days(@RequestParam long tenantId, @RequestParam long storeId,
														  @RequestParam long staffId, @RequestParam String startDate) {
        tenantId = SecurityUtils.getTenantId(tenantId);
        storeId = SecurityUtils.getStoreId(storeId);
		List<SStaffShifts> staffShifts;
		try {
			LocalDate fromDate = LocalDate.parse(startDate);
			LocalDate toDate = fromDate.plusDays(6);
			staffShifts = stSfRepo.findByTenantIdAndStoreIdAndStaffIdAndShiftDateBetween(
					tenantId, storeId, staffId, fromDate, toDate);
			return ResponseEntity.ok().body(staffShifts);
		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);
			return ResponseHandler.generateResponse("Internal Server error please contact to admin.", e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR, null);
		}
	}

	/**
	 * createShiftSlots endpoint is used to create shift slots.
	 *
	 * @param obj shift slot object
	 * @return ResponseEntity with the created shift slot or an error message
	 */
	@PostMapping("/shiftslots")
	public ResponseEntity<?> createShiftSlots(@RequestBody SShiftsSlots obj) {
		try {
            obj.setTenantId(SecurityUtils.getTenantId(obj.getTenantId()));
            obj.setStoreId(SecurityUtils.getStoreId(obj.getStoreId()));
			SShiftsSlots staffShift = shStRepo.save(obj);
			return ResponseEntity.ok().body(staffShift);
		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);
			return ResponseHandler.generateResponse(e.getClass().getName(), e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR, null);
		}

	}

	/**
	 * getShiftSlots endpoint is used to retrieve all shift slots.
	 *
	 * @return ResponseEntity with the shift slots or an error message
	 */
	@GetMapping("/shiftslots/all")
	public ResponseEntity<?> getShiftSlots() {
		try {
			List<SShiftsSlots> staffShifs = shStRepo.findAll();
			return ResponseEntity.ok().body(staffShifs);
		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);
			return ResponseHandler.generateResponse(e.getClass().getName(), e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR, null);
		}

	}

	/**
	 * getShiftSlotsByStoreId endpoint is used to retrieve shift slots for a
	 * specific store.
	 *
	 * @param tenantId ID of the tenant
	 * @param storeId  ID of the store
	 * @return ResponseEntity with the shift slots for the store or an error message
	 */
	@GetMapping("/shiftslots")
	public ResponseEntity<?> getShiftSlotsByStoreId(@RequestParam long tenantId, @RequestParam long storeId) {
        tenantId = SecurityUtils.getTenantId(tenantId);
        storeId = SecurityUtils.getStoreId(storeId);
		try {
			List<SShiftsSlots> shiftSlots = shStRepo.findByTenantIdAndStoreId(tenantId, storeId);
			return ResponseEntity.ok().body(shiftSlots);
		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);
			return ResponseHandler.generateResponse(e.getClass().getName(), e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR, null);
		}
	}

	// INTERNAL API
	@PostMapping("/staffProductivity")
	public ResponseEntity<?> postStaffProductivity(@RequestParam Long tenantId, @RequestParam Long storeId,
			@RequestParam String shiftDate, @RequestBody List<Map<String, Object>> result) {
        tenantId = SecurityUtils.getTenantId(tenantId);
        storeId = SecurityUtils.getStoreId(storeId);
		try {
			staffShiftsService.postStaffProductivity(tenantId, storeId, shiftDate, result);
			return ResponseHandler.generateResponse("Ok", null, HttpStatus.OK, null);
		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);
			return ResponseHandler.generateResponse(e.getClass().getName(), e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR, null);
		}
	}

	// INTERNAL API
	@PostMapping("/staffProductivityForUpdatedOrder")
	public ResponseEntity<?> postStaffProductivityForUpdatedOrder(@RequestParam Long tenantId,
			@RequestParam Long storeId, @RequestParam String shiftDate, @RequestBody List<Map<String, Object>> result) {
        tenantId = SecurityUtils.getTenantId(tenantId);
        storeId = SecurityUtils.getStoreId(storeId);
		try {
			staffShiftsService.postStaffProductivityForUpdatedOrder(tenantId, storeId, shiftDate, result);
			return ResponseHandler.generateResponse("Ok", null, HttpStatus.OK, null);
		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);
			return ResponseHandler.generateResponse(e.getClass().getName(), e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR, null);
		}
	}

	// UTILITY
	@PostMapping("/utility/stylistProductivity")
	public ResponseEntity<?> updateStylistProductivity(@RequestBody List<Object[]> list) {
		try {
			staffShiftsService.updateStylistProductivity(list);
			return ResponseHandler.generateResponse("Success", null, HttpStatus.OK, null);
		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);
			return ResponseHandler.generateResponse("", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);
		}
	}

	// UTILITY
	@PostMapping("/staffBookedshifts/{startday}")
	public ResponseEntity<?> createStaffBookedShifts(@PathVariable(value = "startday") String startDate) {
		try {

			String result = staffShiftsService.createStaffBookedShift(startDate);
			return ResponseHandler.generateResponse("OK", null, HttpStatus.OK, result);
		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
		}
	}

	@GetMapping("/availableStaffSlots")
	public ResponseEntity<?> fetchAvailableStaffSlots(@RequestParam String startDate, @RequestParam long staffId,
			@RequestParam long tenantId, @RequestParam long storeId, @RequestParam String timeZone) {
        tenantId = SecurityUtils.getTenantId(tenantId);
        storeId = SecurityUtils.getStoreId(storeId);
		try {

			List<Map<String, Object>> result = staffShiftsService.getAvailableStaffSlots(startDate, staffId, tenantId,
					storeId, timeZone);
			return ResponseHandler.generateResponse("OK", null, HttpStatus.OK, result);

		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
		}
	}

	@GetMapping("/allStaffsSlots")
	public ResponseEntity<?> fetchAllStaffsSlots(@RequestParam String startDate, @RequestParam long tenantId,
			@RequestParam long storeId, @RequestParam String timeZone) {
        tenantId = SecurityUtils.getTenantId(tenantId);
        storeId = SecurityUtils.getStoreId(storeId);
		try {

			List<StaffShiftDTO> result = staffShiftsService.getAllStaffsSlots(startDate, tenantId, storeId, timeZone);
			return ResponseHandler.generateResponse("OK", null, HttpStatus.OK, result);

		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
		}
	}

	// INTERNAL API
	@GetMapping("/staffShifts/stylistRevenue")
	public ResponseEntity<?> getStaffShiftsForStylistRevenueReport(@RequestParam String fromDate,
			@RequestParam String toDate, @RequestParam long storeId, @RequestParam long tenantId) {
        tenantId = SecurityUtils.getTenantId(tenantId);
        storeId = SecurityUtils.getStoreId(storeId);
		List<Object[]> staffShifts = new ArrayList<>();
		try {
			staffShifts = staffShiftsService.getStaffShiftsForStylistRevenueReport(
					new SimpleDateFormat("yyyy-MM-dd").parse(fromDate),
					new SimpleDateFormat("yyyy-MM-dd").parse(toDate), storeId, tenantId);
		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);
		}
		return ResponseEntity.ok().body(staffShifts);

	}

	@PostMapping("/staffs/weeklyOff")
	public ResponseEntity<?> updateWeeklyOff(@RequestBody Map<String, Object> staff) {
		ResponseModel responseModel = staffShiftsService.updateWeeklyOff(staff);
		return ResponseHandler.generateResponseModel(responseModel);
	}

	@PostMapping("/staff/report")
    public ResponseEntity<?> fetchStaffAttendanceByTenantIdAndStoreIdAndShiftDateInBetween(
            @RequestBody ReportRequestDTO reportRequestDTO) {
        try {
            reportRequestDTO.setTenantId(SecurityUtils.getTenantId(reportRequestDTO.getTenantId()));
            reportRequestDTO.setStoreId(SecurityUtils.getStoreId(reportRequestDTO.getStoreId()));
            List<Map<String, Object>> attendance = staffShiftsService.getAttendenceByTenantIdStoreIdInBetween(reportRequestDTO.getTenantId(),
                    reportRequestDTO.getStoreId(), reportRequestDTO.getFromDateStr(), reportRequestDTO.getToDateStr());
            return ResponseHandler.generateResponse("Ok", "", HttpStatus.OK, attendance);
        } catch (Exception e) {
            logger.error(e.getClass().getName(), e);
            return ResponseHandler.generateResponse("Internal Server error please contact to admin.", e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

	@PostMapping("/staff/report/summary")
    public ResponseEntity<?> fetchStaffAttendanceSummaryByTenantIdAndStoreIdAndShiftDateInBetween(
            @RequestBody ReportRequestDTO reportRequestDTO) {
        try {
            reportRequestDTO.setTenantId(SecurityUtils.getTenantId(reportRequestDTO.getTenantId()));
            reportRequestDTO.setStoreId(SecurityUtils.getStoreId(reportRequestDTO.getStoreId()));
            List<Map<String, Object>> attendanceSummary = staffShiftsService.getAttendanceSummaryByTenantIdStoreIdInBetween(reportRequestDTO.getTenantId(),
                    reportRequestDTO.getStoreId(), reportRequestDTO.getFromDateStr(), reportRequestDTO.getToDateStr());
            return ResponseHandler.generateResponse("Ok", "", HttpStatus.OK, attendanceSummary);
        } catch (Exception e) {
            logger.error(e.getClass().getName(), e);
            return ResponseHandler.generateResponse("Internal Server error please contact to admin.", e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

	@PostMapping("/staff/report/detailed")
    public ResponseEntity<?> fetchDetailedStaffAttendanceByStaffId(
            @RequestBody ReportRequestDTO reportRequestDTO) {
        try {
            if (reportRequestDTO.getStaffId() == null) {
                return ResponseHandler.generateResponse("Bad Request", "staffId is required", HttpStatus.BAD_REQUEST, null);
            }
            reportRequestDTO.setTenantId(SecurityUtils.getTenantId(reportRequestDTO.getTenantId()));
            reportRequestDTO.setStoreId(SecurityUtils.getStoreId(reportRequestDTO.getStoreId()));
            
            List<AttendanceDetailDto> detailedAttendance = staffShiftsService.getDetailedAttendenceByStaffIdStoreIdInBetween(
                    reportRequestDTO.getStaffId(), reportRequestDTO.getTenantId(), reportRequestDTO.getStoreId(),
                    reportRequestDTO.getFromDateStr(), reportRequestDTO.getToDateStr());
                    
            return ResponseHandler.generateResponse("Ok", "", HttpStatus.OK, detailedAttendance);
        } catch (Exception e) {
            logger.error(e.getClass().getName(), e);
            return ResponseHandler.generateResponse("Internal Server error please contact to admin.", e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

	@GetMapping("/availableStaffIds")
	public ResponseEntity<?> fetchAvailableStaffIds(@RequestParam String startDate,
													  @RequestParam long tenantId, @RequestParam long storeId) {
        tenantId = SecurityUtils.getTenantId(tenantId);
        storeId = SecurityUtils.getStoreId(storeId);
		try {

			List<Long> result = staffShiftsService.getAvailableStaffIds(tenantId,
					storeId, startDate);
			return ResponseHandler.generateResponse("OK", null, HttpStatus.OK, result);

		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
		}
	}

	@PostMapping("/staff/report/query")
	public ResponseEntity<?> fetchStaffAttendenceDynamic(@RequestBody AttendanceQueryRequest request) {
		try {
			Object data = staffShiftsService.getAttendenceByTenantIdStoreIdInBetweenDynamic(request);
			return ResponseHandler.generateResponse("Ok", "", HttpStatus.OK, data);
		} catch (IllegalArgumentException e) {
			logger.warn("Bad attendance query: {}", e.getMessage());
			return ResponseHandler.generateResponse(e.getMessage(), "", HttpStatus.BAD_REQUEST, null);
		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);
			return ResponseHandler.generateResponse("Internal Server error please contact to admin.", e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR, null);
		}
	}
}