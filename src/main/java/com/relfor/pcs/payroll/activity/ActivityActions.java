package com.relfor.pcs.payroll.activity;

import java.util.List;

/**
 * Namespaced action catalog: {@code module.verb}.
 * Add a constant and a {@link #catalog()} entry when a feature starts logging.
 * Do not reuse or rename existing values — they are the long-term query key.
 */
public final class ActivityActions {

    public static final String AUTH_LOGIN_SUCCESS = "auth.login.success";
    public static final String AUTH_LOGIN_FAILURE = "auth.login.failure";
    public static final String AUTH_PASSWORD_RESET = "auth.password.reset";
    public static final String AUTH_PASSWORD_UPDATED = "auth.password.updated";
    public static final String AUTH_SUPPORT_SESSION = "auth.support_session";

    public static final String STAFF_CREATED = "staff.created";
    public static final String STAFF_UPDATED = "staff.updated";

    public static final String ROLE_CREATED = "role.created";
    public static final String ROLE_UPDATED = "role.updated";
    public static final String ROLE_ASSIGNED = "role.assigned";

    public static final String SHIFT_ROSTER_UPDATED = "shift.roster.updated";
    public static final String SHIFT_SLOT_SAVED = "shift.slot.saved";
    public static final String SHIFT_WEEKLY_OFF_UPDATED = "shift.weekly_off.updated";

    public static final String ATTENDANCE_REGULARIZATION_REQUESTED = "attendance.regularization.requested";
    public static final String ATTENDANCE_REGULARIZATION_APPROVED = "attendance.regularization.approved";
    public static final String ATTENDANCE_REGULARIZATION_REJECTED = "attendance.regularization.rejected";
    public static final String ATTENDANCE_RETRIEVED = "attendance.retrieved";
    public static final String ATTENDANCE_MONTH_CALCULATED = "attendance.month.calculated";
    public static final String ATTENDANCE_CLOCKED = "attendance.clocked";

    public static final String LEAVE_APPLIED = "leave.applied";
    public static final String LEAVE_APPROVED = "leave.approved";
    public static final String LEAVE_REJECTED = "leave.rejected";
    public static final String LEAVE_CANCEL_REQUESTED = "leave.cancel.requested";
    public static final String LEAVE_CANCELLED = "leave.cancelled";
    public static final String LEAVE_CANCEL_REJECTED = "leave.cancel.rejected";
    public static final String LEAVE_TYPE_SAVED = "leave.type.saved";
    public static final String LEAVE_PLAN_SAVED = "leave.plan.saved";
    public static final String LEAVE_PLAN_ASSIGNED = "leave.plan.assigned";
    public static final String LEAVE_RULE_SAVED = "leave.rule.saved";
    public static final String LEAVE_RULE_DELETED = "leave.rule.deleted";
    public static final String LEAVE_BALANCE_ADJUSTED = "leave.balance.adjusted";
    public static final String LEAVE_HOLIDAY_CREATED = "leave.holiday.created";
    public static final String LEAVE_HOLIDAY_DELETED = "leave.holiday.deleted";
    public static final String LEAVE_OVERRIDE_SAVED = "leave.override.saved";

    public static final String PAYROLL_CALCULATED = "payroll.calculated";
    public static final String PAYROLL_APPLIED = "payroll.hospitality.applied";
    public static final String PAYROLL_COMPONENTS_SAVED = "payroll.components.saved";
    public static final String PAYROLL_RUN_CALCULATED = "payroll.run.calculated";
    public static final String PAYROLL_RUN_SUBMITTED = "payroll.run.submitted";
    public static final String PAYROLL_RUN_APPROVED = "payroll.run.approved";
    public static final String PAYROLL_RUN_PUBLISHED = "payroll.run.published";
    public static final String PAYROLL_RUN_LOCKED = "payroll.run.locked";
    public static final String PAYROLL_STATUTORY_SAVED = "payroll.statutory.saved";
    public static final String PAYROLL_CLAIM_CREATED = "payroll.claim.created";
    public static final String PAYROLL_CLAIM_APPROVED = "payroll.claim.approved";
    public static final String PAYROLL_CLAIM_REJECTED = "payroll.claim.rejected";
    public static final String PAYROLL_DECLARATION_SAVED = "payroll.declaration.saved";
    public static final String PAYROLL_DECLARATION_APPROVED = "payroll.declaration.approved";
    public static final String PAYROLL_DECLARATION_REJECTED = "payroll.declaration.rejected";
    public static final String PAYROLL_LOAN_CREATED = "payroll.loan.created";
    public static final String PAYROLL_LOAN_CLOSED = "payroll.loan.closed";
    public static final String PAYROLL_REVISION_SAVED = "payroll.revision.saved";
    public static final String PAYROLL_FNF_CREATED = "payroll.fnf.created";
    public static final String PAYROLL_FNF_SETTLED = "payroll.fnf.settled";
    public static final String PAYROLL_LOGO_SAVED = "payroll.logo.saved";

    public static final String HOSPITALITY_ADVANCE_CREATED = "hospitality.advance.created";
    public static final String HOSPITALITY_ADVANCE_CLOSED = "hospitality.advance.closed";
    public static final String HOSPITALITY_COMMISSION_SAVED = "hospitality.commission.saved";
    public static final String HOSPITALITY_COMMISSION_DELETED = "hospitality.commission.deleted";
    public static final String HOSPITALITY_SALE_RECORDED = "hospitality.sale.recorded";
    public static final String HOSPITALITY_SALES_IMPORTED = "hospitality.sales.imported";
    public static final String HOSPITALITY_TIP_POOL_CREATED = "hospitality.tip_pool.created";
    public static final String HOSPITALITY_TIP_POOL_DISTRIBUTED = "hospitality.tip_pool.distributed";
    public static final String HOSPITALITY_STATUTORY_SAVED = "hospitality.statutory.saved";
    public static final String HOSPITALITY_DAILY_SALES_SAVED = "hospitality.daily_sales.saved";
    public static final String HOSPITALITY_FNF_CREATED = "hospitality.fnf.created";
    public static final String HOSPITALITY_FNF_SETTLED = "hospitality.fnf.settled";
    public static final String HOSPITALITY_SWAP_REQUESTED = "hospitality.swap.requested";
    public static final String HOSPITALITY_SWAP_DECIDED = "hospitality.swap.decided";
    public static final String HOSPITALITY_AVAILABILITY_SAVED = "hospitality.availability.saved";

    public static final String SETTINGS_STORE_SAVED = "settings.store.saved";

    private ActivityActions() {
    }

    public static List<ActionDef> catalog() {
        return List.of(
                def(AUTH_LOGIN_SUCCESS, ActivityModule.AUTH, "Signed in"),
                def(AUTH_LOGIN_FAILURE, ActivityModule.AUTH, "Sign-in failed"),
                def(AUTH_PASSWORD_RESET, ActivityModule.AUTH, "Reset password"),
                def(AUTH_PASSWORD_UPDATED, ActivityModule.AUTH, "Updated password"),
                def(AUTH_SUPPORT_SESSION, ActivityModule.AUTH, "Minted support session"),
                def(STAFF_CREATED, ActivityModule.STAFF, "Created staff"),
                def(STAFF_UPDATED, ActivityModule.STAFF, "Updated staff"),
                def(ROLE_CREATED, ActivityModule.ROLES, "Created role"),
                def(ROLE_UPDATED, ActivityModule.ROLES, "Updated role"),
                def(ROLE_ASSIGNED, ActivityModule.ROLES, "Assigned role"),
                def(SHIFT_ROSTER_UPDATED, ActivityModule.SHIFTS, "Updated roster"),
                def(SHIFT_SLOT_SAVED, ActivityModule.SHIFTS, "Saved shift template"),
                def(SHIFT_WEEKLY_OFF_UPDATED, ActivityModule.SHIFTS, "Updated weekly off"),
                def(ATTENDANCE_REGULARIZATION_REQUESTED, ActivityModule.ATTENDANCE, "Requested punch correction"),
                def(ATTENDANCE_REGULARIZATION_APPROVED, ActivityModule.ATTENDANCE, "Approved punch correction"),
                def(ATTENDANCE_REGULARIZATION_REJECTED, ActivityModule.ATTENDANCE, "Rejected punch correction"),
                def(ATTENDANCE_RETRIEVED, ActivityModule.ATTENDANCE, "Retrieved attendance"),
                def(ATTENDANCE_MONTH_CALCULATED, ActivityModule.ATTENDANCE, "Calculated monthly attendance"),
                def(ATTENDANCE_CLOCKED, ActivityModule.ATTENDANCE, "Clocked in or out"),
                def(LEAVE_APPLIED, ActivityModule.LEAVE, "Applied for leave"),
                def(LEAVE_APPROVED, ActivityModule.LEAVE, "Approved leave"),
                def(LEAVE_REJECTED, ActivityModule.LEAVE, "Rejected leave"),
                def(LEAVE_CANCEL_REQUESTED, ActivityModule.LEAVE, "Requested leave cancellation"),
                def(LEAVE_CANCELLED, ActivityModule.LEAVE, "Cancelled leave"),
                def(LEAVE_CANCEL_REJECTED, ActivityModule.LEAVE, "Rejected leave cancellation"),
                def(LEAVE_TYPE_SAVED, ActivityModule.LEAVE, "Saved leave type"),
                def(LEAVE_PLAN_SAVED, ActivityModule.LEAVE, "Saved leave plan"),
                def(LEAVE_PLAN_ASSIGNED, ActivityModule.LEAVE, "Assigned leave plan"),
                def(LEAVE_RULE_SAVED, ActivityModule.LEAVE, "Saved leave rule"),
                def(LEAVE_RULE_DELETED, ActivityModule.LEAVE, "Deleted leave rule"),
                def(LEAVE_BALANCE_ADJUSTED, ActivityModule.LEAVE, "Adjusted leave balance"),
                def(LEAVE_HOLIDAY_CREATED, ActivityModule.LEAVE, "Added holiday"),
                def(LEAVE_HOLIDAY_DELETED, ActivityModule.LEAVE, "Removed holiday"),
                def(LEAVE_OVERRIDE_SAVED, ActivityModule.LEAVE, "Saved leave override"),
                def(PAYROLL_CALCULATED, ActivityModule.PAYROLL, "Calculated payroll"),
                def(PAYROLL_APPLIED, ActivityModule.PAYROLL, "Applied hospitality payroll"),
                def(PAYROLL_COMPONENTS_SAVED, ActivityModule.PAYROLL, "Updated salary components"),
                def(PAYROLL_RUN_CALCULATED, ActivityModule.PAYROLL, "Calculated pay run"),
                def(PAYROLL_RUN_SUBMITTED, ActivityModule.PAYROLL, "Submitted pay run"),
                def(PAYROLL_RUN_APPROVED, ActivityModule.PAYROLL, "Approved pay run"),
                def(PAYROLL_RUN_PUBLISHED, ActivityModule.PAYROLL, "Published pay run"),
                def(PAYROLL_RUN_LOCKED, ActivityModule.PAYROLL, "Locked pay run"),
                def(PAYROLL_STATUTORY_SAVED, ActivityModule.PAYROLL, "Saved statutory configuration"),
                def(PAYROLL_CLAIM_CREATED, ActivityModule.PAYROLL, "Submitted reimbursement"),
                def(PAYROLL_CLAIM_APPROVED, ActivityModule.PAYROLL, "Approved reimbursement"),
                def(PAYROLL_CLAIM_REJECTED, ActivityModule.PAYROLL, "Rejected reimbursement"),
                def(PAYROLL_DECLARATION_SAVED, ActivityModule.PAYROLL, "Submitted IT declaration"),
                def(PAYROLL_DECLARATION_APPROVED, ActivityModule.PAYROLL, "Approved IT declaration"),
                def(PAYROLL_DECLARATION_REJECTED, ActivityModule.PAYROLL, "Rejected IT declaration"),
                def(PAYROLL_LOAN_CREATED, ActivityModule.PAYROLL, "Created loan"),
                def(PAYROLL_LOAN_CLOSED, ActivityModule.PAYROLL, "Closed loan"),
                def(PAYROLL_REVISION_SAVED, ActivityModule.PAYROLL, "Saved salary revision"),
                def(PAYROLL_FNF_CREATED, ActivityModule.PAYROLL, "Drafted settlement"),
                def(PAYROLL_FNF_SETTLED, ActivityModule.PAYROLL, "Settled full and final"),
                def(PAYROLL_LOGO_SAVED, ActivityModule.PAYROLL, "Updated payslip logo"),
                def(HOSPITALITY_ADVANCE_CREATED, ActivityModule.HOSPITALITY, "Issued advance"),
                def(HOSPITALITY_ADVANCE_CLOSED, ActivityModule.HOSPITALITY, "Closed advance"),
                def(HOSPITALITY_COMMISSION_SAVED, ActivityModule.HOSPITALITY, "Saved commission plan"),
                def(HOSPITALITY_COMMISSION_DELETED, ActivityModule.HOSPITALITY, "Deleted commission plan"),
                def(HOSPITALITY_SALE_RECORDED, ActivityModule.HOSPITALITY, "Recorded sale"),
                def(HOSPITALITY_SALES_IMPORTED, ActivityModule.HOSPITALITY, "Imported sales"),
                def(HOSPITALITY_TIP_POOL_CREATED, ActivityModule.HOSPITALITY, "Created tip pool"),
                def(HOSPITALITY_TIP_POOL_DISTRIBUTED, ActivityModule.HOSPITALITY, "Distributed tip pool"),
                def(HOSPITALITY_STATUTORY_SAVED, ActivityModule.HOSPITALITY, "Saved statutory config"),
                def(HOSPITALITY_DAILY_SALES_SAVED, ActivityModule.HOSPITALITY, "Saved daily sales"),
                def(HOSPITALITY_FNF_CREATED, ActivityModule.HOSPITALITY, "Created full and final"),
                def(HOSPITALITY_FNF_SETTLED, ActivityModule.HOSPITALITY, "Settled full and final"),
                def(HOSPITALITY_SWAP_REQUESTED, ActivityModule.HOSPITALITY, "Requested shift swap"),
                def(HOSPITALITY_SWAP_DECIDED, ActivityModule.HOSPITALITY, "Decided shift swap"),
                def(HOSPITALITY_AVAILABILITY_SAVED, ActivityModule.HOSPITALITY, "Updated availability"),
                def(SETTINGS_STORE_SAVED, ActivityModule.SETTINGS, "Updated store hours")
        );
    }

    public static ActivityModule moduleFor(String action) {
        if (action == null) {
            return ActivityModule.SYSTEM;
        }
        for (ActionDef def : catalog()) {
            if (def.action().equals(action)) {
                return def.module();
            }
        }
        int dot = action.indexOf('.');
        if (dot > 0) {
            try {
                return ActivityModule.valueOf(action.substring(0, dot).toUpperCase());
            } catch (IllegalArgumentException ignored) {
                return ActivityModule.SYSTEM;
            }
        }
        return ActivityModule.SYSTEM;
    }

    private static ActionDef def(String action, ActivityModule module, String label) {
        return new ActionDef(action, module, label);
    }

    public record ActionDef(String action, ActivityModule module, String label) {
    }
}
