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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
			billDetails = new PrintBillDetailsDTO();
			if (configOpt.isPresent()) {
				StoreProfileConfig config = configOpt.get();
				billDetails.setLogoPath(config.getLogoPath());
				billDetails.setAddress(config.getAddress());
				billDetails.setCompanyName(config.getCompanyName());
			}
			if (billDetails.getCompanyName() == null || billDetails.getCompanyName().isBlank()) {
				billDetails.setCompanyName(paySlipData.getStoreName());
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

		context.put("personnelName", blankToDash(dto.getPersonnelName()));
		context.put("employeeCode", blankToDash(dto.getEmployeeCode()));
		context.put("storeName", blankToDash(dto.getStoreName()));
		context.put("designation", blankToDash(dto.getDesignation()));
		context.put("bankName", blankToDash(dto.getBankName()));
		context.put("IfscCode", dto.getIfscCode() == null ? "" : dto.getIfscCode());
		context.put("panNo", dto.getPanNo() == null ? "" : dto.getPanNo());
		context.put("uanNumber", dto.getUanNumber() == null ? "" : dto.getUanNumber());
		context.put("maskedAccount", maskAccount(dto.getAccountNumber()));
		context.put("hasBank", notBlank(dto.getBankName()) || notBlank(dto.getAccountNumber()));
		context.put("hasIfsc", notBlank(dto.getIfscCode()));
		context.put("hasPan", notBlank(dto.getPanNo()));
		context.put("hasUan", notBlank(dto.getUanNumber()));

		context.put("totalDays", days(dto.getTotalDays()));
		context.put("totalWorkingDays", days(dto.getTotalWorkingDays()));
		context.put("absentDays", days(dto.getAbsentDays()));
		context.put("penaltyAbsentDays", days(dto.getPenaltyAbsentDays()));
		context.put("totalHolidays", days(dto.getTotalHolidays()));
		context.put("totalweeklyOff", days(dto.getTotalweeklyOff()));
		context.put("totalPaidLeaves", days(dto.getTotalPaidLeaves()));
		context.put("totalPaidDays", days(dto.getTotalPaidDays()));

		List<String> earningNames = new ArrayList<>();
		List<String> earningAmounts = new ArrayList<>();
		if (dto.getEarnings() != null) {
			dto.getEarnings().forEach((name, amount) -> {
				earningNames.add(name);
				earningAmounts.add(money(amount));
			});
		}
		List<String> deductionNames = new ArrayList<>();
		List<String> deductionAmounts = new ArrayList<>();
		if (dto.getDeductions() != null) {
			dto.getDeductions().forEach((name, amount) -> {
				deductionNames.add(name);
				deductionAmounts.add(money(amount));
			});
		}
		int earningSize = earningNames.size();
		int deductionSize = deductionAmounts.size();
		int maxRows = Math.max(earningSize, deductionSize);
		context.put("earningNames", earningNames);
		context.put("earningAmounts", earningAmounts);
		context.put("deductionNames", deductionNames);
		context.put("deductionAmounts", deductionAmounts);
		context.put("earningSize", earningSize);
		context.put("deductionSize", deductionSize);
		context.put("maxRows", maxRows);
		context.put("loopEnd", Math.max(maxRows - 1, 0));

		context.put("totalEarning", money(dto.getTotalEarning()));
		context.put("totalDeduction", money(dto.getTotalDeduction()));
		context.put("salaryAmount", money(dto.getSalaryAmount()));
		context.put("salaryAmountInWords", amountInWords(dto.getSalaryAmount()));
		context.put("monthTitle", monthTitle(dto.getSalaryMonth(), dto.getSalaryYear()));
		context.put("periodLabel", prettyPeriod(dto.getSalaryPeriod()));

		String logo = billDetails.getLogoPath();
		String address = billDetails.getAddress();
		String companyName = billDetails.getCompanyName();
		context.put("logo", logo == null ? "" : logo);
		context.put("address", address == null ? "" : address);
		context.put("companyName", companyName == null || companyName.isBlank()
				? blankToDash(dto.getStoreName())
				: companyName);

		return context;
	}

	private static String money(BigDecimal value) {
		BigDecimal amount = value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
		DecimalFormat format = new DecimalFormat("#,##,##0.00", new DecimalFormatSymbols(new Locale("en", "IN")));
		return format.format(amount);
	}

	private static String days(Number value) {
		if (value == null) {
			return "0";
		}
		double raw = value.doubleValue();
		if (Math.abs(raw - Math.rint(raw)) < 0.0001) {
			return String.valueOf((int) Math.rint(raw));
		}
		return new DecimalFormat("0.##").format(raw);
	}

	private static String monthTitle(String month, Integer year) {
		String label = month == null ? "" : month.trim();
		if (!label.isEmpty()) {
			label = label.substring(0, 1).toUpperCase(Locale.ENGLISH) + label.substring(1).toLowerCase(Locale.ENGLISH);
		}
		return (label + " " + (year == null ? "" : year)).trim();
	}

	private static String prettyPeriod(String raw) {
		if (raw == null || raw.isBlank()) {
			return "";
		}
		String[] parts = raw.split("(?i)\\s+to\\s+");
		DateTimeFormatter out = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);
		try {
			if (parts.length == 2) {
				return LocalDate.parse(parts[0].trim()).format(out) + " – " + LocalDate.parse(parts[1].trim()).format(out);
			}
		} catch (Exception ignored) {
			return raw;
		}
		return raw;
	}

	private static String maskAccount(String value) {
		if (value == null) {
			return "";
		}
		String digits = value.replaceAll("\\s+", "");
		if (digits.length() <= 4) {
			return digits;
		}
		int hidden = Math.min(digits.length() - 4, 8);
		return "•".repeat(hidden) + digits.substring(digits.length() - 4);
	}

	private static String amountInWords(BigDecimal value) {
		BigDecimal amount = value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
		int rupees = amount.setScale(0, RoundingMode.DOWN).intValue();
		int paise = amount.remainder(BigDecimal.ONE).movePointRight(2).setScale(0, RoundingMode.HALF_UP).intValue();
		String words = convert(rupees) + " Rupees";
		if (paise > 0) {
			words += " and " + convert(paise) + " Paise";
		}
		return words + " Only";
	}

	private static boolean notBlank(String value) {
		return value != null && !value.isBlank();
	}

	private static String blankToDash(String value) {
		return notBlank(value) ? value : "—";
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