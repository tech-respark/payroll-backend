# Payroll service

Spring Boot API for Relfor Payroll. It serves the React application in the sibling `payroll-frontend` repository and persists to MySQL database `payroll_management`.

## Start here

| Need | Document |
| --- | --- |
| Run the API locally | [Getting started](docs/getting-started.md) |
| Find the right package | [Code map](docs/code-map.md) |
| Follow a request | [Request lifecycle](docs/request-lifecycle.md) |
| Tests and operations | [Testing and operations](docs/testing-and-operations.md) |
| Product, screens, client, and sales docs | [Frontend documentation portal](https://github.com/tech-respark/payroll-frontend/blob/staging/docs/README.md) |

The frontend repository is the shared documentation portal. This repository documents how the service is built and run.

## Stack

- Java 21
- Spring Boot 3.3
- Spring Security with stateless JWT
- Spring Data JPA and MySQL 8
- Maven

The service listens on HTTPS port **8092**. The frontend development server on port **5174** proxies `/payroll-management/v1` to that port.

## Build

```bash
export JAVA_HOME='<path-to-java-21>'
export SPRING_PROFILES_ACTIVE=local
mvn test
mvn spring-boot:run
```

Database URL, username, password, SSL keystore passwords, and `JWT_SECRET` must come from the environment or gitignored `application-local.properties`. Do not commit local credentials. See [getting started](docs/getting-started.md). QA and production follow the frontend portal page `docs/operations/qa-and-production-deployment.md`.

## What not to treat as current

`PROJECT_REVIEW.md` is a historical review from 2026-07-07. Some findings remain relevant, including incomplete trigger authorization and historically leaked secrets that still need rotation. Other statements are stale: scheduling and async execution are now enabled on the application class, tracked properties expose only `health`, and the security matcher permits `/actuator/health` rather than `/actuator/**`.

Current executable code is the source of truth. Product behavior and known gaps are maintained in the frontend documentation portal.
