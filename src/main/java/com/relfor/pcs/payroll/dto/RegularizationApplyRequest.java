package com.relfor.pcs.payroll.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class RegularizationApplyRequest {
    @NotNull(message = "Staff ID is required")
    private Long staffId;
    
    @NotNull(message = "Date is required")
    private LocalDate date;
    
    private LocalTime inTime;
    private LocalTime outTime;
    
    private String reason;
}
