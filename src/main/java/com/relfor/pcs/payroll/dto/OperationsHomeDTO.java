package com.relfor.pcs.payroll.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationsHomeDTO {
    private Queues queues;
    private StaffHomeDTO.Pay pay;
    private DashboardMetricsDTO snapshot;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Queues {
        private Long pendingLeave;
        private Long pendingRegularization;
        private Long onLeaveToday;
    }
}
