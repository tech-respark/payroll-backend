package com.relfor.pcs.payroll.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SalaryComponentDTO {
    private Long salaryComponentDefinitionsId;
    private BigDecimal annualValue;
    private BigDecimal monthlyValue;
    private String componentName;
    private String componentType;
    private Boolean isCalculatedMonthly;

    public Long getSalaryComponentDefinitionsId() {
        return salaryComponentDefinitionsId;
    }

    public void setSalaryComponentDefinitionsId(Long salaryComponentDefinitionsId) {
        this.salaryComponentDefinitionsId = salaryComponentDefinitionsId;
    }

    public BigDecimal getAnnualValue() {
        return annualValue;
    }

    public void setAnnualValue(BigDecimal annualValue) {
        this.annualValue = annualValue;
    }

    public BigDecimal getMonthlyValue() {
        return monthlyValue;
    }

    public void setMonthlyValue(BigDecimal monthlyValue) {
        this.monthlyValue = monthlyValue;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public Boolean getCalculatedMonthly() {
        return isCalculatedMonthly;
    }

    public void setCalculatedMonthly(Boolean calculatedMonthly) {
        isCalculatedMonthly = calculatedMonthly;
    }
}

