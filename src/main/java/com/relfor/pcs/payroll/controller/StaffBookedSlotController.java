package com.relfor.pcs.payroll.controller;


import com.relfor.pcs.payroll.dto.ShiftSlotDTO;
import com.relfor.pcs.payroll.repository.StaffBookedRepository;
import com.relfor.pcs.payroll.service.StaffShiftsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll-management/v1")
public class StaffBookedSlotController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private StaffShiftsService staffShiftService;
    @Autowired
	StaffBookedRepository staffBooked;

	@PostMapping("/staff/slot")
	public ResponseEntity<?> postStaffBookedSlot(@RequestBody List<ShiftSlotDTO> slots) throws Exception {
		try {
			if (!ObjectUtils.isEmpty(slots)) {
				staffShiftService.createOrUpdateSlot(slots);
			} else {
				return ResponseEntity.badRequest().body("Payload is incorrect");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}
		return ResponseEntity.ok().body(slots);
	}

	@PutMapping("/staff/cancelSlot/{appointmentid}")
	public ResponseEntity<?> cancelStaffSlot(@PathVariable(value = "appointmentid") String appointmentId)
			throws Exception {
		try {
			if (!ObjectUtils.isEmpty(appointmentId)) {
				staffBooked.cancelStaffSlot(appointmentId);
			} else {
				return ResponseEntity.badRequest().body("Invalid appointmentId");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}
		return ResponseEntity.ok().body("done");
	}

}