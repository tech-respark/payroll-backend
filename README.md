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
mvn test
mvn spring-boot:run
```

Database URL, username, and password must come from the environment. Do not commit local credentials. See [getting started](docs/getting-started.md).

## What not to treat as current

`PROJECT_REVIEW.md` is a historical review from 2026-07-07. Some findings remain relevant, including committed secrets and incomplete tenant authorization. Other statements are stale: scheduling and async execution are now enabled on the application class, and the properties file exposes only health and Prometheus rather than every actuator endpoint. The security matcher still permits `/actuator/**` without authentication.

Current executable code is the source of truth. Product behavior and known gaps are maintained in the frontend documentation portal.
