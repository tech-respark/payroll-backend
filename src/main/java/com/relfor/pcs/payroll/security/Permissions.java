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
    public static final String MANAGE_SHIFTS = "MANAGE_SHIFTS";
    public static final String MANAGE_ATTENDANCE = "MANAGE_ATTENDANCE";
    public static final String MANAGE_STAFF = "MANAGE_STAFF";

}
