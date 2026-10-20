# Activity log for developers

> **Audience:** Backend developers  
> **Purpose:** Show how to record store activity from any feature  
> **Last verified:** 2026-10-20  
> **Source of truth:** `com.relfor.pcs.payroll.activity`

The activity log is a single append-only table (`activity_log`) and a single write API. Do not add per-feature history tables for dashboard-visible “who did this”. Leave application status history (`leave_application_logs`) can stay as workflow state; still emit a store activity event for the same mutation.

## Record an event

```java
@Autowired
private ActivityRecorder activityRecorder;

activityRecorder.record(ActivityEvent.builder()
        .action(ActivityActions.LEAVE_APPROVED)
        .resourceType(ActivityResourceTypes.LEAVE_APPLICATION)
        .resourceId(application.getId())
        .resourceLabel(staffName)
        .summary("Approved casual leave for Priya")
        .context("from", startDate)
        .context("to", endDate)
        .build());
```

Actor, tenant, store, IP, user-agent, and correlation id are filled from the current request when omitted. For schedulers, set `.actorType(ActivityActorType.SCHEDULER)` and pass tenant/store.

Use `record()` inside the business transaction so a failed insert rolls back the mutation. Use `recordSafely()` only for sign-in paths that must succeed even if the log insert fails.

## Add a new action

1. Add a `module.verb` constant on `ActivityActions`.
2. Add a catalog entry with module and UI label.
3. Reuse `ActivityModule` / `ActivityResourceTypes` values. Add an enum/constant if the product area is new.
4. Never rename an existing action string; filters and exports key off it.

Optional: `@RecordActivity(action = "...", module = ActivityModule.STAFF, resourceIdParam = "staffId")` on a method that returns normally. Prefer the explicit recorder when the summary is computed inside the method.

## What not to put in context

`ActivityRedactor` drops passwords, OTPs, tokens, salary, bank, PAN, UAN, and geolocation keys. Still avoid sending those fields. Summaries should be readable without opening the row.

## Query API

`GET /payroll-management/v1/activity` is `VIEW_ACTIVITY_LOG` only and scoped to the JWT tenant/store. That permission does not grant operations Home (`GET /home/operations`). Filters (`module`, `action`, `q`, `from`, `to`) are applied in the query; `page` (0-based) and `size` (default 20, max 100) return that slice plus `total` and `totalPages`. Search matches summary, actor display name, username, resource label, and action. There is no update or delete endpoint. MySQL triggers reject `UPDATE`/`DELETE` on `activity_log`.
