package com.relfor.pcs.payroll.service;

import com.relfor.pcs.payroll.dto.PrintBillDetailsDTO;
import com.lowagie.text.DocumentException;
import com.relfor.pcs.payroll.dto.SalaryComponentsDTO;
import com.relfor.pcs.payroll.entity.PersonnelPayslipHistory;
import com.relfor.pcs.payroll.entity.StoreProfileConfig;
import com.relfor.pcs.payroll.repository.PersonnelPayslipHistoryRepository;
import com.relfor.pcs.payroll.repository.StoreProfileConfigRepository;
//import org.apache.velocity.runtime.RuntimeConstants;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
//import java.math.BigDecimal;
import java.util.*;


@Service
public class VelocityService {

	@Autowired
	private PersonnelPayslipHistoryRepository personnelPayslipHistoryRepository;
	
	@Autowired
	private StoreProfileConfigRepository storeProfileConfigRepository;
	
	@Autowired
	SalaryCalculation salaryCalculation;
	
	@Autowired
	VelocityEngine velocity;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public String postPersonnelPayslipData(Long staffId, String salaryMonth, Integer salaryYear) {
		String result = null;

		PrintBillDetailsDTO billDetails = null;

		try {


			//this template has to be tenant-wise and path should be given accordingly, since every tenant might have different payslip format
			Template template = velocity.getTemplate("/templates/paySlip.vm");

			Optional<PersonnelPayslipHistory> personnelPayslipHistory = personnelPayslipHistoryRepository.findByStaffIdAndSalaryMonthAndSalaryYear(staffId, salaryMonth, salaryYear);

			PersonnelPayslipHistory paySlipData = personnelPayslipHistory.get();
			SalaryComponentsDTO salarySlip = salaryCalculation.convertToSalaryComponentDTO(personnelPayslipHistory.get());

			Optional<StoreProfileConfig> configOpt = storeProfileConfigRepository.findByTenantIdAndStoreId(paySlipData.getTenantId(), paySlipData.getStoreId());
			if (configOpt.isPresent()) {
				StoreProfileConfig config = configOpt.get();
				billDetails = new PrintBillDetailsDTO();
				billDetails.setLogoPath(config.getLogoPath());
				billDetails.setAddress(config.getAddress());
			} else {
				billDetails = new PrintBillDetailsDTO();
			}

			VelocityContext context = buildPayslipContext(salarySlip, billDetails);

			StringWriter writer = new StringWriter();
			template.merge(context, writer);
			result = writer.toString();

		} catch (Exception e) {
			logger.error("Exception inside postPersonnelPayslipData: {}", e.getMessage());
			throw e;
		}
		return result;
	}


	public static VelocityContext buildPayslipContext(SalaryComponentsDTO dto, PrintBillDetailsDTO billDetails) {
		VelocityContext context = new VelocityContext();

		context.put("tenantId", dto.getTenantId());
		context.put("storeId", dto.getStoreId());
		context.put("staffId", dto.getStaffId());
		context.put("personnelName", dto.getPersonnelName());
		context.put("salaryDate", dto.getSalaryDate());
		context.put("employeeCode", dto.getEmployeeCode());
		context.put("storeName", dto.getStoreName());
		context.put("designation", dto.getDesignation());
		context.put("salaryPeriod", dto.getSalaryPeriod());
		context.put("salaryMonth", dto.getSalaryMonth());
		context.put("salaryYear", dto.getSalaryYear());

		context.put("bankName", dto.getBankName());
		context.put("accountNumber", dto.getAccountNumber());
		context.put("IfscCode", dto.getIfscCode());
		context.put("panNo", dto.getPanNo());
		context.put("uanNumber", dto.getUanNumber());

		context.put("totalDays", dto.getTotalDays());
		context.put("totalWorkingDays", dto.getTotalWorkingDays());
		context.put("absentDays", dto.getAbsentDays());
		context.put("penaltyAbsentDays", dto.getPenaltyAbsentDays());
		context.put("totalHolidays", dto.getTotalHolidays());
		context.put("totalweeklyOff", dto.getTotalweeklyOff());
		context.put("totalPaidLeaves", dto.getTotalPaidLeaves());
		context.put("totalPaidDays", dto.getTotalPaidDays());

		context.put("earnings", dto.getEarnings());
		context.put("deductions", dto.getDeductions());
		context.put("totalEarning", dto.getTotalEarning());
		context.put("totalDeduction", dto.getTotalDeduction());
		context.put("salaryAmount", dto.getSalaryAmount());
		context.put("salaryAmountInWords", convert(dto.getSalaryAmount().intValue()));


		context.put("logo", billDetails.getLogoPath());
		context.put("address", billDetails.getAddress());

		String billHeader = "Payslip for the Month of " + dto.getSalaryMonth() + " " + dto.getSalaryYear();
		context.put("billInvoice", billHeader);

		return context;
	}

	public static String htmlToXhtml(String html) {
		org.jsoup.nodes.Document document = Jsoup.parse(html);
		document.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
		return document.html();
	}

	public static byte[] xhtmlToPdf(String xhtml) throws DocumentException {
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			ITextRenderer iTextRenderer = new ITextRenderer();
			iTextRenderer.setDocumentFromString(xhtml);
			iTextRenderer.layout();
			iTextRenderer.createPDF(os);
			return os.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("Error during PDF generation in memory", e);
		}
	}

	public byte[] getPdfBytes(Long staffId, String month, Integer year) {

		String result  = this.postPersonnelPayslipData(staffId, month, year);
		String xhtmlContent = this.htmlToXhtml(result);
		try {
			return this.xhtmlToPdf(xhtmlContent);
		} catch (DocumentException e) {
			logger.error("Exception in PDF generation: {}", e.getMessage());
			throw new RuntimeException(e);
		}
	}

	private static final String[] units = {
			"", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
			"Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
			"Seventeen", "Eighteen", "Nineteen"
	};

	private static final String[] tens = {
			"", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
	};

	public static String convert(int number) {
		if (number == 0) return "Zero";
		if (number < 0) return "Minus " + convert(-number);

		return convertHelper(number).trim();
	}

	private static String convertHelper(int number) {
		if (number < 20) return units[number];
		if (number < 100)
			return tens[number / 10] + (number % 10 != 0 ? " " + units[number % 10] : "");
		if (number < 1000)
			return units[number / 100] + " Hundred" + (number % 100 != 0 ? " " + convertHelper(number % 100) : "");
		if (number < 1000000)
			return convertHelper(number / 1000) + " Thousand" + (number % 1000 != 0 ? " " + convertHelper(number % 1000) : "");
		if (number < 1000000000)
			return convertHelper(number / 1000000) + " Million" + (number % 1000000 != 0 ? " " + convertHelper(number % 1000000) : "");
		return convertHelper(number / 1000000000) + " Billion" + (number % 1000000000 != 0 ? " " + convertHelper(number % 1000000000) : "");
	}
}