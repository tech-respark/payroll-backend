package com.relfor.pcs.payroll.activity;

/**
 * Per-request snapshot (IP, user-agent, correlation, HTTP path) populated by
 * {@link ActivityRequestFilter}. Cleared at the end of the request.
 */
public final class ActivityRequestContext {

    private static final ThreadLocal<Snapshot> CURRENT = new ThreadLocal<>();

    private ActivityRequestContext() {
    }

    public static void set(Snapshot snapshot) {
        CURRENT.set(snapshot);
    }

    public static Snapshot get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }

    public record Snapshot(
            String ipAddress,
            String userAgent,
            String correlationId,
            String method,
            String path
    ) {
    }
}
