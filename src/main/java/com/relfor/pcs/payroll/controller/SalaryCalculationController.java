package com.relfor.pcs.payroll.controller;

import com.relfor.pcs.payroll.dto.ResponseModel;
import com.relfor.pcs.payroll.dto.PersonnelDetailsRequestModel;
import com.relfor.pcs.payroll.dto.SalaryComponentResponseDTO;
import com.relfor.pcs.payroll.entity.PersonnelPayslipHistory;
import com.relfor.pcs.payroll.entity.PersonnelSalaryComponents;
import com.relfor.pcs.payroll.exceptions.ResourceNotFoundException;
import com.relfor.pcs.payroll.service.PayslipExcelGenerationService;
import com.relfor.pcs.payroll.service.SalaryCalculation;
import com.relfor.pcs.payroll.service.VelocityService;
import com.relfor.pcs.payroll.util.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/payroll-management/v1")
public class SalaryCalculationController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    SalaryCalculation salaryCalService;
    @Autowired
    VelocityService velocityService;
    @Autowired
    PayslipExcelGenerationService payslipExcelGenerationService;

    @PostMapping("/personnelSalaryComponentsCalculation")
    public ResponseEntity<?> personnelSalaryComponentsCalculation(@RequestBody SalaryComponentResponseDTO personnelComponents){
        ResponseModel responseModel = salaryCalService.processSalaryComponents(personnelComponents);
        return ResponseHandler.generateResponseModel(responseModel);
    }

    @GetMapping("/personnelSalaryCalculation")
    public ResponseEntity<?> personnelSalaryCalculation(@RequestParam Long personnelCode, @RequestParam String  month, @RequestParam Long  year, @RequestParam Long tenantId, @RequestParam Long storeId){
        ResponseModel responseModel = salaryCalService.processSalary(personnelCode, month, year, tenantId, storeId);
        return ResponseHandler.generateResponseModel(responseModel);
    }

    @PostMapping("/monthlyCalculationComponents")
    public ResponseEntity<?> monthlyCalculationComponents(@RequestBody SalaryComponentResponseDTO salaryComponent){
        ResponseModel responseModel = salaryCalService.setMonthlyComponents(salaryComponent);
        return ResponseHandler.generateResponseModel(responseModel);
    }

    @GetMapping("/getPayslipData")
    public ResponseEntity<?> getPayslipData(@RequestParam Long personnelCode, @RequestParam String  month, @RequestParam Integer year){
        ResponseModel responseModel = salaryCalService.getPayslipData(personnelCode, month, year);
        return ResponseHandler.generateResponseModel(responseModel);
    }

    @GetMapping("/salaryComponentDefinitions")
    public ResponseEntity<?> getSalaryComponentDefinitions(@RequestParam Long tenantId, @RequestParam Long  storeId){
        ResponseModel responseModel = salaryCalService.getSalaryComponentDefinitions(tenantId, storeId);
        return ResponseHandler.generateResponseModel(responseModel);
    }

    @PostMapping("/printStaffPayslip")
    public ResponseEntity<?> printBillForStaffPayslip(@RequestParam Long personnelId, @RequestParam String month, @RequestParam Integer year) {
        try {
            String result  = velocityService.postPersonnelPayslipData(personnelId, month, year);
            return ResponseHandler.generateResponse("Ok", null, HttpStatus.OK, result);
        } catch (Exception e) {
//            logger.error("An error occurred while processing the request", e);
            return ResponseHandler.generateResponse("ServerError", "Internal server error",
                    HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

    @GetMapping("/personnelSalaryComponents")
    public ResponseEntity<?> getPersonnelSalaryComponents(@RequestParam Long personnelCode){
        ResponseModel responseModel = salaryCalService.getPersonnelSalaryComponents(personnelCode);
        return ResponseHandler.generateResponseModel(responseModel);
    }

    @GetMapping("/personnelSalaryComponentsForMonth")
    public ResponseEntity<?> getPersonnelSalaryComponentsForMonth(@RequestParam Long personnelCode, @RequestParam String month, @RequestParam Integer year){
        ResponseModel responseModel = salaryCalService.getPersonnelSalaryComponentsForMonth(personnelCode, month, year);
        return ResponseHandler.generateResponseModel(responseModel);
    }

    @PostMapping("/downloadBillPdf")
    public ResponseEntity<?> downloadBillPdf(@RequestParam Long personnelId, @RequestParam String month, @RequestParam Integer year) {

        byte[] pdfBytes = velocityService.getPdfBytes(personnelId, month, year);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = "generated-document.pdf";
        headers.setContentDispositionFormData(filename, filename); // Tells the browser to download
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        // Step 5: Stream the byte array to the browser

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/excelOfPayslip")
    public ResponseEntity<?> generateExcelOfPayslip(@RequestParam Long tenantId,
                                                    Long storeId,
                                                    String month,
                                                    Integer year) {
        try {
            byte[] excelData = payslipExcelGenerationService.generateExcelOfPayslip(tenantId, storeId, month, year);
            HttpHeaders responseHeaders = new HttpHeaders();
            String fileName = String.format("payslip-data-%s-%d.xlsx", month, year);
            responseHeaders.add("content-disposition", "attachment; filename=" + fileName);
            return new ResponseEntity<>(excelData, responseHeaders, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return ResponseHandler.generateResponse("NotFound", e.getMessage(), HttpStatus.OK, null);
        } catch (Exception e) {
            logger.error("Exception inside generateExcelOfPayslip", e);
            return ResponseHandler.generateResponse("ServerError", "Internal server error while generating Excel file.",
                    HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }
}