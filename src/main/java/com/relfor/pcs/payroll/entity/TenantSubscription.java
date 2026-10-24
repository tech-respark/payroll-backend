package com.relfor.pcs.payroll.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "tenant_subscription")
public class TenantSubscription {

    public enum Status {
        DRAFT,
        TRIAL,
        ACTIVE,
        SUSPENDED,
        EXPIRED
    }

    public enum PlanCode {
        STANDARD,
        HOSPITALITY
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, unique = true)
    private Long tenantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_code", nullable = false, length = 20)
    private PlanCode planCode = PlanCode.STANDARD;

    @Column(name = "starts_on")
    private LocalDate startsOn;

    @Column(name = "ends_on")
    private LocalDate endsOn;

    @Column(name = "max_stores")
    private Integer maxStores;

    @Column(name = "max_staff")
    private Integer maxStaff;

    @Column(name = "module_salary")
    private Boolean moduleSalary = Boolean.TRUE;

    @Column(name = "module_leave")
    private Boolean moduleLeave = Boolean.TRUE;

    @Column(name = "module_shifts")
    private Boolean moduleShifts = Boolean.TRUE;

    @Column(name = "module_reports")
    private Boolean moduleReports = Boolean.TRUE;

    @Column(name = "module_hospitality")
    private Boolean moduleHospitality = Boolean.FALSE;

    @Column(name = "module_biometric")
    private Boolean moduleBiometric = Boolean.FALSE;

    @Column(name = "module_staff_sync")
    private Boolean moduleStaffSync = Boolean.FALSE;

    @Column(name = "sales_owner_id")
    private Long salesOwnerId;

    @Column(length = 500)
    private String notes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public PlanCode getPlanCode() {
        return planCode;
    }

    public void setPlanCode(PlanCode planCode) {
        this.planCode = planCode;
    }

    public LocalDate getStartsOn() {
        return startsOn;
    }

    public void setStartsOn(LocalDate startsOn) {
        this.startsOn = startsOn;
    }

    public LocalDate getEndsOn() {
        return endsOn;
    }

    public void setEndsOn(LocalDate endsOn) {
        this.endsOn = endsOn;
    }

    public Integer getMaxStores() {
        return maxStores;
    }

    public void setMaxStores(Integer maxStores) {
        this.maxStores = maxStores;
    }

    public Integer getMaxStaff() {
        return maxStaff;
    }

    public void setMaxStaff(Integer maxStaff) {
        this.maxStaff = maxStaff;
    }

    public Boolean getModuleSalary() {
        return moduleSalary;
    }

    public void setModuleSalary(Boolean moduleSalary) {
        this.moduleSalary = moduleSalary;
    }

    public Boolean getModuleLeave() {
        return moduleLeave;
    }

    public void setModuleLeave(Boolean moduleLeave) {
        this.moduleLeave = moduleLeave;
    }

    public Boolean getModuleShifts() {
        return moduleShifts;
    }

    public void setModuleShifts(Boolean moduleShifts) {
        this.moduleShifts = moduleShifts;
    }

    public Boolean getModuleReports() {
        return moduleReports;
    }

    public void setModuleReports(Boolean moduleReports) {
        this.moduleReports = moduleReports;
    }

    public Boolean getModuleHospitality() {
        return moduleHospitality;
    }

    public void setModuleHospitality(Boolean moduleHospitality) {
        this.moduleHospitality = moduleHospitality;
    }

    public Boolean getModuleBiometric() {
        return moduleBiometric;
    }

    public void setModuleBiometric(Boolean moduleBiometric) {
        this.moduleBiometric = moduleBiometric;
    }

    public Boolean getModuleStaffSync() {
        return moduleStaffSync;
    }

    public void setModuleStaffSync(Boolean moduleStaffSync) {
        this.moduleStaffSync = moduleStaffSync;
    }

    public Long getSalesOwnerId() {
        return salesOwnerId;
    }

    public void setSalesOwnerId(Long salesOwnerId) {
        this.salesOwnerId = salesOwnerId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean allowsClientLogin() {
        if (status != Status.TRIAL && status != Status.ACTIVE) {
            return false;
        }
        LocalDate today = LocalDate.now();
        if (startsOn != null && today.isBefore(startsOn)) {
            return false;
        }
        return endsOn == null || !today.isAfter(endsOn);
    }
}
