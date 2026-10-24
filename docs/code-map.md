# Code map

> **Audience:** Backend developers  
> **Purpose:** Show where to look before changing a feature  
> **Last verified:** 2026-10-24  
> **Source of truth:** `src/main/java/com/relfor/pcs/payroll`

The base package is `com.relfor.pcs.payroll`. The application entry point is `PayrollManagementApplication`, which enables scheduling and async execution.

## Packages

| Package | Responsibility | Start here |
| --- | --- | --- |
| `controller` | HTTP routes | The controller named for the feature |
| `service` | Business rules | Service used by that controller |
| `repository` | JPA and custom queries | Repository for the entity being changed |
| `entity` | Tables and relationships | Matching `@Table` class |
| `dto`, `model`, `projection` | Request, response, and query shapes | DTO named in the controller signature |
| `security` | JWT filter, user loading, permission constants | `JwtAuthFilter`, `CustomUserDetailsService`, `Permissions` |
| `config` | Security chain, startup seeding, request scope | `SecurityConfig`, `DataSeeder` |
| `activity` | Append-only store activity log API | `ActivityRecorder`, `ActivityActions`, `ActivityLogController` |
| `handler` | Vendor-specific attendance retrieval | `TeamOfficeHandler` |
| `scheduler` | Due biometric jobs | `AttendanceSchedulerService` |
| `exceptions` | Shared API error body | `ExceptionAdvisor` |
| `util` | Response envelope and tenant override helper | `ResponseHandler`, `SecurityUtils` |

## Feature entry points

| Change | Controller | Main service |
| --- | --- | --- |
| Login and password reset | `AuthController` | `AuthService` |
| Staff profile | `AttendanceManagementController` | `AttendanceManagementService` |
| Roles | `RoleController` | Role repositories directly |
| Shifts, roster, reports | `SStaffShiftsController` | `StaffShiftsService` |
| Attendance history and legacy regularization | `ResparkAttendanceController` | `ResparkAttendanceService`, `ResparkInOutHistoryService` |
| Newer regularization API | `AttendanceRegularizationController` | `AttendanceRegularizationService` |
| Leave requests | `LeaveManagementController` | `LeaveManagementService`, `LeaveLedgerService` |
| Leave policy | `LeavePlanAdminController` | Leave repositories and ledger service |
| Holidays | `StoreHolidayController` | `StoreHolidayService` |
| Salary and payslips | `SalaryCalculationController` | `SalaryCalculation`, `VelocityService`, `PayslipExcelGenerationService` |
| Pay run, claims, loans, settlements, filings, TOTP | `controller.payroll.PayrollController` | `PayRunService`, `PayrollOpsService`, `TotpLoginService` |
| Hospitality payroll | `HospitalityController` | `HospitalityService` |
| Manual payroll jobs | `PayrollTriggerController` | `PayrollTriggerService` |
| External staff import | `StaffSyncController` | `ExternalStaffSyncService` |
| HR metrics | `HRDashboardController` | `HRDashboardService` |
| Home landing | `HomeDashboardController` | `HomeDashboardService` |
| Activity log | `ActivityLogController` | `ActivityLogService` (`ActivityRecorder`) |

Leave accrual jobs live in `LeaveAccrualSchedulerService`, not under `scheduler`.

## Resources

| Path | Contents |
| --- | --- |
| `src/main/resources/application.properties` | Ports, datasource, TLS, messaging, mail |
| `src/main/resources/templates/paySlip.vm` | Payslip HTML template |
| `src/main/resources/sql` | Reference SQL and triggers; not an automated migration set |
| `src/test/java` | Currently one leave-accrual unit test |

## Two regularization APIs

The UI calls the older routes on `ResparkAttendanceController` (`/regularizeAttendance`, `/regularizationRequests`, `/flagRegularizationRequests`). `AttendanceRegularizationController` exposes `/regularizations/*`. Do not merge or rename one without checking both callers.

## Historical files at the repository root

`PROJECT_REVIEW.md` is a point-in-time review. `HashGen.java` and `InsertData.java` are local helper programs and are not part of the Spring Boot runtime. Do not add new operational scripts with embedded credentials.
