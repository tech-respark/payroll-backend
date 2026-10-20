package com.relfor.pcs.payroll.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffHomeDTO {
    private String firstName;
    private Today today;
    private Leave leave;
    private Pay pay;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Today {
        private String date;
        private boolean clockedIn;
        private String lastInAt;
        private Shift shift;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Shift {
        private String slot;
        private Boolean weeklyOff;
        private Boolean onLeave;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Leave {
        private long pendingCount;
        private List<Balance> balances;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Balance {
        private Long id;
        private String type;
        private BigDecimal available;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pay {
        private Integer lastCalculatedMonthNumber;
        private String lastCalculatedMonth;
    }
}
