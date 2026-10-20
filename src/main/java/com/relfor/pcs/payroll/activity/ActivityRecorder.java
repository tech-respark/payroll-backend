package com.relfor.pcs.payroll.activity;

/**
 * Central write API for the store activity log.
 *
 * <p>Inject this into any service or controller that mutates business state:
 * <pre>{@code
 * activityRecorder.record(ActivityEvent.builder()
 *     .action(ActivityActions.LEAVE_APPROVED)
 *     .resourceType(ActivityResourceTypes.LEAVE_APPLICATION)
 *     .resourceId(application.getId())
 *     .resourceLabel(staffName)
 *     .summary("Approved casual leave for Priya")
 *     .context("from", start)
 *     .context("to", end)
 *     .build());
 * }</pre>
 *
 * <p>Actor, tenant, store, IP, user-agent, and correlation id are filled from
 * the current request when omitted. Call {@link #recordSafely} only for paths
 * that must not fail the user action if the log insert fails (sign-in).
 */
public interface ActivityRecorder {

    void record(ActivityEvent event);

    void recordSafely(ActivityEvent event);
}
