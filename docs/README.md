# Backend developer documentation

> **Audience:** Backend developers and operators  
> **Purpose:** Navigate service-specific documentation  
> **Last verified:** 2026-10-20  
> **Source of truth:** This repository's current code

## Pages

- [Getting started](getting-started.md) — Java, database, HTTPS, and first run
- [Code map](code-map.md) — packages and where to change each feature
- [Request lifecycle](request-lifecycle.md) — security, controllers, services, and responses
- [Testing and operations](testing-and-operations.md) — tests, schedulers, logs, and safe changes
- Shared QA and production deployment: frontend portal `docs/operations/qa-and-production-deployment.md`

## Shared product documentation

Screen flows, client and sales material, the API catalog, permissions, data model, onboarding, and known limitations are maintained in the frontend repository:

[https://github.com/tech-respark/payroll-frontend/blob/staging/docs/README.md](https://github.com/tech-respark/payroll-frontend/blob/staging/docs/README.md)

Update that portal in the same change when an API, permission, entity, scheduler, or integration behavior changes. Do not copy secrets, personal data, or production connection details into either repository's documentation.
