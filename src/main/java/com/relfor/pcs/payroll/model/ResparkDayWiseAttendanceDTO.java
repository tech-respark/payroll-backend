package com.relfor.pcs.payroll.model;

import java.time.LocalDate;
import java.util.List;

public class ResparkDayWiseAttendanceDTO {
    private LocalDate dateOfAttendance;
    private String dayOfAttendance;
    private String currentStatus;
    
    // Exact Shift Data for this day
    private String shiftStartTime;
    private String shiftEndTime;
    
    // Pre-calculated metrics
    private String firstInTime;
    private String lastOutTime;
    private Integer totalDurationMinutes;
    private Integer lateInMinutes;
    private Integer earlyOutMinutes;
    
    private List<ResparkIndividualPunchesDTO> individualPunchesList;

    public LocalDate getDateOfAttendance() { return dateOfAttendance; }
    public void setDateOfAttendance(LocalDate dateOfAttendance) { this.dateOfAttendance = dateOfAttendance; }

    public String getDayOfAttendance() { return dayOfAttendance; }
    public void setDayOfAttendance(String dayOfAttendance) { this.dayOfAttendance = dayOfAttendance; }

    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }

    public String getShiftStartTime() { return shiftStartTime; }
    public void setShiftStartTime(String shiftStartTime) { this.shiftStartTime = shiftStartTime; }

    public String getShiftEndTime() { return shiftEndTime; }
    public void setShiftEndTime(String shiftEndTime) { this.shiftEndTime = shiftEndTime; }

    public String getFirstInTime() { return firstInTime; }
    public void setFirstInTime(String firstInTime) { this.firstInTime = firstInTime; }

    public String getLastOutTime() { return lastOutTime; }
    public void setLastOutTime(String lastOutTime) { this.lastOutTime = lastOutTime; }

    public Integer getTotalDurationMinutes() { return totalDurationMinutes; }
    public void setTotalDurationMinutes(Integer totalDurationMinutes) { this.totalDurationMinutes = totalDurationMinutes; }

    public Integer getLateInMinutes() { return lateInMinutes; }
    public void setLateInMinutes(Integer lateInMinutes) { this.lateInMinutes = lateInMinutes; }

    public Integer getEarlyOutMinutes() { return earlyOutMinutes; }
    public void setEarlyOutMinutes(Integer earlyOutMinutes) { this.earlyOutMinutes = earlyOutMinutes; }

    public List<ResparkIndividualPunchesDTO> getIndividualPunchesList() { return individualPunchesList; }
    public void setIndividualPunchesList(List<ResparkIndividualPunchesDTO> individualPunchesList) { this.individualPunchesList = individualPunchesList; }
}
