package com.relfor.pcs.payroll.activity;

import com.relfor.pcs.payroll.entity.PersonnelDetails;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class ActivityNames {

    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);

    private ActivityNames() {
    }

    public static String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DISPLAY_DATE);
    }

    public static String countLabel(int count, String singular, String plural) {
        return count + " " + (count == 1 ? singular : plural);
    }

    public static String displayName(PersonnelDetails person) {
        if (person == null) {
            return null;
        }
        String first = person.getFirstName() != null ? person.getFirstName().trim() : "";
        String last = person.getLastName() != null ? person.getLastName().trim() : "";
        String name = (first + " " + last).trim();
        if (!name.isEmpty()) {
            return name;
        }
        if (person.getUsername() != null && !person.getUsername().isBlank()) {
            return person.getUsername();
        }
        return person.getEmployeeCode();
    }
}
