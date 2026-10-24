package com.relfor.pcs.payroll.security;

/**
 * Global application permissions (authorities).
 * These strings must exactly match the strings stored in the role 'permissionList' in the database.
 */
public final class Permissions {
    
    private Permissions() {
        // Restrict instantiation
    }

    public static final String MANAGE_ROLES = "MANAGE_ROLES";
    public static final String MANAGE_STORE_SETTINGS = "MANAGE_STORE_SETTINGS"; // Store settings
    
    // Aligned with Frontend routing
    public static final String MANAGE_LEAVES = "MANAGE_LEAVES";
    public static final String MANAGE_SALARY = "MANAGE_SALARY";
    public static final String VIEW_SALARY = "VIEW_SALARY";
    public static final String MANAGE_SHIFTS = "MANAGE_SHIFTS";
    public static final String MANAGE_ATTENDANCE = "MANAGE_ATTENDANCE";
    public static final String MANAGE_STAFF = "MANAGE_STAFF";
    public static final String VIEW_STAFF = "VIEW_STAFF";
    public static final String VIEW_ATTENDANCE = "VIEW_ATTENDANCE";
    public static final String VIEW_ROLES = "VIEW_ROLES";

    public static final String VIEW_SHIFTS = "VIEW_SHIFTS";
    public static final String VIEW_REPORTS = "VIEW_REPORTS";
    public static final String VIEW_HR_DASHBOARD = "VIEW_HR_DASHBOARD";
    public static final String VIEW_OTHER_STAFF = "VIEW_OTHER_STAFF";
    public static final String VIEW_HOSPITALITY = "VIEW_HOSPITALITY";
    public static final String MANAGE_HOSPITALITY = "MANAGE_HOSPITALITY";
    public static final String VIEW_ACTIVITY_LOG = "VIEW_ACTIVITY_LOG";

    public static final String SUPERADMIN = "SUPERADMIN";
    public static final String SALES = "SALES";

}
