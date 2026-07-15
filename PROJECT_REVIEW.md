# Payroll Service Project Review

Review date: 2026-07-07

## Scope

This review covers the full Spring Boot payroll service, including:

- Authentication and JWT security
- OTP/password reset
- Role and permission enforcement
- Staff, attendance, leave, shift, and payroll controllers
- Salary calculation and payslip flows
- Scheduled attendance retrieval
- Database access, SQL resources, and persistence patterns
- Build, dependency, test, and repository hygiene

This was a static code review. Maven tests were not executed because `mvn` is not installed in the current environment, and the project does not currently contain a `src/test` test suite.

## Executive Summary

The project has a solid functional shape for a payroll system: staff management, attendance retrieval, leave plans, payroll calculation, payslip generation, and role-based permissions are all present. However, several security and operational issues should be fixed before production use.

The highest-risk areas are:

- Secrets committed in source code and resources
- Public actuator endpoints
- Weak tenant/store/staff authorization boundaries
- Unsafe native SQL sorting
- Inconsistent password hashing
- Weak OTP reset implementation
- Missing scheduling/async enablement
- Payroll and leave flows with concurrency and date-cycle risks
- No automated tests for critical payroll logic

## Critical Findings

### 1. Secrets Are Committed

Files involved:

- `src/main/resources/application.properties`
- `src/main/resources/keystore.jks`
- `src/main/java/com/relfor/pcs/payroll/security/JwtUtil.java`
- `CheckDB.java`

Risk:

Database credentials, SSL keystore credentials, trust-store credentials, and the JWT signing secret are committed directly in the project. Anyone with repository access can connect to local or reused environments, forge JWTs, or misuse certificates.

Recommended fix:

- Rotate all exposed secrets immediately.
- Remove secrets from git history.
- Move secrets to environment variables, Vault, AWS Secrets Manager, Kubernetes secrets, or equivalent.
- Replace hardcoded JWT secret with a strong external secret.
- Add `.gitignore` for `target/`, `*.class`, `*.jar`, `*.jks`, local scripts, and generated files.

### 2. Actuator Endpoints Are Public

Files involved:

- `src/main/java/com/relfor/pcs/payroll/config/SecurityConfig.java`
- `src/main/resources/application.properties`

Risk:

`/actuator/**` is allowed without authentication, while all actuator endpoints are exposed with `management.endpoints.web.exposure.include=*`. This can leak runtime, health, metrics, mappings, and operational data.

Recommended fix:

- Do not permit `/actuator/**` publicly.
- Expose only required endpoints, usually `health` and `prometheus`.
- Put actuator behind admin authentication or private network access.
- Keep detailed health information disabled for public users.

### 3. Multi-Tenant Authorization Is Incomplete

Files involved:

- `AttendanceManagementController.java`
- `LeaveManagementController.java`
- `AttendanceRegularizationController.java`
- `PayrollTriggerController.java`
- `SalaryCalculationController.java`
- `RoleController.java`
- `SecurityUtils.java`

Risk:

Many endpoints accept `tenantId`, `storeId`, `staffId`, `managerId`, or `approverId` directly from requests. This can allow an authenticated user to read or modify another tenant, store, staff member, salary, leave, or attendance record.

Examples:

- Staff data lookup by arbitrary staff IDs
- Payslip and salary component lookup by arbitrary staff ID
- Leave apply/cancel using request-provided staff ID
- Leave reject/cancellation endpoints missing `MANAGE_LEAVES`
- Attendance regularization approval endpoints missing explicit permission checks
- Payroll trigger endpoints accessible to any authenticated user

Recommended fix:

- Always derive tenant, store, and staff identity from the authenticated JWT principal.
- Use request IDs only after verifying that the resource belongs to the principal's tenant/store.
- Add method-level authorization to all sensitive endpoints.
- Create helper methods such as `requireTenantStoreAccess`, `requireStaffAccess`, and `requireManagerPermission`.
- Add tests for cross-tenant access attempts.

### 4. Native SQL Sorting Is Injectable

File involved:

- `src/main/java/com/relfor/pcs/payroll/repository/PersonnelAttendanceCustomRepoImpl.java`

Risk:

`sortField` and `sortOrder` are appended directly into native SQL. Parameters protect values, but not SQL identifiers or keywords.

Recommended fix:

- Whitelist allowed sort fields using a map from API field name to SQL column.
- Accept only `ASC` or `DESC` for sort direction.
- Reject unknown fields with HTTP 400.

Example approach:

```java
Map<String, String> allowedSorts = Map.of(
    "attendanceDate", "pa.attendance_date",
    "personnelName", "personnelName",
    "currentStatus", "pa.current_status"
);
```

### 5. Password Update Stores Plaintext

File involved:

- `src/main/java/com/relfor/pcs/payroll/service/AttendanceManagementService.java`

Risk:

Staff creation hashes passwords with BCrypt, but `updatePasswordForPersonnel` saves `staffDTO.getPwd()` directly. This creates plaintext password storage and also breaks BCrypt login for updated passwords.

Recommended fix:

- Always store `passwordEncoder.encode(newPassword)`.
- Require current password for self-service changes.
- Require admin permission for administrative password resets.
- Never return password fields in DTOs.

## High-Risk Findings

### 6. Inactive Staff Can Still Authenticate

Files involved:

- `CustomUserDetails.java`
- `AuthController.java`
- `CustomUserDetailsService.java`

Risk:

`isEnabled()` always returns `true`. Login returns the active flag, but it does not block inactive users.

Recommended fix:

- Return `personnel.getActive() == true` from `isEnabled()`.
- Block inactive users in login before token generation.
- Consider account lock, credential expiry, and deleted-user states.

### 7. OTP Reset Flow Is Weak

Files involved:

- `AuthService.java`
- `StaffPasswordResetOTP.java`
- `StaffPasswordResetOTPRepository.java`

Risk:

The OTP flow uses `new Random()`, stores OTP and reset token in plaintext, returns user-enumeration messages, and looks up reset tokens without validating expiry.

Recommended fix:

- Use `SecureRandom`.
- Store hashed OTP and hashed reset token.
- Return generic messages such as "If the account exists, an OTP has been sent."
- Rate-limit by username, IP, and device.
- Check token expiry during reset.
- Expire or invalidate all prior reset tokens.

### 8. Scheduled And Async Logic May Not Run

Files involved:

- `PayrollManagementApplication.java`
- `AttendanceSchedulerService.java`
- `AsyncLeaveAttendanceSyncService.java`
- `AsyncAttendanceSummaryCalculation.java`

Risk:

The code uses `@Scheduled` and `@Async`, but the application class does not enable scheduling or async execution.

Recommended fix:

Add:

```java
@EnableScheduling
@EnableAsync
```

to the application configuration.

### 9. Scheduler Advances Jobs Even After Failure

File involved:

- `AttendanceSchedulerService.java`

Risk:

The scheduler moves each invocation time forward before retrieval and saves the changed records even when retrieval fails. This can skip attendance retrieval windows.

Recommended fix:

- Mark a job `PROCESSING`.
- Execute retrieval.
- Only advance invocation time after successful processing.
- On failure, keep the same invocation time and store retry/error metadata.
- Add idempotency protection.

### 10. Salary Formula Evaluation Uses MVEL

File involved:

- `SalaryCalculation.java`

Risk:

Salary formulas and conditions are evaluated as MVEL expressions from database strings. This is powerful but dangerous if non-technical admins or compromised users can modify salary rules.

Recommended fix:

- Replace MVEL with a constrained formula DSL.
- If MVEL is retained, strictly restrict who can edit formulas.
- Validate formulas before saving.
- Restrict accessible variables to approved payroll fields.
- Add regression tests for every salary component rule.

### 11. Leave Balance Can Race

Files involved:

- `LeaveLedgerService.java`
- `LeaveManagementService.java`
- `LeaveTransactionLedgerRepository.java`

Risk:

Leave allocation uses "count then insert" without a unique constraint or lock. Approval debits leave without rechecking balance at approval time.

Recommended fix:

- Add a unique constraint for annual allocation per staff, leave type, and year.
- Recheck balance during approval in the same transaction.
- Use pessimistic locking or versioning for leave approvals.
- Add tests for parallel leave approvals.

## Medium-Risk Findings

### 12. Input Validation Is Mostly Missing

Files involved:

- Multiple controllers and DTOs

Risk:

Most request bodies are raw DTOs without validation, and leave/regularization flows parse `Map<String,Object>`. Invalid payloads can cause runtime errors, inconsistent data, or unclear responses.

Recommended fix:

- Create dedicated request DTOs.
- Use `@Valid`, `@NotNull`, `@NotBlank`, `@Min`, `@Max`, `@Email`, and `@Pattern`.
- Add centralized validation errors.
- Avoid raw `Map<String,Object>` for business APIs.

### 13. Database Schema Management Is Unsafe

Files involved:

- `application.properties`
- `src/main/resources/sql/*`

Risk:

`spring.jpa.hibernate.ddl-auto=update` is enabled, while SQL table, index, and trigger scripts are stored as loose resources. Environments can drift silently.

Recommended fix:

- Use Flyway or Liquibase.
- Convert SQL files into ordered migrations.
- Disable `ddl-auto=update` outside local development.
- Include indexes, constraints, triggers, and seed data in migrations.

### 14. Payroll Month Cycle Has Edge Case Bugs

Files involved:

- `AttendanceManagementService.java`
- `PayrollTriggerService.java`

Risk:

Manual and scheduled monthly summary calculation duplicate date-cycle logic. The scheduled path appears to produce invalid dates for some January/non-1 salary-cycle cases.

Recommended fix:

- Extract salary-cycle date calculation to one tested utility.
- Test:
  - January
  - December
  - salary cycle start day 1
  - salary cycle start day not 1
  - leap years

### 15. Staff DTOs Contain Sensitive Data

Files involved:

- `StaffDTO.java`
- `AttendanceManagementService.java`

Risk:

DTOs contain password, OTP, salary, bank account, documents, emergency contacts, and contact information. Some `toString()` methods include sensitive fields.

Recommended fix:

- Split request DTOs from response DTOs.
- Remove sensitive fields from `toString()`.
- Mask bank account and PAN/UAN data in responses unless explicitly required.
- Limit staff detail access by permission.

### 16. Build And Repository Hygiene Needs Cleanup

Files/directories involved:

- `target/`
- `*.class`
- `CheckDB.java`
- `test-login.js`
- `keystore.jks`

Risk:

Generated build artifacts and local helper scripts are present in the project. This makes reviews noisy and increases risk of leaking secrets or stale compiled classes.

Recommended fix:

- Add `.gitignore`.
- Remove generated files from git.
- Add Maven wrapper: `mvnw`, `mvnw.cmd`, `.mvn/wrapper`.
- Move local scripts into a `tools/` folder or remove them.

## Logically Complex Flows To Review And Test

### Payroll Calculation Flow

Path:

1. Attendance retrieval
2. Day-wise summary calculation
3. Month-wise summary calculation
4. Salary component formula calculation
5. Payslip history save/update
6. PDF/Excel export

Main risks:

- Date-cycle errors
- Duplicate payslip histories
- Missing salary component definitions
- Formula execution risks
- Incorrect deductions when monthly components are edited

Tests to add:

- Salary calculation for normal month
- Salary cycle crossing month boundary
- January/December cycle
- Missing attendance summary
- Missing salary definitions
- Manual monthly deduction edits
- Recalculation should be idempotent

### Leave Application Flow

Path:

1. Employee applies for leave
2. Balance is checked
3. Manager approves/rejects
4. Ledger debit is recorded
5. Attendance and shift data is synced
6. Cancellation may refund ledger and revert attendance

Main risks:

- Race conditions during approval
- Staff applying with another staff ID
- Manager ID supplied by request body
- Async attendance sync failure after leave approval
- Balance not rechecked during approval

Tests to add:

- Apply with insufficient balance
- Apply overlapping leave
- Parallel approvals
- Cancel pending leave
- Cancel approved leave
- Reject cancellation
- Cross-tenant leave approval attempt

### Attendance Regularization Flow

Path:

1. Staff submits regularization
2. Manager approves or rejects
3. Attendance punch records are updated
4. Attendance summary recalculates

Main risks:

- Approval endpoints missing permission checks
- Request body supplies approver ID
- Native SQL sort injection
- Race conditions in punch status updates
- Summary recalculation drift

Tests to add:

- Approve pending request
- Reject pending request
- Approve already approved request
- Sort field whitelist validation
- Cross-tenant regularization access

### Scheduled Attendance Retrieval Flow

Path:

1. Scheduler finds due events
2. Retrieves vendor attendance
3. Saves punches
4. Updates store last retrieval timestamp
5. Calculates summaries
6. Advances next scheduler invocation

Main risks:

- Scheduler annotations not enabled
- External API call has no timeout/retry policy
- Failed retrieval can still advance invocation time
- Store timestamp can be updated even when data retrieval fails
- Vendor URL is assembled manually

Tests to add:

- Vendor success
- Vendor timeout
- Vendor error response
- Duplicate scheduled run
- Partial store failure
- Retry without data loss

## Recommended Remediation Roadmap

### Phase 1: Immediate Security Fixes

- Rotate all committed secrets.
- Move secrets to environment/config server.
- Remove public actuator access.
- Add `.gitignore` and remove generated artifacts.
- Fix plaintext password update.
- Block inactive users from login.
- Add permission checks to payroll trigger, leave reject/cancel, regularization approval, staff management, and payslip endpoints.
- Whitelist native SQL sort fields.

### Phase 2: Stabilize Core Payroll Logic

- Enable scheduling and async support.
- Fix scheduler retry/advance behavior.
- Extract and test salary-cycle date calculations.
- Add approval-time leave balance validation.
- Add uniqueness/idempotency constraints for leave allocations and payslip histories.
- Replace raw `Map<String,Object>` request parsing with validated DTOs.

### Phase 3: Operational Maturity

- Add Flyway or Liquibase migrations.
- Add Maven wrapper.
- Add unit and integration tests.
- Add security tests for tenant isolation.
- Add dependency vulnerability scanning.
- Add structured logging without sensitive data.
- Add API documentation.

## Suggested Test Suite

Minimum test coverage before production:

- Auth login success/failure/inactive user
- JWT tenant/store claims and authorization
- OTP generation, expiry, failed attempts, and reset
- Staff create/update/password update
- Cross-tenant access denial
- Leave apply/approve/reject/cancel
- Parallel leave approvals
- Attendance regularization approval/rejection
- Payroll month-cycle calculations
- Salary formula evaluation
- Payslip generation and recalculation idempotency
- Scheduler success/failure/retry behavior

## Final Recommendation

The service is a good functional start, but it should not be deployed to production until the critical security items are fixed. The most important architectural improvement is to centralize tenant/store/staff authorization and make every sensitive workflow use that shared guardrail. After that, payroll date-cycle tests and leave/attendance concurrency tests will give the project a much safer foundation.
