# Testing and operations

> **Audience:** Backend developers and operators  
> **Purpose:** Run tests and understand scheduled work  
> **Last verified:** 2026-10-20  
> **Source of truth:** `src/test`, scheduler classes, and `application.properties`

## Tests

The repository currently has one unit test:

`src/test/java/com/relfor/pcs/payroll/service/LeaveAccrualSchedulerServiceTest.java`

```bash
export JAVA_HOME='<path-to-java-21>'
mvn test
```

A successful run reports 2 tests, 0 failures. There is no integration-test suite for authentication, payroll calculation, or cross-tenant access. Add tests next to the behavior being changed rather than treating the current suite as sufficient coverage.

## Schedulers

All schedules run inside this JVM. More than one running instance can duplicate work until a lock or leader election exists.

| Job | Class | Schedule |
| --- | --- | --- |
| Biometric reminder | `AttendanceSchedulerService` | Fixed delay, default 1,200,000 ms |
| Monthly leave accrual | `LeaveAccrualSchedulerService` | 00:00 on day 1 of each month |
| Quarterly leave accrual | `LeaveAccrualSchedulerService` | 00:00 on day 1 of January, April, July, and October |
| Annual expiry and grant | `LeaveAccrualSchedulerService` | 00:01 on 1 January |

The biometric job selects `ATTENDANCEDATAREMINDER` rows from one hour ago through 100 minutes ahead. It moves `invocation_time` forward one day only after `retrieveScheduledAttendanceData` returns successfully. A failure leaves the row due, but a row older than the one-hour lookback is not selected again.

Leave accrual writes ledger rows for every enrollment whose rule frequency matches the job. It does not check whether that period was already accrued.

## Logs

The configured log file is `logs/payroll-service.log`, relative to the process working directory. `logs/` should stay out of Git. Do not log tokens, passwords, OTPs, or full salary payloads.

## Safe change checklist

- Use Java 21.
- Override datasource credentials with environment variables.
- Run against a disposable database while `ddl-auto=update` remains enabled.
- Run `mvn test`.
- Exercise the changed endpoint with a valid local user and with a user who must be rejected.
- Update the frontend documentation portal when the contract, permission, schema, or schedule changes.
- Leave `PROJECT_REVIEW.md` unchanged unless the team explicitly refreshes that historical review.

Shared QA and production steps live in the frontend portal: `payroll-frontend` `docs/operations/qa-and-production-deployment.md`.

## Production gaps to preserve in design

- Replace `ddl-auto=update` with versioned migrations before sharing a production database.
- Add authorization and ownership checks to remaining JWT-only sensitive routes (including `/trigger/*`).
- Keep actuator limited to `/actuator/health` on public paths.
- Make payroll and leave jobs idempotent across restarts and multiple instances.
- Rotate any secret that previously existed in source.
