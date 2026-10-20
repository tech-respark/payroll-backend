package com.relfor.pcs.payroll.activity;

/**
 * Top-level product area for filtering. Add a value here when a new product
 * module starts emitting events — do not invent ad-hoc module strings.
 */
public enum ActivityModule {
    AUTH("Sign-in"),
    STAFF("Staff"),
    ROLES("Roles"),
    SHIFTS("Shifts"),
    ATTENDANCE("Attendance"),
    LEAVE("Leave"),
    PAYROLL("Payroll"),
    HOSPITALITY("Hospitality"),
    SETTINGS("Settings"),
    SYSTEM("System");

    private final String label;

    ActivityModule(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
