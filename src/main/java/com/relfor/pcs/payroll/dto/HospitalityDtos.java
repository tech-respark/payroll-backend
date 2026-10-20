package com.relfor.pcs.payroll.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class HospitalityDtos {

    private HospitalityDtos() {
    }

    @Data
    public static class AdvanceRequest {
        private Long staffId;
        private String advanceType;
        private BigDecimal principal;
        private BigDecimal installmentAmount;
        private Integer remainingInstallments;
        private String startMonth;
        private Integer startYear;
        private LocalDate issuedOn;
        private String remarks;
    }

    @Data
    public static class SlabRequest {
        private BigDecimal thresholdFrom;
        private BigDecimal thresholdTo;
        private BigDecimal ratePercent;
    }

    @Data
    public static class CommissionPlanRequest {
        private Long staffId;
        private String planName;
        private String category;
        private Boolean active;
        private List<SlabRequest> slabs = new ArrayList<>();
    }

    @Data
    public static class SalesRequest {
        private Long staffId;
        private LocalDate saleDate;
        private String category;
        private BigDecimal amount;
        private String source;
        private String externalRef;
        private String jobCode;
        private String notes;
    }

    @Data
    public static class SalesImportRequest {
        private List<SalesRequest> rows = new ArrayList<>();
    }

    @Data
    public static class TipPoolRequest {
        private LocalDate periodFrom;
        private LocalDate periodTo;
        private BigDecimal poolAmount;
        private String method;
        private String remarks;
        private List<Map<String, Object>> points = new ArrayList<>();
    }

    @Data
    public static class StatutoryRequest {
        private Boolean pfEnabled;
        private Boolean esiEnabled;
        private Boolean ptEnabled;
        private Boolean tdsEnabled;
        private Boolean lwfEnabled;
        private BigDecimal pfWageCeiling;
        private BigDecimal esiWageCeiling;
        private BigDecimal pfEmployeeRate;
        private BigDecimal pfEmployerRate;
        private BigDecimal esiEmployeeRate;
        private BigDecimal esiEmployerRate;
        private BigDecimal ptAmount;
        private BigDecimal lwfEmployeeAmount;
        private BigDecimal lwfEmployerAmount;
        private BigDecimal tdsRate;
    }

    @Data
    public static class DailySalesRequest {
        private LocalDate saleDate;
        private BigDecimal netSales;
        private Integer covers;
        private String notes;
    }

    @Data
    public static class PeriodRequest {
        private String month;
        private Integer year;
        private LocalDate fromDate;
        private LocalDate toDate;
        private Long staffId;
    }

    @Data
    public static class FnfRequest {
        private Long staffId;
        private LocalDate lastWorkingDate;
        private BigDecimal leaveEncashment;
        private BigDecimal otherDeductions;
        private String remarks;
    }

    @Data
    public static class SwapRequest {
        private Long targetStaffId;
        private LocalDate shiftDate;
        private String reason;
    }

    @Data
    public static class SwapDecisionRequest {
        private boolean approved;
    }

    @Data
    public static class AvailabilityRequest {
        private String weekday;
        private String availableFrom;
        private String availableTo;
        private Boolean available;
    }

    @Data
    public static class ClockRequest {
        private String punchEvent;
        private Double latitude;
        private Double longitude;
        private String note;
    }
}
