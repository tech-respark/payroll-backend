package com.relfor.pcs.payroll.controller;

import com.relfor.pcs.payroll.dto.ResponseModel;
import com.relfor.pcs.payroll.dto.StoreHolidayRequest;
import com.relfor.pcs.payroll.dto.StoreHolidayResponse;
import com.relfor.pcs.payroll.service.StoreHolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll-management/v1/holidays")
public class StoreHolidayController {

    @Autowired
    private StoreHolidayService storeHolidayService;

    @PostMapping
    public ResponseModel createHoliday(@RequestBody StoreHolidayRequest request) {
        ResponseModel responseModel = new ResponseModel();
        try {
            StoreHolidayResponse holiday = storeHolidayService.createHoliday(request);
            responseModel.setCode(HttpStatus.CREATED);
            responseModel.setMessage("SUCCESS");
            responseModel.setData(holiday);
        } catch (Exception e) {
            responseModel.setCode(HttpStatus.BAD_REQUEST);
            responseModel.setMessage(e.getMessage());
        }
        return responseModel;
    }

    @GetMapping
    public ResponseModel getHolidays(
            @RequestParam("tenantId") Long tenantId,
            @RequestParam("storeId") Long storeId) {
        ResponseModel responseModel = new ResponseModel();
        try {
            List<StoreHolidayResponse> holidays = storeHolidayService.getHolidaysByStore(tenantId, storeId);
            responseModel.setCode(HttpStatus.OK);
            responseModel.setMessage("SUCCESS");
            responseModel.setData(holidays);
        } catch (Exception e) {
            responseModel.setCode(HttpStatus.INTERNAL_SERVER_ERROR);
            responseModel.setMessage("Error retrieving holidays.");
        }
        return responseModel;
    }

    @DeleteMapping("/{id}")
    public ResponseModel deleteHoliday(@PathVariable Long id) {
        ResponseModel responseModel = new ResponseModel();
        try {
            storeHolidayService.deleteHoliday(id);
            responseModel.setCode(HttpStatus.OK);
            responseModel.setMessage("SUCCESS");
        } catch (Exception e) {
            responseModel.setCode(HttpStatus.BAD_REQUEST);
            responseModel.setMessage(e.getMessage());
        }
        return responseModel;
    }
}
