package com.relfor.pcs.payroll.model;

import java.time.LocalDate;
import java.util.List;

public class ResparkPersonnelAttendanceDTO {
    private Long tenantId;
    private Long storeId;
    private String applicationName;
    private Long personnelCode;
    private String personnelName;
    private String personnelGender;
    private String personnelDesignation;
    private String personnelMobileNumber;
    private LocalDate fromDate;
    private LocalDate toDate;
    private List<ResparkDayWiseAttendanceDTO> dayWiseAttendanceList;

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }

    public String getApplicationName() { return applicationName; }
    public void setApplicationName(String applicationName) { this.applicationName = applicationName; }

    public Long getPersonnelCode() { return personnelCode; }
    public void setPersonnelCode(Long personnelCode) { this.personnelCode = personnelCode; }

    public String getPersonnelName() { return personnelName; }
    public void setPersonnelName(String personnelName) { this.personnelName = personnelName; }

    public String getPersonnelGender() { return personnelGender; }
    public void setPersonnelGender(String personnelGender) { this.personnelGender = personnelGender; }

    public String getPersonnelDesignation() { return personnelDesignation; }
    public void setPersonnelDesignation(String personnelDesignation) { this.personnelDesignation = personnelDesignation; }

    public String getPersonnelMobileNumber() { return personnelMobileNumber; }
    public void setPersonnelMobileNumber(String personnelMobileNumber) { this.personnelMobileNumber = personnelMobileNumber; }

    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }

    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }

    public List<ResparkDayWiseAttendanceDTO> getDayWiseAttendanceList() { return dayWiseAttendanceList; }
    public void setDayWiseAttendanceList(List<ResparkDayWiseAttendanceDTO> dayWiseAttendanceList) { this.dayWiseAttendanceList = dayWiseAttendanceList; }
}
