package com.relfor.pcs.payroll.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SalaryComponentResponseDTO {
    private Long tenantId;
    private Long storeId;
    @JsonAlias({"personnelCode", "personnelId"})
	private Long personnelId;
    private List<SalaryComponentDTO> earningsList;
    private List<SalaryComponentDTO> deductionsList;
    private String salaryMonth;
    private Integer salaryYear;
    private BigDecimal totalEarning;
    private BigDecimal totalDeduction;
    private BigDecimal salaryAmount;

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public Long getPersonnelId() {
        return personnelId;
    }

    public void setPersonnelId(Long personnelId) {
        this.personnelId = personnelId;
    }

    public List<SalaryComponentDTO> getEarningsList() {
        return earningsList;
    }

    public void setEarningsList(List<SalaryComponentDTO> earningsList) {
        this.earningsList = earningsList;
    }

    public List<SalaryComponentDTO> getDeductionsList() {
        return deductionsList;
    }

    public void setDeductionsList(List<SalaryComponentDTO> deductionsList) {
        this.deductionsList = deductionsList;
    }

    public String getSalaryMonth() {
        return salaryMonth;
    }

    public void setSalaryMonth(String salaryMonth) {
        this.salaryMonth = salaryMonth;
    }

    public Integer getSalaryYear() {
        return salaryYear;
    }

    public void setSalaryYear(Integer salaryYear) {
        this.salaryYear = salaryYear;
    }

    public BigDecimal getTotalEarning() {
        return totalEarning;
    }

    public void setTotalEarning(BigDecimal totalEarning) {
        this.totalEarning = totalEarning;
    }

    public BigDecimal getTotalDeduction() {
        return totalDeduction;
    }

    public void setTotalDeduction(BigDecimal totalDeduction) {
        this.totalDeduction = totalDeduction;
    }

    public BigDecimal getSalaryAmount() {
        return salaryAmount;
    }

    public void setSalaryAmount(BigDecimal salaryAmount) {
        this.salaryAmount = salaryAmount;
    }
}
