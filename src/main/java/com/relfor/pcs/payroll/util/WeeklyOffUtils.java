package com.relfor.pcs.payroll.util;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Locale;

/**
 * Staff weekly-off is stored as a comma-separated list of day names
 * (e.g. "sunday,monday"). Compare each token to {@link java.time.DayOfWeek}.
 */
public final class WeeklyOffUtils {

    private WeeklyOffUtils() {}

    public static boolean isWeeklyOff(String weeklyOffCsv, LocalDate date) {
        if (weeklyOffCsv == null || weeklyOffCsv.isBlank() || date == null) {
            return false;
        }
        String dayName = date.getDayOfWeek().name();
        return Arrays.stream(weeklyOffCsv.split(","))
                .map(String::trim)
                .filter(token -> !token.isEmpty())
                .anyMatch(token -> token.equalsIgnoreCase(dayName)
                        || token.toUpperCase(Locale.ROOT).equals(dayName));
    }
}
