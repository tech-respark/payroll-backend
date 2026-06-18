package com.relfor.pcs.payroll.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Instant;

public class ResparkIndividualPunchesDTO {
    private LocalDate punchDate;
    private LocalTime punchTime;
    private Long personnelAttendanceId;
    private String currentStatus;
    private String punchEvent;
    private String uploadSource;
    private Long createdBy;
    private Instant createdTimestamp;
    private Long modifiedBy;
    private Instant modifiedTimestamp;

    public LocalDate getPunchDate() { return punchDate; }
    public void setPunchDate(LocalDate punchDate) { this.punchDate = punchDate; }

    public LocalTime getPunchTime() { return punchTime; }
    public void setPunchTime(LocalTime punchTime) { this.punchTime = punchTime; }

    public Long getPersonnelAttendanceId() { return personnelAttendanceId; }
    public void setPersonnelAttendanceId(Long personnelAttendanceId) { this.personnelAttendanceId = personnelAttendanceId; }

    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }

    public String getPunchEvent() { return punchEvent; }
    public void setPunchEvent(String punchEvent) { this.punchEvent = punchEvent; }

    public String getUploadSource() { return uploadSource; }
    public void setUploadSource(String uploadSource) { this.uploadSource = uploadSource; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public Instant getCreatedTimestamp() { return createdTimestamp; }
    public void setCreatedTimestamp(Instant createdTimestamp) { this.createdTimestamp = createdTimestamp; }

    public Long getModifiedBy() { return modifiedBy; }
    public void setModifiedBy(Long modifiedBy) { this.modifiedBy = modifiedBy; }

    public Instant getModifiedTimestamp() { return modifiedTimestamp; }
    public void setModifiedTimestamp(Instant modifiedTimestamp) { this.modifiedTimestamp = modifiedTimestamp; }
}
