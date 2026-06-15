package com.relfor.pcs.payroll.service;

import com.relfor.pcs.payroll.dto.SalaryComponentDTO;
import com.relfor.pcs.payroll.entity.PersonnelPayslipHistory;
import com.relfor.pcs.payroll.exceptions.ResourceNotFoundException;
import com.relfor.pcs.payroll.repository.PersonnelPayslipHistoryRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PayslipExcelGenerationService {
	@Autowired
	private PersonnelPayslipHistoryRepository personnelPayslipHistoryRepository;

	public byte[] generateExcelOfPayslip(Long tenantId,
										 Long storeId,
										 String salaryMonth,
										 Integer salaryYear) throws IOException {
		List<PersonnelPayslipHistory> payslipList =
				personnelPayslipHistoryRepository.findByTenantIdAndStoreIdAndSalaryMonthAndSalaryYear(tenantId, storeId, salaryMonth, salaryYear);
		if (payslipList.isEmpty()) {
			throw new ResourceNotFoundException("No Data Found");
		}
		try (XSSFWorkbook workbook = new XSSFWorkbook();
			 ByteArrayOutputStream out = new ByteArrayOutputStream()) {

			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
			Sheet sheet = workbook.createSheet("Salary Details");

			// ----- 1. Defining Headers for 5 panes -----
			// Pane 1: defining headers (static)
			List<String> employeeHeaders = List.of(
					"EMPLOYEE CODE",
					"EMPLOYEE NAME",
					"DESIGNATION",
					"LOCATION",
					"SALARY MONTH",
					"SALARY YEAR",
					"SALARY PERIOD",
					"SALARY DATE",
					"UAN NUMBER",
					"PAN NO",
					"BANK NAME",
					"ACCOUNT NUMBER",
					"IFSC CODE");
			// Pane 2: defining headers (static)
			List<String> attendanceHeaders = List.of(
					"TOTAL DAYS IN MONTH",
					"TOTAL WORKING DAYS",
					"TOTAL PAID DAYS",
					"ABSENT DAYS",
					"PENALTY ABSENT DAYS",
					"TOTAL HOLIDAYS",
					"TOTAL WEEKLY OFF",
					"TOTAL PAID LEAVES");
			Set<String> dynamicEarningHeaders = new LinkedHashSet<>();
			Set<String> dynamicDeductionHeaders = new LinkedHashSet<>();
			// Pane 3 & 4: defining headers (dynamic)
			payslipList.forEach(payslip -> {
				if (payslip.getEarnings() != null) payslip.getEarnings().forEach(e -> dynamicEarningHeaders.add(e.getComponentName().toUpperCase()));
				if (payslip.getDeductions() != null) payslip.getDeductions().forEach(d -> dynamicDeductionHeaders.add(d.getComponentName().toUpperCase()));
			});
			// Pane 5: defining headers (static)
			List<String> finalSalaryHeaders = List.of(
					"TOTAL EARNINGS",
					"TOTAL DEDUCTIONS",
					"NET SALARY");

			// ----- 2. Creating cell styles for super-header, sub-header and normal data cells -----
			CellStyle superHeaderStyle = this.createSuperHeaderStyle(workbook);
			CellStyle subHeaderStyle = this.createSubHeaderStyle(workbook);
			CellStyle dataCellStyle = this.createDataCellStyle(workbook);

			// ----- 3. Creating row 0: super-headers (Pane titles) ------
			Row superHeaderRow = sheet.createRow(0);
			int currentCell = 0;

			// merging super-header cells to form 5 different panes
			currentCell = this.createMergedHeader(sheet, superHeaderRow, "EMPLOYEE DETAILS", currentCell, employeeHeaders.size(), superHeaderStyle);
			currentCell = this.createMergedHeader(sheet, superHeaderRow, "ATTENDANCE DETAILS", currentCell, attendanceHeaders.size(), superHeaderStyle);
			currentCell = this.createMergedHeader(sheet, superHeaderRow, "EARNINGS", currentCell, dynamicEarningHeaders.size(), superHeaderStyle);
			currentCell = this.createMergedHeader(sheet, superHeaderRow, "DEDUCTIONS", currentCell, dynamicDeductionHeaders.size(), superHeaderStyle);
			this.createMergedHeader(sheet, superHeaderRow, "TOTAL SALARY DETAILS", currentCell, finalSalaryHeaders.size(), superHeaderStyle);

			// ----- 4. Creating row 1: sub-headers (individual column headers) -----
			Row subHeaderRow = sheet.createRow(1);
			List<String> allSubHeaders = new ArrayList<>();
			allSubHeaders.addAll(employeeHeaders);
			allSubHeaders.addAll(attendanceHeaders);
			allSubHeaders.addAll(dynamicEarningHeaders);
			allSubHeaders.addAll(dynamicDeductionHeaders);
			allSubHeaders.addAll(finalSalaryHeaders);

			for (int i = 0; i < allSubHeaders.size(); i++) {
				Cell cell = subHeaderRow.createCell(i);
				cell.setCellValue(allSubHeaders.get(i));
				cell.setCellStyle(subHeaderStyle);
			}

			// ----- 5. Populating the data in rows from third row -----
			int rowIdx = 2; // data starts from the third row
			for (PersonnelPayslipHistory payslip : payslipList) {
				Row row = sheet.createRow(rowIdx++);
				int cellIdx = 0;

				// Pane 1: EMPLOYEE DETAILS
				this.createCell(row, cellIdx++, payslip.getEmployeeCode(), dataCellStyle);
				this.createCell(row, cellIdx++, payslip.getPersonnelName(), dataCellStyle);
				this.createCell(row, cellIdx++, payslip.getDesignation(), dataCellStyle);
				this.createCell(row, cellIdx++, payslip.getStoreName(), dataCellStyle);
				this.createCell(row, cellIdx++, payslip.getSalaryMonth(), dataCellStyle);
				this.createCell(row, cellIdx++, payslip.getSalaryYear(), dataCellStyle);
				this.createCell(row, cellIdx++, payslip.getSalaryPeriod(), dataCellStyle);
				this.createCell(row, cellIdx++, (payslip.getSalaryDate() != null ? payslip.getSalaryDate().format(dateTimeFormatter) : ""), dataCellStyle);
				this.createCell(row, cellIdx++, payslip.getUanNumber(), dataCellStyle);
				this.createCell(row, cellIdx++, payslip.getPanNo(), dataCellStyle);
				this.createCell(row, cellIdx++, payslip.getBankName(), dataCellStyle);
				this.createCell(row, cellIdx++, payslip.getAccountNumber(), dataCellStyle);
				this.createCell(row, cellIdx++, payslip.getIfscCode(), dataCellStyle);

				// Pane 2: ATTENDANCE DETAILS
				this.createCell(row, cellIdx++, payslip.getTotalDays(), dataCellStyle);
				this.createCell(row, cellIdx++, payslip.getTotalWorkingDays(), dataCellStyle);
				this.createCell(row, cellIdx++, payslip.getTotalPaidDays(), dataCellStyle);
				this.createCell(row, cellIdx++, payslip.getAbsentDays(), dataCellStyle);
				this.createCell(row, cellIdx++, payslip.getPenaltyAbsentDays(), dataCellStyle);
				this.createCell(row, cellIdx++, payslip.getTotalHolidays(), dataCellStyle);
				this.createCell(row, cellIdx++, payslip.getTotalweeklyOff(), dataCellStyle);
				this.createCell(row, cellIdx++, payslip.getTotalPaidLeaves(), dataCellStyle);

				// Pane 3 & 4: EARNINGS & DEDUCTIONS
				Map<String, BigDecimal> earningsMap = (payslip.getEarnings() != null)
						? payslip.getEarnings().stream().collect(Collectors.toMap(e -> e.getComponentName().toUpperCase(), SalaryComponentDTO::getMonthlyValue))
						: Collections.emptyMap();
				for (String earningHeader : dynamicEarningHeaders) {
					createCell(row, cellIdx++, earningsMap.getOrDefault(earningHeader, BigDecimal.ZERO).doubleValue(), dataCellStyle);
				}
				Map<String, BigDecimal> deductionsMap = (payslip.getDeductions() != null)
						? payslip.getDeductions().stream().collect(Collectors.toMap(d -> d.getComponentName().toUpperCase(), SalaryComponentDTO::getMonthlyValue))
						: Collections.emptyMap();
				for (String deductionHeader : dynamicDeductionHeaders) {
					createCell(row, cellIdx++, deductionsMap.getOrDefault(deductionHeader, BigDecimal.ZERO).doubleValue(), dataCellStyle);
				}

				// Pane 5: TOTAL SALARY DETAILS
				this.createCell(row, cellIdx++, payslip.getTotalEarning(), dataCellStyle);
				this.createCell(row, cellIdx++, payslip.getTotalDeduction(), dataCellStyle);
				this.createCell(row, cellIdx++, payslip.getSalaryAmount(), dataCellStyle);
			}

			// ----- 6. Auto sizing columns for better readability ------
			for(int i = 0; i < allSubHeaders.size(); i++) {
				sheet.autoSizeColumn(i);
			}
			workbook.write(out);
			return out.toByteArray();
		} catch (Exception e) {
			throw e;
		}
	}

	private CellStyle createSuperHeaderStyle(Workbook workbook) {
		CellStyle style = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setBold(true);
		font.setFontHeightInPoints((short) 12);
		style.setFont(font);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setBorderTop(BorderStyle.THICK);
		style.setBorderBottom(BorderStyle.THICK);
		style.setBorderLeft(BorderStyle.THICK);
		style.setBorderRight(BorderStyle.THICK);
		return style;
	}

	private CellStyle createSubHeaderStyle(Workbook workbook) {
		CellStyle style = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setBold(true);
		style.setFont(font);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderBottom(BorderStyle.MEDIUM);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		return style;
	}

	private CellStyle createDataCellStyle(Workbook workbook) {
		CellStyle style = workbook.createCellStyle();
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		return style;
	}

	private int createMergedHeader(Sheet sheet, Row row, String title, int startCol, int width, CellStyle style) {
		if (width == 0) {
			return startCol; // no header for empty section
		} else {
			CellRangeAddress mergedRegion = new CellRangeAddress(0, 0, startCol, startCol + width - 1);
			sheet.addMergedRegion(mergedRegion);
			// applying style to all cells in the merged region for borders
			for (int i = startCol; i < startCol + width; i++) {
				row.createCell(i).setCellStyle(style);
			}
			row.getCell(startCol).setCellValue(title);
			return startCol + width;
		}
	}

	private void createCell(Row row, int column, Object value, CellStyle style) {
		Cell cell = row.createCell(column);
		if (value instanceof String) cell.setCellValue((String) value);
		else if (value instanceof Integer) cell.setCellValue((Integer) value);
		else if (value instanceof Double) cell.setCellValue((Double) value);
		else if (value instanceof Float) cell.setCellValue(((Float) value).doubleValue());
		else if (value instanceof BigDecimal) cell.setCellValue(((BigDecimal) value).doubleValue());
		else if (value == null) cell.setCellValue("");
		cell.setCellStyle(style);
	}
}