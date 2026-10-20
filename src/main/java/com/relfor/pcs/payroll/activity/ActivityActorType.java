package com.relfor.pcs.payroll.activity;

/**
 * Who produced the event. USER is filled from the JWT when possible.
 * SYSTEM/SCHEDULER/INTEGRATION are for jobs and inbound sync.
 */
public enum ActivityActorType {
    USER,
    SYSTEM,
    SCHEDULER,
    INTEGRATION
}
