package com.relfor.pcs.payroll.service.payroll;

import java.time.LocalDate;
import java.time.Month;
import java.util.Locale;

public final class PayrollMonths {

    private PayrollMonths() {
    }

    public static String monthName(String month) {
        if (month == null || month.isBlank()) {
            return LocalDate.now().getMonth().name();
        }
        return Month.valueOf(month.trim().toUpperCase(Locale.ROOT)).name();
    }

    public static String financialYear(String month, Integer year) {
        Month resolved = Month.valueOf(monthName(month));
        int y = year != null ? year : LocalDate.now().getYear();
        int start = resolved.getValue() >= 4 ? y : y - 1;
        return start + "-" + (start + 1);
    }
}
